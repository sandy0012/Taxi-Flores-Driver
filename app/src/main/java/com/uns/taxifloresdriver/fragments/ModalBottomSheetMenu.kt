package com.uns.taxifloresdriver.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.models.Booking
import com.uns.taxifloresdriver.models.Driver
import com.uns.taxifloresdriver.providers.AuthProvider
import com.uns.taxifloresdriver.providers.BookingProvider
import com.uns.taxifloresdriver.providers.DriverProvider
import com.uns.taxifloresdriver.providers.GeoProvider

class ModalBottomSheetMenu: BottomSheetDialogFragment() {

    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()

    var textViewUserName : TextView? = null
    var linearLayoutLogout : LinearLayout? = null
    var linearLayoutProfile : LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_menu,container,false)
        textViewUserName = view.findViewById(R.id.textViewUserName)
        linearLayoutLogout = view.findViewById(R.id.linearLayoutLogout)
        linearLayoutProfile = view.findViewById(R.id.linearLayoutProfile)

        getDriver()
        linearLayoutLogout?.setOnClickListener{goToMain()}
        linearLayoutProfile?.setOnClickListener { goMapToProfile() }

        return view
    }


    private fun goProfileToMap(){
        findNavController().navigate(R.id.action_profileFragment_to_map)
    }

    private fun goMapToProfile(){
        findNavController().navigate(R.id.action_map_to_profileFragment)
    }

    private fun goToMain(){
        authProvider.logout()
        findNavController().navigate(R.id.action_map_to_login)

    }

    public fun goProfileToLogin(){
        authProvider.logout()
        findNavController().navigate(R.id.action_profileFragment_to_login)
    }

    private fun getDriver(){
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                val driver = document.toObject(Driver::class.java)
                textViewUserName?.text = "${driver?.name} ${driver?.lastName}"
            }
        }
    }

    companion object{
        const val TAG = "ModalBottomSheetMenu"
    }



}