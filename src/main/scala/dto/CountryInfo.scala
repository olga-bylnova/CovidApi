package com.innowise
package dto

import java.time.LocalDateTime

case class CountryInfo(Country: String, CountryCode: String,
                       Province: String,
                       City: String,
                       CityCode: String,
                       Lat: String,
                       Lon: String,
                       Cases: Long,
                       Status: String,
                       Date: LocalDateTime)
