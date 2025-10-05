package com.example.list_wise.ui.lists

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.list_wise.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListFragment : Fragment() {

    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewCategories)
        val btnFinalize = view.findViewById<FloatingActionButton>(R.id.btnFinalize)

        // Categorias iniciais (pode ajustar conforme o protótipo)
        categories.addAll(
            listOf(
                Category("Mercearia", mutableListOf()),
                Category("Frutas e Legumes", mutableListOf()),
                Category("Frios e Congelados", mutableListOf()),
                Category("Limpeza e Higiene", mutableListOf()),
                Category("Temperos", mutableListOf()),
                Category("Bebidas", mutableListOf()),
                Category("Utensílios", mutableListOf())
            )
        )

        categoryAdapter = CategoryAdapter(
            categories,
            onAddItem = { category -> showAddDialog(category) },
            onEditItem = { item -> showEditDialog(item) },
            onDeleteItem = { item -> deleteItem(item) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = categoryAdapter

        btnFinalize.setOnClickListener {
            Toast.makeText(requireContext(), "Lista finalizada!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddDialog(category: Category) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val nome = dialogView.findViewById<EditText>(R.id.editItemName)
        val marca = dialogView.findViewById<EditText>(R.id.editItemBrand)
        val quantidade = dialogView.findViewById<EditText>(R.id.editItemQuantity)
        val preco = dialogView.findViewById<EditText>(R.id.editItemPrice)

        AlertDialog.Builder(requireContext())
            .setTitle("Novo Item em ${category.name}")
            .setView(dialogView)
            .setPositiveButton("Adicionar") { _, _ ->
                val item = Item(
                    nome.text.toString(),
                    marca.text.toString(),
                    quantidade.text.toString().toIntOrNull() ?: 1,
                    preco.text.toString().toDoubleOrNull() ?: 0.0
                )
                category.items.add(item)
                categoryAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(item: Item) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val nome = dialogView.findViewById<EditText>(R.id.editItemName)
        val marca = dialogView.findViewById<EditText>(R.id.editItemBrand)
        val quantidade = dialogView.findViewById<EditText>(R.id.editItemQuantity)
        val preco = dialogView.findViewById<EditText>(R.id.editItemPrice)

        nome.setText(item.nome)
        marca.setText(item.marca)
        quantidade.setText(item.quantidade.toString())
        preco.setText(item.preco.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Item")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                item.nome = nome.text.toString()
                item.marca = marca.text.toString()
                item.quantidade = quantidade.text.toString().toIntOrNull() ?: 1
                item.preco = preco.text.toString().toDoubleOrNull() ?: 0.0
                categoryAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteItem(item: Item) {
        for (category in categories) {
            if (category.items.remove(item)) {
                categoryAdapter.notifyDataSetChanged()
                break
            }
        }
    }
}