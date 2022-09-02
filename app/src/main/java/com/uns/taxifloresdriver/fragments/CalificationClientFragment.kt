package com.uns.taxifloresdriver.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.databinding.FragmentCalificationClientBinding
import com.uns.taxifloresdriver.databinding.FragmentLoginBinding
import com.uns.taxifloresdriver.models.History
import com.uns.taxifloresdriver.providers.HistoryProvider

class CalificationClientFragment : Fragment() {
    private var _binding: FragmentCalificationClientBinding? = null
    private val binding get() = _binding!!
    private var historyProvider = HistoryProvider()

    private var extraPrice = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalificationClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments.let { bundle ->
            extraPrice = bundle!!.getDouble("price", 0.0)
            binding.textViewPrice.text = "Precio: $extraPrice"
            getHistory()
        }
    }

    private fun getHistory(){
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->
            if (query != null){
                if (query.documents.size > 0){
                    val document = query.documents[0].toObject(History::class.java)
                    Log.d("FIRESTORE","HISTORIAL: $document")
                }

            }
        }
    }

}