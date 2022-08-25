package com.uns.taxifloresdriver.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.databinding.FragmentLoginBinding
import com.uns.taxifloresdriver.providers.AuthProvider

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authProvider = AuthProvider()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener { goToRegister() }
        binding.btnLogin.setOnClickListener { login() }
    }

    private fun login(){
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()

        if(isValidForm(email,password)){
            authProvider.login(email,password).addOnCompleteListener{
                if (it.isSuccessful){
                    goToMap()
                }else{
                    Toast.makeText(context, "Error al ingresar", Toast.LENGTH_SHORT).show()
                    Log.d("FIREBASE","ERROR: ${it.exception.toString()}")
                }
            }
        }
    }
    private  fun goToMap(){
        findNavController().navigate(R.id.action_login_to_map)
    }

    private fun isValidForm(email:String, password:String) : Boolean{
        if(email.isEmpty()){
            Toast.makeText(context, "Ingresar su Correo Electronico", Toast.LENGTH_SHORT).show()
            return false
        }
        if(password.isEmpty()){
            Toast.makeText(context, "Ingresar su Contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun goToRegister(){
        findNavController().navigate(R.id.action_login_to_register)
    }

    override fun onStart() {
        super.onStart()
        if (authProvider.existSession()){
            goToMap()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}