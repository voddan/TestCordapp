package com.luxoft.poc.mobi.data

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class GeoPoint(
    val lat: Float,
    val lng: Float
)

@CordaSerializable
data class GeoLine(
    val points: String
)

@CordaSerializable
data class Step(
    val from: GeoPoint,
    val to: GeoPoint,

    val description: String,
    val transportDetails: TransportDetails
) {
    @CordaSerializable
    data class TransportDetails(
        val type: TransportType,
        val descriptor: String,

        val price: Int,
        val distance: Int,
        val duration: Int,

        val description: String,
        val geoline: GeoLine
    )

    @CordaSerializable
    enum class TransportType {
        CAR, BIKE, TAXI, PLANE, SHIP, FERRY, RAIL
    }
}