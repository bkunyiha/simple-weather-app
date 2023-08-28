package com.example.simpleweatherapp.services

import io.circe.Encoder
import io.circe.generic.semiauto._
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

final case class WeatherResponse(shortForecast: String, temperature: String)

object WeatherResponse {
  implicit val pointForecastEncoder: Encoder[WeatherResponse] = deriveEncoder[WeatherResponse]

  implicit def weatherResponseEntityEncoder[F[_]]: EntityEncoder[F, WeatherResponse] =
    jsonEncoderOf

  implicit def seqWeatherResponseEntityEncoder[F[_]]: EntityEncoder[F, Seq[WeatherResponse]] =
    jsonEncoderOf
}
