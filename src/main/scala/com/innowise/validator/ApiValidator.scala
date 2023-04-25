package com.innowise.validator

import com.innowise.service.ApiService

import java.time.{LocalDate, LocalDateTime}

class ApiValidator(apiService: ApiService) {
  private val RECORD_START_DATE: LocalDate = LocalDate.of(2020, 1, 22)

  def validateDateParam(paramArray: String*): Boolean = {
    val list = paramArray.map(element => {
      val date: LocalDate = LocalDate.parse(element)
      date.isAfter(RECORD_START_DATE) && date.isBefore(LocalDate.now())
    })
    list.forall(_ == true)
  }

  def validateCountryRequestBody(countries: List[String]): Boolean = {
    countries.map(country =>
      apiService.checkCountrySlug(country)
    )
      .forall(_ == true)
  }
}
