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

class PurchasedListFragment : Fragment() {

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var recyclerView: RecyclerView
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
        val btnFinalize = view.findViewById<ExtendedFloatingActionButton>(R.id.btnFinalize)

        txtListName.text = getString(R.string.title_purchased_lists)
        btnFinalize.visibility = View.GONE

        categoryAdapter = CategoryAdapter(
            onAddItem = {}, // histórico não adiciona
            onEditItem = {}, // histórico não edita
            onDeleteItem = {} // histórico não exclui
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = categoryAdapter

        val categoriasCompradas = getMockedPurchasedCategories()
        categoryAdapter.submitList(categoriasCompradas)
    }

    private fun getMockedPurchasedCategories(): List<Category> {
        return listOf(
            Category("Padaria", listOf(Item("Pão francês", null, 10, 8.0)).toMutableList()),
            Category("Bebidas", listOf(Item("Suco de uva", "Integral", 1, 12.0)).toMutableList())
        )
    }
}