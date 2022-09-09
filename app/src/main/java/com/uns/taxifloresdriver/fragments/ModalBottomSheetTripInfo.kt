package com.uns.taxifloresdriver.fragments

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.models.Booking
import com.uns.taxifloresdriver.models.Client
import com.uns.taxifloresdriver.providers.*
import de.hdodenhof.circleimageview.CircleImageView

class ModalBottomSheetTripInfo: BottomSheetDialogFragment() {

    private var client: Client? = null
    private lateinit var booking: Booking
    val clientProvider = ClientProvider()
    val authProvider = AuthProvider()
    var textViewClientName : TextView? = null
    var textViewOrigin : TextView? = null
    var textViewDestination : TextView? = null
    var circleImageClient : CircleImageView? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_trip_info,container,false)

        textViewClientName = view.findViewById(R.id.textViewClientName)
        textViewOrigin = view.findViewById(R.id.textViewOrigin)
        textViewDestination = view.findViewById(R.id.textViewDestination)
        circleImageClient = view.findViewById(R.id.circleImageClient)


        arguments.let { data ->
            booking = Booking.fromJson(data?.getString("booking")!!)!!
            textViewOrigin?.text = booking.origin
            textViewDestination?.text = booking.destination
        }


       getDriver()

        return view
    }



    private fun getDriver(){
        clientProvider.getClient(booking.idClient!!).addOnSuccessListener { document ->
            if (document.exists()){
                client = document.toObject(Client::class.java)
                textViewClientName?.text = "${client?.name} ${client?.lastName}"

                if (client?.image != null){
                    if (client?.image != ""){
                        Glide.with(requireContext()).load(client?.image).into(circleImageClient!!)
                    }
                }

                //textViewUserName?.text = "${driver?.name} ${driver?.lastName}"
            }
        }
    }

    companion object{
        const val TAG = "ModalBottomSheetTripInfo"
    }



}