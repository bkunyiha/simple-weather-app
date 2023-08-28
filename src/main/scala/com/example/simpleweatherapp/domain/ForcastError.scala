package com.example.simpleweatherapp.domain

import org.http4s.ParseFailure

final case class ForecastError(e: Throwable) extends RuntimeException

final case class UrlError(e: ParseFailure) extends RuntimeException
