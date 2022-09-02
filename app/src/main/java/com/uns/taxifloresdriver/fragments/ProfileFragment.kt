package com.uns.taxifloresdriver.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.databinding.FragmentProfileBinding
import com.uns.taxifloresdriver.models.Driver
import com.uns.taxifloresdriver.providers.AuthProvider
import com.uns.taxifloresdriver.providers.DriverProvider


class ProfileFragment : Fragment() {

    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()
    private val modalMenu = ModalBottomSheetMenu()

    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root

        getDriver()
        binding.imageViewBack.setOnClickListener { showModalMenu()}


    }

    private fun showModalMenu(){
        findNavController().navigate(R.id.action_profileFragment_to_map)
    }

    private fun getDriver(){
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                val driver = document.toObject(Driver::class.java)
                binding.textViewEmail.text = driver?.email
                binding.textFieldName.setText(driver?.name)
                binding.textFieldLastName.setText(driver?.lastName)
                binding.textFieldPhone.setText(driver?.phone)
                binding.textFieldBrandCar.setText(driver?.brandCar)
                binding.textFieldColorCar.setText(driver?.colorCar)
                binding.textFieldPlateCar.setText(driver?.plateNumber)
            }
        }
    }


}