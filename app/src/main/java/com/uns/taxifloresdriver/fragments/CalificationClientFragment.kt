package com.uns.taxifloresdriver.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.databinding.FragmentCalificationClientBinding
import com.uns.taxifloresdriver.databinding.FragmentLoginBinding

class CalificationClientFragment : Fragment() {
    private var _binding: FragmentCalificationClientBinding? = null
    private val binding get() = _binding!!

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
        }
    }

}