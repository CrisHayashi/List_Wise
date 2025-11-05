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
import androidx.lifecycle.ViewModelProvider
import com.example.list_wise.R
import com.example.list_wise.ui.lists.ListViewModel

class ListFragment : Fragment() {

    private lateinit var viewModel: ListViewModel

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

        viewModel = ViewModelProvider(this, ViewModelFactory(requireContext())).get(ListViewModel::class.java)

        binding.recyclerViewCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            isVerticalScrollBarEnabled = true // barra de rolagem lateral
        }

        // Carrega todas as categorias de uma vez
        viewModel.categorias.observe(viewLifecycleOwner) { categorias ->
            allCategories.clear()
            allCategories.addAll(categorias)
            categoryAdapter.submitList(categorias)
        }

        viewModel.carregarItensAgrupadosPorCategoria()

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

            val localInformado = binding.editListLocation.text.toString().ifBlank { "Local não informado" }
            if (selectedItems.isNotEmpty()) {
                viewModel.finalizarLista(args.listName, selectedItems, localInformado)
                Toast.makeText(requireContext(), "Lista salva com ${selectedItems.size} itens!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Nenhum item selecionado.", Toast.LENGTH_SHORT).show()
            }
        }

        // Filtro de pesquisa
        binding.editSearchItem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { performFilter(s?.toString()?.trim() ?: "")}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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
                viewModel.adicionarItem(item, category.name)

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