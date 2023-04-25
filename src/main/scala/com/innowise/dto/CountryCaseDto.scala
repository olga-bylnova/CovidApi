package com.innowise
package dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.time.LocalDateTime

case class CountryCaseDto(Country: String, Cases: Int, Date: LocalDateTime)
