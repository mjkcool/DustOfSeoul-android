package com.mjkcool.dustofseoul.dataseouldata

data class TimeAverageAirQuality(
    val TimeAverageAirQuality: TimeAverageAirQualityX
)

data class TimeAverageAirQualityX(
    val RESULT: RESULT,
    val list_total_count: Int,
    val row: List<Row>
)

data class RESULT(
    val CODE: String,
    val MESSAGE: String
)

data class Row(
    val CO: Double,
    val MSRDT: String,
    val MSRSTE_NM: String,
    val NO2: Double,
    val O3: Double,
    val PM10: Int,
    val PM25: Int,
    val SO2: Double
)