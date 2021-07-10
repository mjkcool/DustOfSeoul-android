package com.mjkcool.dustofseoul.kakaodata

data class Coord2regioncode(
    val documents: List<Document>,
    val meta: Meta
)

data class Meta(
    val total_count: Int
)

data class Document(
    val address_name: String,
    val code: String,
    val region_1depth_name: String,
    val region_2depth_name: String,
    val region_3depth_name: String,
    val region_4depth_name: String,
    val region_type: String,
    val x: Double,
    val y: Double
)