package com.example.simpleweatherapp

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run = SimpleweatherappServer.run[IO]
}
