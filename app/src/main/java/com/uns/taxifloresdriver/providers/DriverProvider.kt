package com.uns.taxifloresdriver.providers

import com.uns.taxifloresdriver.models.Client
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uns.taxifloresdriver.models.Driver

class DriverProvider {
    val db = Firebase.firestore.collection("Drivers")

    fun create(driver: Driver): Task<Void> {
    return db.document(driver.id!!).set(driver)
    }

    fun getDriver(idDriver: String): Task<DocumentSnapshot> {
        return db.document(idDriver).get()
    }

    fun update(driver: Driver): Task<Void>{
        val map : MutableMap<String,Any> = HashMap()
        map["name"] = driver.name!!
        map["lastName"] = driver.lastName!!
        map["phone"] = driver.phone!!
        map["colorCar"] = driver.colorCar!!
        map["brandCar"] = driver.brandCar!!
        map["plateNumber"] = driver.plateNumber!!

        return db.document(driver.id!!).update(map)
    }
}