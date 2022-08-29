package com.uns.taxifloresdriver.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.models.Booking

class ModalBottomSheetBooking: BottomSheetDialogFragment() {

    private lateinit var textViewOrigin: TextView
    private lateinit var textViewDestination: TextView
    private lateinit var textViewTimeAndDistance: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnCancel: Button

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
        val booking = Booking.fromJson(data!!)
        Log.d("ARGUMENTS", "booking: ${booking?.toJson()}")

        textViewOrigin.text = booking?.origin
        textViewDestination.text = booking?.destination
        textViewTimeAndDistance.text = "${booking?.time} Min - ${booking?.km} Km"
        return view
    }

    companion object{
        const val TAG = "ModalBottomSheet"
    }
}