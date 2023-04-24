package com.innowise
package service

import dto.{CountryCaseInfo, CountryInfo, MinMaxCaseDto}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import play.api.libs.json.Json

import scala.collection.mutable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.Duration

class ApiService {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val objectMapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)
  val casesByCountryRequest = "https://api.covid19api.com/country/%s/status/confirmed?from=%s&to=%s"

  def getMinMaxCasesByCountryPerTimePeriod(countries: List[String],
                                           startDate: String,
                                           endDate: String): MinMaxCaseDto = {
    objectMapper.registerModule(new JavaTimeModule)

    val minList = new ListBuffer[CountryCaseInfo]
    val maxList = new ListBuffer[CountryCaseInfo]
    countries.foreach(country => {

      //val startDay: LocalDateTime = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME)
      //val endDay: LocalDateTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME)

      val uri = String.format(casesByCountryRequest, country, "2020-02-19", "2020-02-21")
      val result = sendHttpRequest(uri)

      val confirmedCasesByCountryDate = objectMapper.readValue(result, classOf[Array[CountryInfo]]).toList

      val minCases = confirmedCasesByCountryDate.map(element => element.Cases).min
      val maxCases = confirmedCasesByCountryDate.map(element => element.Cases).max

      val elementMin = confirmedCasesByCountryDate
        .find(element => element.Cases == minCases)
        .get
      val elementMax = confirmedCasesByCountryDate
        .find(element => element.Cases == maxCases)
        .get

      minList += CountryCaseInfo(elementMin.Country, elementMin.Cases, elementMin.Date)
      maxList += CountryCaseInfo(elementMax.Country, elementMax.Cases, elementMax.Date)
    }
    )
    MinMaxCaseDto(minList.toList, maxList.toList)
  }

  def sendHttpRequest(_uri: String): String = {
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = _uri
    )
    val result = Http().singleRequest(request)
      .flatMap { res =>
        Unmarshal(res).to[String].map { data =>
          Json.parse(data)
        }
      }
    val futureJson = result.map(json => json.toString)
    Await.result(futureJson, Duration.Inf)
  }

}
