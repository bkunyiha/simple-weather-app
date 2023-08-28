package com.example.simpleweatherapp.domain

import cats.effect.Concurrent
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, _}
import org.http4s.circe._

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

final case class GripPointForecast(`type`: String = "Feature", properties: GripPointProperties)

final case class GripPointProperties(
                                      units: String = "us",
                                      forecastGenerator: String,
                                      generatedAt: ZonedDateTime,
                                      updateTime: ZonedDateTime,
                                      validTimes: String,
                                      periods: List[GripPointPeriod]
                                    )

final case class GripPointPeriod(
                                  number: Int,
                                  name: String,
                                  startTime: ZonedDateTime,
                                  endTime: ZonedDateTime,
                                  isDaytime: Boolean,
                                  temperature: Int,
                                  temperatureUnit: Char,
                                  temperatureTrend: Option[String],
                                  windDirection: String,
                                  shortForecast: String,
                                  detailedForecast: String)

object GripPointForecast {

  implicit val encodeZonedDateTime: Encoder[ZonedDateTime] = Encoder.encodeString.contramap[ZonedDateTime](_.toString)

  implicit val decodeZonedDateTime: Decoder[ZonedDateTime] = Decoder.decodeString.emapTry { str =>
    Try(ZonedDateTime.parse(str, DateTimeFormatter.ISO_DATE_TIME))
  }

  implicit val gripPointPeriodDecoder: Decoder[GripPointPeriod] = deriveDecoder[GripPointPeriod]
  implicit val gripPointPropertiesDecoder: Decoder[GripPointProperties] = deriveDecoder[GripPointProperties]
  implicit val gripPointForecastDecoder: Decoder[GripPointForecast] = deriveDecoder[GripPointForecast]

  implicit def gripPointEntityDecoder[F[_] : Concurrent]: EntityDecoder[F, GripPointForecast] =
    jsonOf

  implicit val gripPointPeriodEncoder: Encoder[GripPointPeriod] = deriveEncoder[GripPointPeriod]
  implicit val gripPointPropertiesEncoder: Encoder[GripPointProperties] = deriveEncoder[GripPointProperties]
  implicit val GripPointForecastEncoder: Encoder[GripPointForecast] = deriveEncoder[GripPointForecast]

  implicit def gripPointForecastEntityEncoder[F[_]]: EntityEncoder[F, GripPointForecast] =
    jsonEncoderOf
}