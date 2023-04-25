package com.innowise
package controller

import service.ApiService
import util.JsonSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.innowise.dto.{CountryCaseDto, CountryDto, MinMaxCaseDto}
import com.innowise.validator.ApiValidator
import spray.json.enrichAny

import java.time.LocalDateTime
import scala.concurrent.Await
import scala.concurrent.duration.Duration


class ApiController(apiService: ApiService, apiValidator: ApiValidator) extends JsonSupport {
  def getRoute: Route = userRoutes

  private val userRoutes: Route =
    pathPrefix("covid" / "countries") {
      path("period") {
        parameters("from", "to") { (from, to) =>
          post {
            entity(as[List[String]]) { countries =>
              validate(apiValidator.validateCountryRequestBody(countries),
                "Incorrect country input") {
                validate(apiValidator.validateDateParam(from, to),
                  "Incorrect time period input") {
                  val result = apiService.getMinMaxCasesByCountryPerTimePeriod(countries, from, to)
                  complete(HttpEntity(ContentTypes.`application/json`, result.toJson.toString))
                }
              }
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
