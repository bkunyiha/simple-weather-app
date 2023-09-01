package com.example.simpleweatherapp.routes

import cats.effect.IO
import com.example.simpleweatherapp.routes.SimpleweatherappRoutes.{Latitude, Longitude}
import com.example.simpleweatherapp.services.{Forecast, WeatherResponse}
import io.circe.Json
import io.circe.syntax.KeyOps
import org.http4s.circe._
import org.http4s.{EntityDecoder, Method, Request, Response, Status, Uri}
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class SimpleweatherappRoutesTest extends AnyFunSuite with MockFactory {

  import cats.effect.unsafe.IORuntime

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val forecastMock = mock[Forecast[IO]]
  val lat = Latitude(39.7456)
  val lon = Longitude(-97.0892)

  test("Get Weather should Return 200 and proper payload") {
    val resp = WeatherResponse("Sunny all day", "hot")
    val expectedJson = Json.obj(
      "shortForecast" := resp.shortForecast,
      "temperature" := resp.temperature
    )
    (forecastMock.weather(_: Latitude, _: Longitude)).expects(lat, lon).returning(IO.pure(resp)).once(): Unit
    assert(check[Json](retGetCart(lat, lon), Status.Ok, Some(expectedJson)))
  }

  private[this] def retGetCart(lat: Latitude, lon: Longitude): IO[Response[IO]] = {
    val getCart = Request[IO](Method.GET, Uri.unsafeFromString(s"/weather/lat/${lat.value}/lon/${lon.value}"))
    SimpleweatherappRoutes.forecastRoutes[IO](forecastMock).orNotFound(getCart)
  }

  private[this] def check[A](actual: IO[Response[IO]],
                             expectedStatus: Status,
                             expectedBody: Option[A])(
                              implicit ev: EntityDecoder[IO, A]
                            ): Boolean = {
    val actualResp = actual.unsafeRunSync()
    val statusCheck = actualResp.status == expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](
      // Verify Response's body is empty.
      actualResp.body.compile.toVector.unsafeRunSync().isEmpty) {
      expected =>
        val result = actualResp.as[A].unsafeRunSync()
        logger.debug(result.toString): Unit
        result == expected
    }
    logger.debug(s"statusCheck -> $statusCheck, bodyCheck -> $bodyCheck"): Unit
    statusCheck && bodyCheck
  }
}
