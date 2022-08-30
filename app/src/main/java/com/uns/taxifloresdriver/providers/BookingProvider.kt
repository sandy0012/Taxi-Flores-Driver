package com.uns.taxifloresdriver.providers

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uns.taxifloresdriver.models.Booking

const val STATUS = "status"

class BookingProvider {
    val db = Firebase.firestore.collection("Bookings")
    val authProvider = AuthProvider()

    fun create(booking: Booking): Task<Void>{
        return db.document(authProvider.getId()).set(booking).addOnFailureListener{
            Log.d("FIRESTORE", "Error: ${it.message}")
        }
    }

    fun getBooking(): Query {
        return db.whereEqualTo("idDriver",authProvider.getId())
    }
    fun updateStatus(idClient: String, status: String): Task<Void>{
        return db.document(idClient).update(STATUS, status).addOnFailureListener{
            Log.d("FIRESTORE", "Error: ${it.message}")
        }
    }
}