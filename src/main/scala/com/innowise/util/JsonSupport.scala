package com.innowise
package util

import dto.{CountryCaseDto, CountryDto, MinMaxCaseDto}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat, RootJsonWriter}

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val countryFormat: RootJsonFormat[CountryDto] = jsonFormat3(CountryDto.apply)

  implicit val countryCaseInfoFormat: RootJsonFormat[CountryCaseDto] = jsonFormat3(CountryCaseDto.apply)
  implicit val minMaxCaseFormat: RootJsonFormat[MinMaxCaseDto] = jsonFormat2(MinMaxCaseDto.apply)

  implicit object LocalDateTimeJsonFormat extends RootJsonFormat[LocalDateTime] {
    def write(instant: LocalDateTime): JsValue = JsString(DateTimeFormatter.ISO_DATE_TIME.format(instant))

    def read(json: JsValue): LocalDateTime = json match {
      case JsString(s) => LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME)
      case _ => throw DeserializationException("Expected string")
    }
  }

  implicit object LocalDateJsonFormat extends RootJsonFormat[LocalDate] {
    def write(instant: LocalDate): JsValue = JsString(DateTimeFormatter.ISO_DATE.format(instant))

    def read(json: JsValue): LocalDate = json match {
      case JsString(s) => LocalDate.parse(s, DateTimeFormatter.ISO_DATE)
      case _ => throw DeserializationException("Expected string")
    }
  }
}
