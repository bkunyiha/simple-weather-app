package com.example.simpleweatherapp.services

import cats.effect.Concurrent
import cats.implicits._
import com.example.simpleweatherapp.domain._
import io.circe.Json
import org.http4s.Method._
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

trait ForecastClient[F[_]] {
  def httpRequest(url: String): F[Json]
}

object ForecastClient {
  def apply[F[_]](implicit ev: ForecastClient[F]): ForecastClient[F] = ev

  def impl[F[_] : Concurrent](C: Client[F]): ForecastClient[F] = new ForecastClient[F] {
    val dsl = new Http4sClientDsl[F] {}

    import dsl._

    def httpRequest(url: String): F[Json] = {
      Uri.fromString(url).fold(
        e => {
          UrlError(e).raiseError[F, Json]
        },
        uri =>
          C.expect[Json](GET(uri))
            .adaptError { case t =>
              ForecastError(t)
            } // Prevent Client Json Decoding Failure Leaking
      )
    }
  }
}
