package com.uns.taxifloresdriver.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.models.Booking

class ModalBottomSheetBooking: BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_booking,container,false)
        val data = arguments?.getString("booking")
        val booking = Booking.fromJson(data!!)
        Log.d("ARGUMENTS", "booking: ${booking?.toJson()}")
        return view
    }

    companion object{
        const val TAG = "ModalBottomSheet"
    }
}