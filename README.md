# simple-weather-app

Simple Weather App

## Technical stack
- cats and cats-effect: basic functional blocks as well as concurrency and functional effects
- circe: JSON serialization library
- http4s: functional HTTP server and client built on top of fs2
- log4cats: standard logging framework for Cats

## Running And Testing Locally
cd simple-weather-app
sbt run

### Api endpoint
curl http://localhost:8080/weather/lat/39.7456/lon/-97.0892

##
This is just a simple one route service that queries the National Weather Service API(https://www.weather.gov/documentation/services-web-api)
and returns the weather for the day short forecast(eg Cloudy, Sunny etc) and a characterization of the temperature(hot, cold or moderate)

Its just a howto and not what one would consider production code as its missing the following
- Tests
- Error handling at the route level to return ineligible responses to clients
- Logging
- Config Service
- Build process
- API documentation eg using Swagger