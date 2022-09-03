package com.uns.taxifloresdriver.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.databinding.FragmentHistoriesBinding
import com.uns.taxifloresdriver.databinding.FragmentHistoryDetailBinding
import com.uns.taxifloresdriver.models.Client
import com.uns.taxifloresdriver.models.History
import com.uns.taxifloresdriver.providers.ClientProvider
import com.uns.taxifloresdriver.providers.HistoryProvider
import com.uns.taxifloresdriver.utils.RelativeTime

class HistoryDetailFragment : Fragment() {

    private var _binding: FragmentHistoryDetailBinding? = null
    private val binding get() = _binding!!
    private val historyProvider = HistoryProvider()
    private val clientProvider = ClientProvider()
    private var extraId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryDetailBinding.inflate(inflater, container, false)

        arguments.let {
            extraId = it?.getString("id").toString()
            getHistory()
        }
        binding.imageViewBack.setOnClickListener{finish()}

        return binding.root
    }

    private fun getHistory(){
        historyProvider.getHistoryById(extraId).addOnSuccessListener { document ->
            if (document.exists()){
                val history = document.toObject(History::class.java)
                binding.textViewOrigin.text = history?.origin
                binding.textViewDestination.text = history?.destination
                binding.textViewDate.text = RelativeTime.getTimeAgo(history?.timestamp!!)
                binding.textViewPrice.text = "S/.${String.format("%.1f",history.price)}"
                binding.textViewClientCalification.text = "${history.calificationToDriver}"
                binding.textViewClientCalification.text = "${history.calificationToCliente}"
                binding.textViewTimeAndDistance.text = "${history.time} Min - ${String.format("%.1f",history.km)} Km"
                clientInfo(history?.idClient!!)
            }
        }
    }

    private fun clientInfo(id: String){
        clientProvider.getClient(id).addOnSuccessListener{ document ->
            if (document.exists()){
                val client = document.toObject(Client::class.java)

                binding.textViewEmail.text = client?.email
                binding.textViewName.text = "${client?.name} ${client?.lastName}"
                if (client?.image != null){
                    Glide.with(this).load(client?.image).into(binding.circleImageProfile)
                }
            }
        }
    }

    private fun finish(){
        findNavController().navigate(R.id.action_historyDetailFragment_to_historiesFragment)
    }

}