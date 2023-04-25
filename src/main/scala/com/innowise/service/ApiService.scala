package com.innowise
package service

import dto.{CountryCaseDto, CountryDto, CountryJsonReadDto, MinMaxCaseDto}
import util.ApiConstant
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
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.Duration

class ApiService {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val objectMapper: ObjectMapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
    .registerModule(new JavaTimeModule)
  private val COUNTRIES_NAMES_LIST: List[CountryDto] = fetchAllCountries()

  def getMinMaxCasesByCountryPerTimePeriod(countries: List[String],
                                           startDate: String,
                                           endDate: String): MinMaxCaseDto = {
    var minList = new ListBuffer[CountryCaseDto]
    var maxList = new ListBuffer[CountryCaseDto]

    val startDay: LocalDateTime = LocalDate.parse(startDate).atStartOfDay()
    val endDay: LocalDateTime = LocalDate.parse(endDate).atStartOfDay()

    countries.foreach(country => {
      val uri = String.format(ApiConstant.CASES_BY_COUNTRY_REQUEST_URI, country, startDay, endDay)
      val result = sendHttpRequest(uri)

      val confirmedCasesByCountryDate = objectMapper.readValue(result, classOf[Array[CountryJsonReadDto]])
        .toList
        .filter(element => element.Province.equals(""))

      var min = Int.MaxValue
      var max = Int.MinValue

      for (index <- 1 until confirmedCasesByCountryDate.length) {
        val newCasesCount = confirmedCasesByCountryDate(index).Cases - confirmedCasesByCountryDate(index - 1).Cases
        if (newCasesCount <= min) {
          min = newCasesCount
          minList += CountryCaseDto(confirmedCasesByCountryDate(index).Country,
            newCasesCount,
            confirmedCasesByCountryDate(index).Date)
        }
        if (newCasesCount >= max) {
          max = newCasesCount
          maxList += CountryCaseDto(confirmedCasesByCountryDate(index).Country,
            newCasesCount,
            confirmedCasesByCountryDate(index).Date)
        }
      }
      val minCases = minList.map(element => element.Cases).min
      val maxCases = maxList.map(element => element.Cases).max

      minList = minList.filter(element => element.Cases == minCases)
      maxList = maxList.filter(element => element.Cases == maxCases)
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

  def checkCountrySlug(slug: String): Boolean = {
    COUNTRIES_NAMES_LIST.exists(element => element.Slug.equals(slug))
  }

  def fetchAllCountries(): List[CountryDto] = {
    val countriesJson = sendHttpRequest(ApiConstant.ALL_COUNTRIES_URI)

    objectMapper.readValue(countriesJson, classOf[Array[CountryDto]]).toList
  }

}
