package com.example.list_wise.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.list_wise.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.list_wise.databinding.FragmentHistoricBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.text.NumberFormat

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

        adapter = HistoricListAdapter { lista ->
            // Navegar para o detalhe da lista finalizada (PurchasedListFragment)
            val action =
                HistoricFragmentDirections.actionHistoricFragmentToPurchasedListFragment(lista.id)
            findNavController().navigate(action)
        }

        binding.recyclerViewHistoric.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistoric.adapter = adapter

        viewModel.gastos.observe(viewLifecycleOwner) { valor ->
            binding.txtMonthlyExpense.text =
                getString(R.string.monthly_expense_format, valor)
        }

        viewModel.totalListas.observe(viewLifecycleOwner) { total ->
            val formatted = NumberFormat.getIntegerInstance().format(total)
            binding.txtCompletedListsCount.text = formatted
        }

        viewModel.listasFinalizadas.observe(viewLifecycleOwner) { listas ->
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            val listasOrdenadas = listas.sortedByDescending { lista ->
                val dataStr = lista.dataFinalizacao

                if (dataStr.isNullOrBlank()) {
                    0L  // se não tiver data, joga pro final
                } else {
                    try {
                        sdf.parse(dataStr)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }
            }

            adapter.submitList(listasOrdenadas)
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
            /// Navegar direto para o Dashboard
            findNavController().navigate(R.id.dashboardFragment)
        }

        viewModel.carregarDados()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
