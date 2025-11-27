package kr.co.fitview.api.app.domain.member.dto

data class BoundingBox(
    val minLat : Double,
    val maxLat : Double,
    val minLng : Double,
    val maxLng : Double
)