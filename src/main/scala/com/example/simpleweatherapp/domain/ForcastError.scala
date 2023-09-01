package com.example.simpleweatherapp.domain

import org.http4s.ParseFailure

final case class ForecastError(e: Throwable) extends RuntimeException
final case class ForecastNotFoundError(e: Throwable) extends RuntimeException
final case class ForecastDecodingError(e: Throwable) extends RuntimeException

final case class UrlError(e: ParseFailure) extends RuntimeException
