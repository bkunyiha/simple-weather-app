package com.example.simpleweatherapp.domain

import cats.effect.Concurrent
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, _}
import org.http4s.circe._

final case class PointForecast(id: String, `type`: String = "Feature", properties: PointForecastsProperties)

final case class PointForecastsProperties(
                                           geometry: Option[String],
                                           `@id`: String,
                                           `@type`: String = "wx:Point",
                                           cwa: String = "AKQ",
                                           forecastOffice: String,
                                           gridId: String = "AKQ",
                                           gridX: Int,
                                           gridY: Int,
                                           forecast: String,
                                           forecastHourly: String,
                                           forecastGridData: String,
                                           observationStations: String,
                                           forecastZone: String,
                                           county: String,
                                           fireWeatherZone: String,
                                           timeZone: String,
                                           radarStation: String)

object PointForecast {
  implicit val pointForecastsPropertiesDecoder: Decoder[PointForecastsProperties] = deriveDecoder[PointForecastsProperties]
  implicit val pointForecastDecoder: Decoder[PointForecast] = deriveDecoder[PointForecast]

  implicit def pointForecastEntityDecoder[F[_] : Concurrent]: EntityDecoder[F, PointForecast] =
    jsonOf

  implicit val pointForecastsPropertiesEncoder: Encoder[PointForecastsProperties] = deriveEncoder[PointForecastsProperties]
  implicit val pointForecastEncoder: Encoder[PointForecast] = deriveEncoder[PointForecast]

  implicit def pointForecastEntityEncoder[F[_]]: EntityEncoder[F, PointForecast] =
    jsonEncoderOf
}


