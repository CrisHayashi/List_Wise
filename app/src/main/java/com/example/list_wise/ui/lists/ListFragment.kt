package com.example.list_wise.ui.lists

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.navArgs
import com.example.list_wise.databinding.FragmentListBinding
import com.example.list_wise.R

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    //Lista de categorias com seus itens
    private val allCategories = mutableListOf<Category>()

    // Adaptador principal
    private lateinit var categoryAdapter: CategoryAdapter

    // SafeArgs
    private val args: ListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Define o nome da lista dinamicamente
        binding.textListName.text = args.listName
        requireActivity().title = args.listName

        // Configura RecyclerView
        categoryAdapter = CategoryAdapter(
            onAddItem = { category -> showAddDialog(category) },
            onEditItem = { item -> showEditDialog(item) },
            onDeleteItem = { item -> deleteItem(item) }
        )
        binding.recyclerViewCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            isVerticalScrollBarEnabled = true // barra de rolagem lateral
        }

        // Carrega todas as categorias de uma vez (mock)
        loadCategories()

        // Botão "Finalizar"
        binding.btnFinalize.setOnClickListener {
            // Pega todos os itens selecionados de todas as categorias
            val selectedItems = allCategories.flatMap { it.items }.filter { it.isSelected }

            // Futuramente, colocar aqui a navegaçao para a próxima tela via safeargs e enviar os itens
            // Trecho comentado até implementar a próxima tela:
            // <action
            //     android:id="@+id/action_listFragment_to_selectedItemsFragment"
            //     app:destination="@id/selectedItemsFragment" />
            // Para enviar a lista de itens, é necessário que Item implemente Parcelable:
            // val action = ListFragmentDirections
            //     .actionListFragmentToSelectedItemsFragment(selectedItems.toTypedArray())
            // findNavController().navigate(action)

            // Por enquanto apenas exibe o Toast com quantidade de itens selecionados
            Toast.makeText(
                requireContext(),
                "Lista finalizada! Itens selecionados: ${selectedItems.size}",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Filtro de pesquisa
        binding.editSearchItem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { performFilter(s?.toString()?.trim() ?: "")}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    //** Carrega todas as categorias mockadas */
    private fun loadCategories() {
        allCategories.clear()
        allCategories.addAll(
            listOf(
                Category(
                    "Mercearia", mutableListOf(
                        Item("Arroz Branco", "Tio Joao", 1, 8.90),
                        Item("Feijão Preto", "Turquesa", 1, 9.90),
                        Item("Azeite de Oliva Extra Virgem", "Galo", 1, 29.90),
                        Item("Farinha de Mandioca", "Yoki", 1, 5.90),
                        Item("Farinha de Trigo", "Sao Braz", 1, 6.00),
                        Item("Macarrao Fettuccine", "Paganini", 1, 0.0)
                    )
                ),
                Category(
                    "Frutas e Legumes", mutableListOf(
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
                    )
                ),
                Category(
                    "Frios e Congelados", mutableListOf(
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
                    )
                ),
                Category(
                    "Limpeza e Higiene", mutableListOf(
                        Item("Sabão em pó", "OMO", 1, 22.0),
                        Item("Detergente", "Ypê", 3, 2.50)
                    )
                ),
                Category(
                    "Temperos", mutableListOf(
                        Item("Sal", "Cisne", 1, 2.0),
                        Item("Pimenta-do-reino", "Kitano", 1, 4.5)
                    )
                ),
                Category(
                    "Bebidas", mutableListOf(
                        Item("Água mineral", "Crystal", 6, 2.0),
                        Item("Suco de uva", "Aurora", 1, 8.0)
                    )
                ),
                Category("Utensílios", mutableListOf())
            )
        )
        categoryAdapter.submitList(allCategories.toList())
    }

    private fun performFilter(query: String) {
        if (query.isEmpty()) {
            // restaurar lista completa (sem filtrar)
            categoryAdapter.submitList(allCategories.toList())
            return
        }

        val filtered = allCategories.mapNotNull { cat ->
            val matched = cat.items.filter { it.nome.contains(query, ignoreCase = true) }
            if (matched.isNotEmpty()) Category(cat.name, matched.toMutableList(), expanded = true) else null
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

                // Atualiza a lista inteira sem criar novas instâncias
                category.expanded = true // garante que categoria está expandida
                categoryAdapter.notifyItemChanged(allCategories.indexOf(category))
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

                // Atualiza a categoria que contém o item
                val category = allCategories.find { it.items.contains(item) }
                category?.let {
                    categoryAdapter.notifyItemChanged(allCategories.indexOf(it))
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteItem(item: Item) {
        val category = allCategories.find { it.items.contains(item) }
        category?.items?.remove(item)
        category?.let {
            categoryAdapter.notifyItemChanged(allCategories.indexOf(it))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}