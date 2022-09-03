package com.uns.taxifloresdriver.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.databinding.FragmentProfileBinding
import com.uns.taxifloresdriver.models.Driver
import com.uns.taxifloresdriver.providers.AuthProvider
import com.uns.taxifloresdriver.providers.DriverProvider
import java.io.File


class ProfileFragment : Fragment() {

    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()
    private val modalMenu = ModalBottomSheetMenu()

    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private var imageFile : File? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        getDriver()
        binding.imageViewBack.setOnClickListener { showModalMenu()}
        binding.btnUpdate.setOnClickListener { updateInfo()}
        binding.circleImageProfile.setOnClickListener { selectImage() }

        return binding.root
    }

    private fun showModalMenu(){
        findNavController().navigate(R.id.action_profileFragment_to_map)
    }

    private fun updateInfo(){
        val name = binding.textFieldName.text.toString()
        val lastName = binding.textFieldLastName.text.toString()
        val phone = binding.textFieldPhone.text.toString()
        val carBrand = binding.textFieldBrandCar.text.toString()
        val carColor =  binding.textFieldColorCar.text.toString()
        val carPlate = binding.textFieldPlateCar.text.toString()

        val driver = Driver(
            id = authProvider.getId(),
            name = name,
            lastName = lastName,
            phone = phone,
            colorCar = carColor,
            brandCar = carBrand,
            plateNumber = carPlate
        )

        if (imageFile != null){
            driverProvider.uploadImage(authProvider.getId(),imageFile!!).addOnSuccessListener{ taskSnapshot ->
                driverProvider.getImageUrl().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    driver.image = imageUrl
                    update(driver)
                    Log.d("STORAGE", "URL: ${imageUrl}")
                }
            }
        }else{
            update(driver)
        }

    }

    private fun update(driver: Driver){
        driverProvider.update(driver).addOnCompleteListener{
            if (it.isSuccessful){
                Toast.makeText(context, "Datos Actualizados", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, "No se pudo actualizar", Toast.LENGTH_SHORT).show()
            }
        }
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

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result : ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path)
            binding.circleImageProfile.setImageURI(fileUri)
        }
        else if(resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }
        else{
            Toast.makeText(context,"Tarea Cancelada", Toast.LENGTH_LONG).show()
        }
    }

    private fun selectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                    startImageForResult.launch(intent)
            }
    }


}