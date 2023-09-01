package com.example.simpleweatherapp.services

import cats.effect.IO
import com.example.simpleweatherapp.domain.{PointForecast, PointForecastsProperties}
import org.scalamock.scalatest.MockFactory
import com.example.simpleweatherapp.routes.SimpleweatherappRoutes.{Latitude, Longitude}
import munit.CatsEffectAssertions.assertIO
import org.scalatest.funsuite.AnyFunSuite
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class ForecastTest extends AnyFunSuite with MockFactory {

  import cats.effect.unsafe.IORuntime
  import com.example.simpleweatherapp.services.ForecastTest.pointForecastJsonS

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val forecastClientMock = mock[ForecastClient[IO]]
  val forecastService = Forecast.impl[IO](forecastClientMock)
  val lat = Latitude(39.7456)
  val lon = Longitude(-97.0892)


  test("find ShoppingCart By Id Should Return Shopping Cart") {
    val url = s"https://api.weather.gov/points/${lat.value},${lon.value}"

    val pointForecastsProperties = PointForecastsProperties(geometry = None,
      `@id` = "https://api.weather.gov/points/39.7456,-97.0892",
      `@type` = "wx:Point",
      cwa = "TOP",
      forecastOffice = "https://api.weather.gov/offices/TOP",
      gridId = "TOP",
      gridX = 32,
      gridY = 81,
      forecast = "https://api.weather.gov/gridpoints/TOP/32,81/forecast",
      forecastHourly = "https://api.weather.gov/gridpoints/TOP/32,81/forecast/hourly",
      forecastGridData = "https://api.weather.gov/gridpoints/TOP/32,81",
      observationStations = "https://api.weather.gov/gridpoints/TOP/32,81/stations",
      forecastZone = "https://api.weather.gov/zones/forecast/KSZ009",
      county = "https://api.weather.gov/zones/county/KSC201",
      fireWeatherZone = "https://api.weather.gov/zones/fire/KSZ009",
      timeZone = "America/Chicago",
      radarStation = "KTWX"
    )
    val pointForecastRes = PointForecast(pointForecastsProperties.`@id`, "Feature", properties = pointForecastsProperties)
    (forecastClientMock.httpRequest _).expects(url).returning(IO.pure(pointForecastJsonS)).once(): Unit

    val pointForecast: IO[PointForecast] = forecastService.forecast(lat, lon)
    assertIO(pointForecast, pointForecastRes)
  }
}

object ForecastTest {

  import io.circe.parser._

  val pointForecastJsonString =
    """{
      |    "@context": [
      |        "https://geojson.org/geojson-ld/geojson-context.jsonld",
      |        {
      |            "@version": "1.1",
      |            "wx": "https://api.weather.gov/ontology#",
      |            "s": "https://schema.org/",
      |            "geo": "http://www.opengis.net/ont/geosparql#",
      |            "unit": "http://codes.wmo.int/common/unit/",
      |            "@vocab": "https://api.weather.gov/ontology#",
      |            "geometry": {
      |                "@id": "s:GeoCoordinates",
      |                "@type": "geo:wktLiteral"
      |            },
      |            "city": "s:addressLocality",
      |            "state": "s:addressRegion",
      |            "distance": {
      |                "@id": "s:Distance",
      |                "@type": "s:QuantitativeValue"
      |            },
      |            "bearing": {
      |                "@type": "s:QuantitativeValue"
      |            },
      |            "value": {
      |                "@id": "s:value"
      |            },
      |            "unitCode": {
      |                "@id": "s:unitCode",
      |                "@type": "@id"
      |            },
      |            "forecastOffice": {
      |                "@type": "@id"
      |            },
      |            "forecastGridData": {
      |                "@type": "@id"
      |            },
      |            "publicZone": {
      |                "@type": "@id"
      |            },
      |            "county": {
      |                "@type": "@id"
      |            }
      |        }
      |    ],
      |    "id": "https://api.weather.gov/points/39.7456,-97.0892",
      |    "type": "Feature",
      |    "geometry": {
      |        "type": "Point",
      |        "coordinates": [
      |            -97.089200000000005,
      |            39.745600000000003
      |        ]
      |    },
      |    "properties": {
      |        "@id": "https://api.weather.gov/points/39.7456,-97.0892",
      |        "@type": "wx:Point",
      |        "cwa": "TOP",
      |        "forecastOffice": "https://api.weather.gov/offices/TOP",
      |        "gridId": "TOP",
      |        "gridX": 32,
      |        "gridY": 81,
      |        "forecast": "https://api.weather.gov/gridpoints/TOP/32,81/forecast",
      |        "forecastHourly": "https://api.weather.gov/gridpoints/TOP/32,81/forecast/hourly",
      |        "forecastGridData": "https://api.weather.gov/gridpoints/TOP/32,81",
      |        "observationStations": "https://api.weather.gov/gridpoints/TOP/32,81/stations",
      |        "relativeLocation": {
      |            "type": "Feature",
      |            "geometry": {
      |                "type": "Point",
      |                "coordinates": [
      |                    -97.086661000000007,
      |                    39.679375999999998
      |                ]
      |            },
      |            "properties": {
      |                "city": "Linn",
      |                "state": "KS",
      |                "distance": {
      |                    "unitCode": "wmoUnit:m",
      |                    "value": 7366.9851976443997
      |                },
      |                "bearing": {
      |                    "unitCode": "wmoUnit:degree_(angle)",
      |                    "value": 358
      |                }
      |            }
      |        },
      |        "forecastZone": "https://api.weather.gov/zones/forecast/KSZ009",
      |        "county": "https://api.weather.gov/zones/county/KSC201",
      |        "fireWeatherZone": "https://api.weather.gov/zones/fire/KSZ009",
      |        "timeZone": "America/Chicago",
      |        "radarStation": "KTWX"
      |    }
      |}""".stripMargin.trim

  val pointForecastJsonS = parse(pointForecastJsonString).toOption.get
}