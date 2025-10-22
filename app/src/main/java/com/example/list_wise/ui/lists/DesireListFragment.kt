package com.example.list_wise.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.list_wise.R
import com.example.list_wise.ui.lists.Category
import com.example.list_wise.ui.lists.CategoryAdapter
import com.example.list_wise.ui.lists.Item
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class DesireListFragment : Fragment() {

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnFinalize: ExtendedFloatingActionButton
    private lateinit var txtListName: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtListName = view.findViewById(R.id.textListName)
        recyclerView = view.findViewById(R.id.recyclerViewCategories)
        btnFinalize = view.findViewById(R.id.btnFinalize)

        txtListName.text = getString(R.string.title_desired_lists)
        btnFinalize.visibility = View.VISIBLE

        categoryAdapter = CategoryAdapter(
            onAddItem = { category -> /* abrir diálogo para adicionar item */ },
            onEditItem = { item -> /* abrir diálogo para editar item */ },
            onDeleteItem = { item -> /* excluir item */ }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = categoryAdapter

        // Simulação de dados
        val categoriasDesejadas = getMockedDesiredCategories()
        categoryAdapter.submitList(categoriasDesejadas)

        btnFinalize.setOnClickListener {
            Toast.makeText(requireContext(), "Finalizando compra...", Toast.LENGTH_SHORT).show()
            // Aqui você pode mover os itens selecionados para o histórico
        }
    }

    private fun getMockedDesiredCategories(): List<Category> {
        // Simulação de dados
        return listOf(
            Category("Frutas", listOf(Item("Banana", "Prata", 6, 5.0)).toMutableList()),
            Category("Limpeza", listOf(Item("Detergente", "Ypê", 2, 3.5)).toMutableList())
        )
    }
}