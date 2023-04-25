package com.innowise
package controller

import service.ApiService
import util.JsonSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.innowise.dto.{CountryCaseInfo, CountryDto, MinMaxCaseDto}
import spray.json.enrichAny

import java.time.LocalDateTime
import scala.concurrent.Await
import scala.concurrent.duration.Duration


class ApiController(apiService: ApiService) extends JsonSupport {
  def getRoute: Route = userRoutes

  private val userRoutes: Route =
    pathPrefix("covid" / "countries") {
      path("period") {
        parameters("from", "to") { (from, to) =>
          post {
            entity(as[List[String]]) { countries =>
              //TODO validate
              val result = apiService.getMinMaxCasesByCountryPerTimePeriod(countries, from, to)
             // val o1 = new CountryCaseInfo("asf", 2L, LocalDateTime.now())
              //val o2 = new CountryCaseInfo("asf", 2L, LocalDateTime.now())
             // val result = MinMaxCaseDto(List(o1, o2), List(o1, o2))
              complete(HttpEntity(ContentTypes.`application/json`, result.toJson.toString))
            }
          }
        }
      } ~
      //TODO more routes
        path("aloha") {
          get {
            complete(HttpEntity(ContentTypes.`application/json`, "hello bitch"))
          }
        }
    }
}
