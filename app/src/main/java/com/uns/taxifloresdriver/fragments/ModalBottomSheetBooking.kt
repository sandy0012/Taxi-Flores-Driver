package com.uns.taxifloresdriver.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.models.Booking
import com.uns.taxifloresdriver.providers.AuthProvider
import com.uns.taxifloresdriver.providers.BookingProvider
import com.uns.taxifloresdriver.providers.GeoProvider

class ModalBottomSheetBooking: BottomSheetDialogFragment() {

    private lateinit var textViewOrigin: TextView
    private lateinit var textViewDestination: TextView
    private lateinit var textViewTimeAndDistance: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnCancel: Button

    private val bookingProvider = BookingProvider()
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private lateinit var booking: Booking

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_booking,container,false)


        textViewOrigin = view.findViewById(R.id.textViewOrigin)
        textViewDestination = view.findViewById(R.id.textViewDestination)
        textViewTimeAndDistance = view.findViewById(R.id.textViewTimeAndDistance)
        btnAccept = view.findViewById(R.id.bntAccept)
        btnCancel= view.findViewById(R.id.bntCancel)

        val data = arguments?.getString("booking")
        booking = Booking.fromJson(data!!)!!
        Log.d("ARGUMENTS", "booking: ${booking?.toJson()}")

        textViewOrigin.text = booking?.origin
        textViewDestination.text = booking?.destination
        textViewTimeAndDistance.text = "${booking?.time} Min - ${booking?.km} Km"

        btnAccept.setOnClickListener{acceptBooking(booking?.idClient!!)}
        btnCancel.setOnClickListener{cancelBooking(booking?.idClient!!)}

        return view
    }

    private fun cancelBooking(idClient: String){

        bookingProvider.updateStatus(idClient,"cancel").addOnCompleteListener{
            MapFragment()?.timer?.cancel()
            dismiss()
        }
    }

    private fun acceptBooking(idClient: String){
        bookingProvider.updateStatus(idClient,"accept").addOnCompleteListener{
            MapFragment()?.timer?.cancel()
            if (it.isSuccessful){
                MapFragment().easyWayLocation?.endUpdates()
                geoProvider.removeLocation(authProvider.getId())
                goToMapTrip()

            }else{
                if (context != null) {
                    Toast.makeText(context, "no de pudo cancelar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun goToMapTrip(){
        findNavController().navigate(R.id.action_map_to_mapTripFragment)
    }

    companion object{
        const val TAG = "ModalBottomSheet"
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        MapFragment()?.timer?.cancel()
        /*if (booking.id != null){
            cancelBooking(booking.idClient!!)
        }*/
    }
}