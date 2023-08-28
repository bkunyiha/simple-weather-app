package com.example.simpleweatherapp

import cats.effect.Async
import com.comcast.ip4s._
import com.example.simpleweatherapp.routes.SimpleweatherappRoutes
import com.example.simpleweatherapp.services.Forecast
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object SimpleweatherappServer {

  def run[F[_]: Async: Network]: F[Nothing] = {
    for {
      client <- EmberClientBuilder.default[F].build
      jokeAlg = Forecast.impl[F](client)

      httpApp = (
        SimpleweatherappRoutes.forecastRoutes[F](jokeAlg)
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
