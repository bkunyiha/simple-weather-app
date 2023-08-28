package com.example.simpleweatherapp.services

import cats.effect.Concurrent
import cats.implicits._
import com.example.simpleweatherapp.domain.{ForecastError, GripPointForecast, PointForecast, UrlError}
import com.example.simpleweatherapp.routes.SimpleweatherappRoutes.{Latitude, Longitude}
import org.http4s.Method._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{ParseFailure, Uri}

trait Forecast[F[_]] {
  def weather(lat: Latitude, lon: Longitude): F[WeatherResponse]

  def forecast(lat: Latitude, lon: Longitude): F[PointForecast]

  def gridPoint(url: String): F[GripPointForecast]
}

object Forecast {
  def apply[F[_]](implicit ev: Forecast[F]): Forecast[F] = ev

  def impl[F[_] : Concurrent](C: Client[F]): Forecast[F] = new Forecast[F] {
    val dsl = new Http4sClientDsl[F] {}
    import dsl._
    import PointForecast._


    def weather(lat: Latitude, lon: Longitude): F[WeatherResponse] = for {
      forecast <- forecast(lat: Latitude, lon: Longitude)
      gridPoint <- gridPoint(forecast.properties.forecast)
      resp <- gridPoint.properties.periods
        .find(_.number == 1)
        .fold(ForecastError(new RuntimeException("Empty GridPoint->Property->Period")).raiseError[F, WeatherResponse]) {
          prop => WeatherResponse(prop.shortForecast, temperature(prop.temperature)).pure[F]
        }
    } yield resp

    def forecast(lat: Latitude, lon: Longitude): F[PointForecast] = {
      Uri.fromString(s"https://api.weather.gov/points/${lat.value},${lon.value}").fold(
        (e: ParseFailure) => {
          e.printStackTrace()
          UrlError(e).raiseError[F, PointForecast]
        },
        uri =>
          C.expect[PointForecast](GET(uri))
            .adaptError { case t =>
              t.printStackTrace()
              ForecastError(t)
            } // Prevent Client Json Decoding Failure Leaking
      )
    }

    def gridPoint(url: String): F[GripPointForecast] = {
      Uri.fromString(url).fold(
        e => {
          e.printStackTrace()
          UrlError(e).raiseError[F, GripPointForecast]
        },
        uri =>
          C.expect[GripPointForecast](GET(uri))
            .adaptError { case t =>
              t.printStackTrace()
              ForecastError(t)
            } // Prevent Client Json Decoding Failure Leaking
      )
    }

    def temperature(temp: Int): String = temp match {
      case t if t <= 62 => "cold"
      case t if t <= 78 => "moderate"
      case _ => "hot"

    }
  }
}
