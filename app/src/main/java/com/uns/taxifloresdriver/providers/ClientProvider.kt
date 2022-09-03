package com.uns.taxifloresdriver.providers

import com.uns.taxifloresdriver.models.Client
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ClientProvider {
    val db = Firebase.firestore.collection("Clients")

    fun create(client: Client): Task<Void> {
    return db.document(client.id!!).set(client)
    }
    fun getClient(id: String): Task<DocumentSnapshot> {
        return db.document(id).get()
    }
}