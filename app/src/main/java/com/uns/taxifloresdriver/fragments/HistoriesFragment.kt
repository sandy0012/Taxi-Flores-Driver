package com.uns.taxifloresdriver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.adapters.HistoriesAdapter
import com.uns.taxifloresdriver.databinding.FragmentHistoriesBinding
import com.uns.taxifloresdriver.models.History
import com.uns.taxifloresdriver.providers.HistoryProvider


class HistoriesFragment : Fragment() {

    private var _binding: FragmentHistoriesBinding? = null
    private val binding get() = _binding!!
    private var historyProvider = HistoryProvider()
    private var histories = ArrayList<History>()
    private lateinit var adapter : HistoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoriesBinding.inflate(inflater, container, false)
        var linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerViewHistories.layoutManager = linearLayoutManager


        getHistories()
        return binding.root
    }

    private fun getHistories(){
        histories.clear()

        historyProvider.getHistories().get().addOnSuccessListener { query ->
            if (query != null){
                if (query.documents.size > 0){
                    val documents = query.documents

                    for (d in documents){
                        var history = d.toObject(History::class.java)
                        history?.id = d.id
                        histories.add(history!!)
                    }

                    adapter = HistoriesAdapter(this@HistoriesFragment,histories)
                    binding.recyclerViewHistories.adapter = adapter
                }
            }
        }
    }

}