import akka.actor.ActorSystem
import akka.http.scaladsl.model.*
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.stream.{ActorMaterializer, Materializer}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.*
import play.api.libs.json.Json
import jdk.internal.org.jline.utils.ShutdownHooks.Task

import java.util
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.stream.Collectors
import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.language.postfixOps
import scala.util.control.NonLocalReturns.*
import scala.util.{Failure, Success}

object CovidApiApp {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val casesByCountryRequest = "https://api.covid19api.com/total/country/%s/status/confirmed"
  val allCountriesRequest = "https://api.covid19api.com/countries"
  var COUNTRIES_NAMES_LIST: List[Country] = List.empty
  var COUNTRIES_INFO_LIST: List[CountryCasesInfo] = List.empty

  val objectMapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)
  var countryToCasesMap: mutable.Map[String, List[CountryCasesInfo]] = scala.collection.mutable.Map[String, List[CountryCasesInfo]]()

  case class Country(Country: String, Slug: String, ISO2: String)

  case class CountryCasesInfo(Country: String, CountryCode: String,
                              Province: String,
                              City: String,
                              CityCode: String,
                              Lat: String,
                              Lon: String,
                              Cases: Long,
                              Status: String,
                              Date: String)

  def main(args: Array[String]): Unit = {

    val COUNTRIES = List("France", "Italy")

    val countriesJson = sendHttpRequest(allCountriesRequest)

    COUNTRIES_NAMES_LIST = objectMapper.readValue(countriesJson, classOf[Array[Country]]).toList

    findMaxMinByCountriesPerDay(COUNTRIES)

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

  def findMaxMinByCountriesPerDay(countries: List[String]): Unit = {

    countries.foreach(country => {
      if (!countryToCasesMap.contains(country)) {
        val countryOption = COUNTRIES_NAMES_LIST.find(data => {
          data.Country.equals(country)
        })
        countryOption match {
          case Some(data) =>
            val casesByCountryUri = String.format(casesByCountryRequest, data.Slug)

            val json = sendHttpRequest(casesByCountryUri)
            val confirmedCasesByCountryDate = objectMapper.readValue(json, classOf[Array[CountryCasesInfo]]).toList

            countryToCasesMap += (confirmedCasesByCountryDate.head.Country -> confirmedCasesByCountryDate)
          case None => throw new NoSuchElementException()
        }
      }
    })
  }
}
