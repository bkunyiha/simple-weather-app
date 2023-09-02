package com.example.simpleweatherapp.services

object ForecastOps {
  object cold {
    def unapply(temp: Int): Boolean = temp <= 62
  }

  object moderate {
    def unapply(temp: Int): Boolean = temp <= 78
  }

  def temperature(temp: Int): String = temp match {
    case cold() => "cold"
    case moderate() => "moderate"
    case _ => "hot"
  }

}
