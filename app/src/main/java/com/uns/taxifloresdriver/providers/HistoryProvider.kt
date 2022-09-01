package com.uns.taxifloresdriver.providers

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uns.taxifloresdriver.models.Booking
import com.uns.taxifloresdriver.models.History


class HistoryProvider {
    val db = Firebase.firestore.collection("Histories")
    val authProvider = AuthProvider()

    fun create(history: History): Task<DocumentReference> {
        return db.add(history).addOnFailureListener{
            Log.d("FIRESTORE", "Error: ${it.message}")
        }
    }

    fun getBooking(): Query {
        return db.whereEqualTo("idDriver",authProvider.getId())
    }
    fun updateStatus(idClient: String, status: String): Task<Void>{
        return db.document(idClient).update("status", status).addOnFailureListener{
            Log.d("FIRESTORE", "Error: ${it.message}")
        }
    }
}