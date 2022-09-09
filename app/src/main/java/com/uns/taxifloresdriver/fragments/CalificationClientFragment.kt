package com.uns.taxifloresdriver.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.databinding.FragmentCalificationClientBinding
import com.uns.taxifloresdriver.models.History
import com.uns.taxifloresdriver.providers.HistoryProvider

class CalificationClientFragment : Fragment() {
    private var history: History? = null
    private var _binding: FragmentCalificationClientBinding? = null
    private val binding get() = _binding!!
    private var historyProvider = HistoryProvider()

    private var extraPrice = 0.0
    private var calification = 0f


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
            binding.textViewPrice.text = "Precio: ${format(extraPrice)}"


            binding.btnCalfication.setOnClickListener{
                if(history?.id != null){
                    calification = binding.ratinBar.numStars.toFloat()
                    updateCalificationClient(history?.id!!)
                }else{
                    Toast.makeText(context, "el id del historial es nulo", Toast.LENGTH_SHORT).show()
                }

            }
            binding.ratinBar.setOnRatingBarChangeListener { ratingBar, value, b ->
                calification = value
            }
            getHistory()
        }
    }

    private fun updateCalificationClient(id: String){
        historyProvider.updateCalificationToClient(id, calification).addOnCompleteListener{
            if (it.isSuccessful){
                gotoMap()
            }else{
                Toast.makeText(context, "Error al actualziar calificacion", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun gotoMap(){
        findNavController().navigate(R.id.action_calificationClientFragment_to_map)

        //val fragmentManager: FragmentManager = requireFragmentManager()
        //this will clear the back stack and displays no animation on the screen
        //this will clear the back stack and displays no animation on the screen
        //fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun getHistory(){
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->
            if (query != null){
                if (query.documents.size > 0){
                    history = query.documents[0].toObject(History::class.java)
                    history?.id = query.documents[0].id

                    binding.textViewOrigin.text = history?.origin
                    binding.textViewDestination.text = history?.destination
                    binding.textViewTimeAndDistance.text = "${history?.time} Min - ${format(history?.km!!)}"

                    Log.d("FIRESTORE","HISTORIAL: $history")
                }

            }
        }
    }


    private fun format(value : Double): String{
        return String.format("%.1f",value)
    }

}