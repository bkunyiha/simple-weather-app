package com.example.simpleweatherapp.routes

import cats.effect.Sync
import cats.implicits._
import com.example.simpleweatherapp.services.WeatherResponse._
import com.example.simpleweatherapp.services.Forecast
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import scala.util.Try

object SimpleweatherappRoutes {

  case class Latitude(value: Double) extends AnyVal

  case class Longitude(value: Double) extends AnyVal

  private object LatitudeVar {
    def unapply(str: String): Option[Latitude] = {
      Try(Latitude(str.toDouble)).toOption.filter {
        case Latitude(value) => value >= -90.0 && value <= 90.0
      }
    }
  }

  private object LongitudeVar {
    def unapply(str: String): Option[Longitude] = {
      Try(Longitude(str.toDouble)).toOption.filter {
        case Longitude(value) => value >= -180.0 && value <= 180.0
      }
    }
  }

  def forecastRoutes[F[_] : Sync](J: Forecast[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "weather" / "lat" / LatitudeVar(lat) / "lon" / LongitudeVar(lon) =>
        for {
          forecast <- J.weather(lat, lon)
          resp <- Ok(forecast)
        } yield resp
    }
  }
}