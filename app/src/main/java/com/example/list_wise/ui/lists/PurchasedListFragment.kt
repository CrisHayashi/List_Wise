package com.example.list_wise.ui.lists

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.list_wise.R
import com.example.list_wise.data.database.DatabaseHelper
import com.example.list_wise.data.model.ItemEntity
import com.example.list_wise.data.model.ListaItemRelation
import com.example.list_wise.data.repository.ListRepository
import com.example.list_wise.databinding.FragmentPurchasedListBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class PurchasedListFragment : Fragment() {

    private var _binding: FragmentPurchasedListBinding? = null
    private val binding get() = _binding!!

    private val args: PurchasedListFragmentArgs by navArgs()

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var repository: ListRepository

    private lateinit var categoryAdapter: CategoryAdapter
    private val displayCategories = mutableListOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPurchasedListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Título do toolbar
        val titleView = requireActivity().findViewById<TextView>(R.id.txtToolbarTitle)
        titleView.text = getString(R.string.title_purchased_list)

        dbHelper = DatabaseHelper(requireContext())
        repository = ListRepository(dbHelper)

        setupRecyclerView()
        carregarDetalhesDaLista(args.listaId)
        carregarItensDaLista(args.listaId)
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onAddItem = { /* não adiciona item em lista finalizada */ },
            onEditItem = { /* opcional: editar dados da compra no futuro */ },
            onDeleteItem = { /* não remove aqui */ },
            onCheckItem = { _, _ ->
                // Em lista finalizada, não vamos mexer em "comprado"
            }
        )

        binding.recyclerViewPurchasedItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            isVerticalScrollBarEnabled = true
        }
    }

    private fun carregarDetalhesDaLista(listaId: Int) {
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.rawQuery(
            """
            SELECT nome, dataFinalizacao, local, totalGasto
            FROM listas
            WHERE id = ?
            """.trimIndent(),
            arrayOf(listaId.toString())
        )

        if (cursor.moveToFirst()) {
            val nome = cursor.getString(0) ?: ""
            val dataFinalizacao = cursor.getString(1) ?: "-"
            val local = cursor.getString(2) ?: "-"
            val total = cursor.getDouble(3)

            binding.txtPurchasedListName.text = nome
            binding.txtPurchasedDate.text =
                getString(R.string.purchased_date_format, dataFinalizacao)
            binding.txtPurchasedLocation.text =
                getString(R.string.purchased_location_format, local)
            binding.txtPurchasedTotal.text =
                getString(R.string.purchased_total_format, total)
        }

        cursor.close()
    }

    private fun carregarItensDaLista(listaId: Int) {
        val itensDb: List<Triple<ItemEntity, ListaItemRelation, Boolean>> =
            repository.obterItensDaLista(listaId)

        displayCategories.clear()

        val agrupado = itensDb.groupBy { it.first.categoria ?: "Sem Categoria" }

        agrupado.forEach { (nomeCategoria, listaTriples) ->
            val uiItems = listaTriples.map { (entity, relation, comprado) ->
                Item(
                    id = entity.id,
                    relationId = relation.id,
                    nome = entity.nome,
                    marca = entity.marca ?: "",
                    quantidade = relation.quantidadeDesejada,
                    preco = relation.precoEstimado,
                    isSelected = comprado,  // já vem marcado do histórico
                    categoria = entity.categoria
                )
            }.toMutableList()

            displayCategories.add(
                Category(
                    name = nomeCategoria,
                    items = uiItems,
                    expanded = true
                )
            )
        }

        categoryAdapter.submitList(displayCategories.toList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}