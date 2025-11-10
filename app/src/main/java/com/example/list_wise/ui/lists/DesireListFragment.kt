package com.example.list_wise.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var viewModel: ListViewModel

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

        // 1. Inicializar o ViewModel usando a Factory (necessária pelo construtor com Context)
        viewModel = ViewModelProvider(this, ViewModelFactory(requireContext())).get(ListViewModel::class.java)

        txtListName.text = getString(R.string.title_desired_lists)
        btnFinalize.visibility = View.VISIBLE

        categoryAdapter = CategoryAdapter(
            onAddItem = { category -> /* abrir diálogo para adicionar item */ },
            onEditItem = { item -> /* abrir diálogo para editar item */ },
            onDeleteItem = { item -> /* excluir item */ }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = categoryAdapter

        viewModel.categorias.observe(viewLifecycleOwner) { categorias ->
            categoryAdapter.submitList(categorias)
        }

        val listaIdParaCarregar = arguments?.getInt("listaId")

        if (listaIdParaCarregar != null && listaIdParaCarregar > 0) {
            // Carrega a lista específica (Feira, Confraternização, etc.)
            viewModel.carregarItensDaListaSelecionada(listaIdParaCarregar)
            // 💡 IMPORTANTE: Você precisa garantir que a função ListRepository.obterItensDaLista(listaId)
            // traga apenas itens com 'comprado_na_lista = 0' se for a lista Desejada.

        } else {
            // Caso não tenha ID (erro ou a primeira vez), trate a falha
            Toast.makeText(requireContext(), "Erro: Nenhuma lista selecionada.", Toast.LENGTH_LONG).show()
        }

        // 4. Ação de Finalizar
        btnFinalize.setOnClickListener {
            // A lógica de Finalizar lista agora precisa do ID.
            if (listaIdParaCarregar != null && listaIdParaCarregar > 0) {
                Toast.makeText(requireContext(), "Preparando para finalizar lista ID: $listaIdParaCarregar...", Toast.LENGTH_SHORT).show()
                // 💡 Chamar a função de finalização do ViewModel aqui:
                // viewModel.finalizarLista(listaIdParaCarregar, "Nome Compra", "Local")
            } else {
                Toast.makeText(requireContext(), "Selecione uma lista antes de finalizar.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}