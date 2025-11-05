package com.example.list_wise.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.list_wise.databinding.FragmentHistoricBinding
import com.example.list_wise.ui.lists.ViewModelFactory

class HistoricFragment : Fragment() {

    private var _binding: FragmentHistoricBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HistoricViewModel
    private lateinit var adapter: HistoricListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoricBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelFactory(requireContext())).get(HistoricViewModel::class.java)

        adapter = HistoricListAdapter()
        binding.recyclerViewHistoric.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistoric.adapter = adapter

        viewModel.gastos.observe(viewLifecycleOwner) {
            binding.txtMonthlyExpense.text = "R$ %.2f".format(it)
        }

        viewModel.totalListas.observe(viewLifecycleOwner) {
            binding.txtCompletedListsCount.text = it.toString()
        }

        viewModel.listasFinalizadas.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.itensMaisComprados.observe(viewLifecycleOwner) { itens ->
            if (itens.isEmpty()) {
                binding.txtNoHistory.visibility = View.VISIBLE
            } else {
                binding.txtNoHistory.visibility = View.GONE
                binding.txtMostPurchasedSubtitle.text = itens.joinToString(", ")
            }
        }

        binding.btnCreateList.setOnClickListener {
            // Navegar para tela de criação de lista
        }

        viewModel.carregarDados()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}