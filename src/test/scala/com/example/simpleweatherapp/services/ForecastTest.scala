package com.example.simpleweatherapp.services

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeErrorId
import com.example.simpleweatherapp.domain.{ForecastNotFoundError, GripPointForecast, PointForecast}
import com.example.simpleweatherapp.routes.SimpleweatherappRoutes.{Latitude, Longitude}
import io.circe.Json
import munit.CatsEffectSuite
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class ForecastTest extends CatsEffectSuite {

  import cats.effect.unsafe.IORuntime
  import ForecastTest._

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def fakeForecastClient = new ForecastClient[IO] {
    override def httpRequest(url: String): IO[Json] = {
      if(url.contains("https://api.weather.gov/points"))
        IO.pure(expectedPointForecastJson)
      else if(url.contains(expectedPointForecast.properties.forecast))
        IO.pure(expectedGridPointForecastJson)
      else
        ForecastNotFoundError(new RuntimeException("Invalid URL")).raiseError[IO, Json]
    }
  }
  val forecastService = Forecast.impl[IO](fakeForecastClient)
  val lat = Latitude(39.7456)
  val lon = Longitude(-97.0892)

  test("forecast Should Return the PointForecast object") {
    val pointForecastRes: IO[PointForecast] = forecastService.forecast(lat, lon)
    assertIO(pointForecastRes, expectedPointForecast)
  }

  test("gridPoint Should Return the GripPointForecast object") {
    val gridPointForecastRes: IO[GripPointForecast] = forecastService.gridPoint(expectedPointForecast.properties.forecast)
    assertIO(gridPointForecastRes, expectedGridPointForecast)
  }

  test("weather Should Return the WeatherResponse object") {
    val gridPointPeriod = expectedGridPointForecast.properties.periods.find(_.number == 1).get
    val weatherResponse =  WeatherResponse(
      shortForecast = gridPointPeriod.shortForecast,
      temperature = ForecastOps.temperature(gridPointPeriod.temperature)
    )
    val weatherResponseRes: IO[WeatherResponse] = forecastService.weather(lat, lon)
    assertIO(weatherResponseRes, weatherResponse)
  }
}

object ForecastTest {

  import scala.io.Source
  import io.circe.parser._

  val pointsJsonResource = Source.fromResource("points.json")
  val pointsJsonString = pointsJsonResource.getLines().mkString
  val expectedPointForecastJson: Json = parse(pointsJsonString).toOption.get
  val expectedPointForecast = expectedPointForecastJson.as[PointForecast].toOption.get

  val gridPointsJsonResource = Source.fromResource("gridpoints.json")
  val gridPointJsonString = gridPointsJsonResource.getLines().mkString
  val expectedGridPointForecastJson: Json = parse(gridPointJsonString).toOption.get
  val expectedGridPointForecast = expectedGridPointForecastJson.as[GripPointForecast].toOption.get

}