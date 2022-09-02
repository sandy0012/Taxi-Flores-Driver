package com.uns.taxifloresdriver.models

import com.beust.klaxon.*
import com.google.android.gms.maps.model.LatLng

private val klaxon = Klaxon()

data class History(
    val id: String? = null,
    val idClient: String? = null,
    val idDriver: String? = null,
    val origin: String? = null,
    val destination: String? = null,
    val calificationToCliente: Double? = null,
    val calificationToDriver: Double? = null,
    val time: Int? = null,
    val km: Double? = null,
    val originLat: Double? = null,
    val originLng: Double? = null,
    val destinationLat: Double? = null,
    val destinationLng: Double? = null,
    val price: Double? = null,
    val timestamp: Long? = null,
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<History>(json)
    }
}