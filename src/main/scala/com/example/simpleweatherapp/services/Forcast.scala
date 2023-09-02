package com.example.simpleweatherapp.services

import cats.effect.Concurrent
import cats.implicits._
import com.example.simpleweatherapp.domain.GripPointForecast._
import com.example.simpleweatherapp.domain._
import com.example.simpleweatherapp.routes.SimpleweatherappRoutes.{Latitude, Longitude}
import com.example.simpleweatherapp.services.ForecastOps._
import org.typelevel.log4cats.Logger

trait Forecast[F[_]] {
  def weather(lat: Latitude, lon: Longitude): F[WeatherResponse]

  def forecast(lat: Latitude, lon: Longitude): F[PointForecast]

  def gridPoint(url: String): F[GripPointForecast]
}

object Forecast {
  def apply[F[_]](implicit ev: Forecast[F]): Forecast[F] = ev

  def impl[F[_] : Concurrent](client: ForecastClient[F])(implicit logger: Logger[F]): Forecast[F] = new Forecast[F] {

    import PointForecast._

    def weather(lat: Latitude, lon: Longitude): F[WeatherResponse] = for {
      forecast <- forecast(lat: Latitude, lon: Longitude)
      gridPoint <- gridPoint(forecast.properties.forecast)
      resp <- gridPoint.properties.periods
        .find(_.number == 1)
        .fold(ForecastNotFoundError(new RuntimeException("Empty GridPoint->Property->Period")).raiseError[F, WeatherResponse]) {
          prop => WeatherResponse(prop.shortForecast, temperature(prop.temperature)).pure[F]
        }
    } yield resp

    def forecast(lat: Latitude, lon: Longitude): F[PointForecast] = {
      val url = s"https://api.weather.gov/points/${lat.value},${lon.value}"
      client.httpRequest(url).map(_.as[PointForecast]).flatMap {
        case Right(p) => p.pure[F]
        case Left(error) =>
          logger.error(s"Decoding error decoding PointForecast ${error.getMessage()}"): Unit
          ForecastDecodingError(error).raiseError[F, PointForecast]
      }
    }

    def gridPoint(url: String): F[GripPointForecast] = {
      client.httpRequest(url).map(_.as[GripPointForecast]).flatMap {
        case Right(gp) => gp.pure[F]
        case Left(error) =>
          logger.error(s"Decoding error decoding GripPointForecast ${error.getMessage()}"): Unit
          ForecastDecodingError(error).raiseError[F, GripPointForecast]
      }
    }
  }
}
