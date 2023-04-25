package com.innowise.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
case class CountryJsonReadDto(Country: String, Cases: Int, Date: LocalDateTime, Province: String)
