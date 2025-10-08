package com.example.list_wise.ui.lists

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.list_wise.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListFragment : Fragment() {

    private val args: ListFragmentArgs by navArgs()
    private lateinit var categoryAdapter: CategoryAdapter
    private val allCategories = mutableListOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById< RecyclerView>(R.id.recyclerViewCategories)
        val btnFinalize = view.findViewById< ExtendedFloatingActionButton>(R.id.btnFinalize)
        val editSearch = view.findViewById<EditText>(R.id.editSearchItem)

        // Título dinâmico
        requireActivity().title = args.listName

        // Categorias iniciais (pode ajustar conforme o protótipo)
        allCategories.addAll(
            listOf(
                Category("Mercearia", mutableListOf(
                    Item("Arroz Branco", "Tio Joao", 1, 0.0),
                    Item("Feijão Preto", "Turquesa", 1, 0.0),
                    Item("Azeite de Oliva Extra Virgem", "Galo", 1, 0.0),
                    Item("Farinha de Mandioca", "Yoki", 1, 0.0),
                    Item("Farinha de Trigo", "Sao Braz", 1, 0.0),
                    Item("Macarrao Fettuccine", "Paganini", 1, 0.0)
                )),
                Category("Frutas e Legumes", mutableListOf(
                    Item("Banana", "Prata", 6, 0.0),
                    Item("Maçã", "Gala", 4, 0.0),
                    Item("Abacaxi", " ", 1, 0.0),
                    Item("Laranja", " ", 10, 0.0),
                    Item("Limao", " ", 6, 0.0),
                    Item("Mamao", " ", 1, 0.0),
                    Item("Maracuja", " ", 0, 0.0),
                    Item("Melao", " ", 0, 0.0),
                    Item("Morango", " ", 1, 0.0),
                    Item("Batata Inglesa", " ", 6, 0.0),
                    Item("Tomate", "Cereja", 6, 0.0),
                    Item("Cebola", " ", 8, 0.0)
                )),
                Category("Frios e Congelados", mutableListOf(
                    Item("Manteiga", "Itacolomy", 1, 0.0),
                    Item("Iogurte", " ", 0, 0.0),
                    Item("Requeijao", " ", 1, 0.0),
                    Item("Queijo Minas", " ", 1, 0.0),
                    Item("Queijo Coalho", " ", 0, 0.0),
                    Item("Presunto", " ", 0, 0.0),
                    Item("Carne Moida", " ", 1, 0.0),
                    Item("Carne Picanha", " ", 1, 0.0),
                    Item("Carne Musculo", " ", 1, 0.0),
                    Item("Carne Suina", " ", 0, 0.0),
                    Item("Frango Inteiro", "Natto", 1, 0.0),
                    Item("Frango Filet Peito", "Natto", 2, 0.0)
                )),
                Category("Limpeza e Higiene", mutableListOf()),
                Category("Temperos", mutableListOf()),
                Category("Bebidas", mutableListOf()),
                Category("Utensílios", mutableListOf())
            )
        )

        categoryAdapter = CategoryAdapter(
            allCategories,
            onAddItem = { category -> showAddDialog(category) },
            onEditItem = { item -> showEditDialog(item) },
            onDeleteItem = { item -> deleteItem(item) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = categoryAdapter

        btnFinalize.setOnClickListener {
            Toast.makeText(requireContext(), "Lista finalizada!", Toast.LENGTH_SHORT).show()
        }

        // Search: filtra por nome do item em todas as categorias
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                performFilter(query)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun performFilter(query: String) {
        if (query.isEmpty()) {
            // restaurar lista completa (sem filtrar)
            categoryAdapter.submitList(allCategories)
            return
        }

        val filtered = mutableListOf<Category>()
        for (cat in allCategories) {
            val matchItems = cat.items.filter { it.nome.contains(query, ignoreCase = true) }
            if (matchItems.isNotEmpty()) {
                // cria nova categoria com apenas os itens filtrados e já expandida
                filtered.add(Category(cat.name, matchItems.toMutableList(), expanded = true))
            }
        }
        categoryAdapter.submitList(filtered)
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
                    nome.text.toString().ifBlank { "Item sem nome" },
                    marca.text.toString().ifBlank { "" },
                    quantidade.text.toString().toIntOrNull() ?: 1,
                    preco.text.toString().toDoubleOrNull() ?: 0.0
                )
                category.items.add(item)
                // atualizar fonte original (allCategories) e adapter
                // encontrar a categoria original pelo nome e atualizar
                val original = allCategories.find { it.name == category.name }
                original?.items?.add(item)
                categoryAdapter.submitList(allCategories)
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
                categoryAdapter.submitList(allCategories)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteItem(item: Item) {
        for (category in allCategories) {
            if (category.items.remove(item)) {
                categoryAdapter.submitList(allCategories)
                break
            }
        }
    }
}