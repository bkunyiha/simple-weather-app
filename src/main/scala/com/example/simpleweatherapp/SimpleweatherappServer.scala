package com.example.simpleweatherapp

import cats.effect.Async
import com.comcast.ip4s._
import com.example.simpleweatherapp.routes.SimpleweatherappRoutes
import com.example.simpleweatherapp.services.{Forecast, ForecastClient}
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.{Logger => CatsLogger}

object SimpleweatherappServer {

  def run[F[_]: Async: CatsLogger: Network]: F[Nothing] = {
    for {
      client <- EmberClientBuilder.default[F].build
      forecastClientAlg = ForecastClient.impl[F](client)
      forecastAlg = Forecast.impl[F](forecastClientAlg)

      httpApp = (
        SimpleweatherappRoutes.forecastRoutes[F](forecastAlg)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}
