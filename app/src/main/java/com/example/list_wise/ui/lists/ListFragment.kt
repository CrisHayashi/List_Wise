package com.example.list_wise.ui.lists

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import com.example.list_wise.R
import com.example.list_wise.data.database.DatabaseHelper
import com.example.list_wise.data.model.ItemEntity
import com.example.list_wise.data.model.Lista
import com.example.list_wise.data.model.ListaItemRelation
import com.example.list_wise.data.repository.ListRepository
import com.example.list_wise.databinding.FragmentListBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ListViewModel

    // Banco de Dados e Repositório
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var repository: ListRepository

    // Estado da Lista Atual
    private var currentLista: Lista? = null

    // Adapter e lista para exibição
    private lateinit var categoryAdapter: CategoryAdapter
    private val displayCategories = mutableListOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(requireContext())
        )[ListViewModel::class.java]

        // Inicializa DB e repositório
        dbHelper = DatabaseHelper(requireContext())
        repository = ListRepository(dbHelper)

        setupRecyclerView()
        carregarListaAtiva()
        setupListeners()
    }

    private fun setupRecyclerView() {
        // Na tela de lista, checkbox significa "pertence à lista"
        categoryAdapter = CategoryAdapter(
            onAddItem = { category -> showAddDialog(category) },
            onEditItem = { item -> showEditDialog(item) },
            onDeleteItem = { item -> deleteItem(item) },
            onCheckItem = { item, isChecked ->
                val lista = currentLista ?: return@CategoryAdapter

                if (isChecked) {
                    // Marcou: se ainda não estava na lista, cria relação em lista_itens
                    if (item.relationId <= 0) {
                        val relation = ListaItemRelation(
                            listaId = lista.id,
                            itemId = item.id,
                            quantidadeDesejada = if (item.quantidade > 0) item.quantidade else 1,
                            precoEstimado = item.preco
                        )
                        repository.adicionarItemNaLista(relation)
                    }
                } else {
                    // Desmarcou: se tinha relação, remove da lista
                    if (item.relationId > 0) {
                        repository.removerItemDaLista(item.relationId)
                    }
                }
                // Recarrega a lista com o catálogo + status atualizado
                carregarListaAtiva()
        }
    )

        binding.recyclerViewCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            isVerticalScrollBarEnabled = true // barra de rolagem lateral
        }
    }

    private fun carregarListaAtiva() {
        currentLista = repository.getListaDesejadaAtiva()

        val lista = currentLista
        if (lista != null) {
            binding.textListName.text = lista.nome
            requireActivity().title = lista.nome

            // carrega TODO o catálogo com status para esta lista
            val itensDoBanco = repository.obterCatalogoComStatus(lista.id)
            atualizarListaVisual(itensDoBanco)
        } else {
            binding.textListName.text = getString(R.string.title_desired_lists)
            Toast.makeText(
                context,
                "Nenhuma lista ativa. Crie uma nova.",
                Toast.LENGTH_SHORT
            ).show()
            displayCategories.clear()
            categoryAdapter.submitList(displayCategories.toList())
        }
    }

    // Converte dados planos do SQLite (Triple) para a estrutura de Categorias da UI
    // Agora o Triple tem ListaItemRelation? (pode ser null) e Boolean = inLista
    private fun atualizarListaVisual(itensDb: List<Triple<ItemEntity, ListaItemRelation?, Boolean>>) {
        displayCategories.clear()

        // Agrupa por categoria (ou "Sem Categoria")
        val agrupado = itensDb.groupBy { it.first.categoria ?: "Sem Categoria" }

        agrupado.forEach { (nomeCategoria, listaTriples) ->
            val uiItems = listaTriples.map { (entity, relation, inLista) ->
                // Mapeia do DB para o objeto Item da UI
                Item(
                    id = entity.id, // Certifique-se que seu Item de UI tem ID
                    relationId = relation?.id ?: -1,                // ID da relação para updates
                    nome = entity.nome,
                    marca = entity.marca ?: "",
                    quantidade = relation?.quantidadeDesejada ?: 0, // 0 se ainda não está na lista
                    preco = relation?.precoEstimado ?: entity.precoPadrao,
                    isSelected = inLista,                           // agora significa "pertence à lista"
                    categoria = entity.categoria
                )
            }.toMutableList()

            displayCategories.add(Category(nomeCategoria, uiItems, expanded = true))
        }

        // Atualiza o adapter
        categoryAdapter.submitList(displayCategories.toList())
    }

     private fun setupListeners() {
        // Botão "Ir às compras"
         binding.btnFinalize.setOnClickListener {
             val action = ListFragmentDirections.actionListFragmentToShoppingFragment()
             findNavController().navigate(action)
         }

        // Pesquisa Rápida (Adiciona item direto no "Sem Categoria" ou busca)
         // ENTER faz BUSCA no catálogo primeiro, e só cria item novo se não encontrar
        binding.editSearchItem.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = (v as EditText).text.toString()
                if (query.isNotEmpty()) {
                    adicionarItemRapido(query)
                }
                true
            } else {
                false
            }
        }
        // Se quiser filtro em tempo real, descomente e implemente performFilter
        // binding.editSearchItem.addTextChangedListener(object : TextWatcher {
        //     override fun afterTextChanged(s: Editable?) { performFilter(s?.toString()?.trim() ?: "") }
        //     override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        //     override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        // })
    }

    private fun adicionarItemRapido(nomeItem: String) {
        val lista = currentLista ?: run {
            Toast.makeText(context, "Nenhuma lista ativa.", Toast.LENGTH_SHORT).show()
            return
        }

        // Adiciona ao catálogo
        val novoItem = ItemEntity(nome = nomeItem, categoria = null, precoPadrao = 0.0)
        val itemId = repository.inserirItem(novoItem)

        if (itemId != -1L) {
            // Adiciona relação na lista atual
            val relation = ListaItemRelation(
                listaId = lista.id,
                itemId = itemId.toInt(),
                quantidadeDesejada = 1,
                precoEstimado = 0.0
            )
            repository.adicionarItemNaLista(relation)

            // Limpa e recarrega
            binding.editSearchItem.setText("")
            carregarListaAtiva() // Recarrega tudo do banco
            Toast.makeText(context, "Item adicionado!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Erro ao adicionar item.", Toast.LENGTH_SHORT).show()
        }
    }

    // Não usamos mais diretamente aqui, mas mantive se quiser finalizar sem modo compra
    private fun finalizarCompra() {
        val lista = currentLista ?: run {
            Toast.makeText(context, "Nenhuma lista ativa.", Toast.LENGTH_SHORT).show()
            return
        }
        val local = "" // por enquanto não estamos usando campo de local
        val dataHoje = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val total = repository.calcularTotalGastoDaLista(lista.id, isHistorico = true)

        val sucesso = repository.finalizarListaEMigrar(
            listaId = lista.id,
            dataFinalizacao = dataHoje,
            local = local,
            endereco = null,
            totalGasto = total
        )

        if (sucesso) {
            Toast.makeText(context, "Lista Finalizada! Nova lista criada.", Toast.LENGTH_LONG).show()
            currentLista = null
            carregarListaAtiva()
        } else {
            Toast.makeText(context, "Erro ao finalizar.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Diálogos (Adaptados para chamar o Repository) ---
    private fun showAddDialog(category: Category) {
    // (Mantém a lógica de inflar o layout do diálogo)
    val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
    val nomeEdit = dialogView.findViewById<EditText>(R.id.editItemName)
    val marcaEdit = dialogView.findViewById<EditText>(R.id.editItemBrand)
    val categoriaEdit = dialogView.findViewById<EditText>(R.id.editItemCategory)
    val quantidadeEdit = dialogView.findViewById<EditText>(R.id.editItemQuantity)
    val precoEdit = dialogView.findViewById<EditText>(R.id.editItemPrice)

    categoriaEdit.setText(category.name)

    AlertDialog.Builder(requireContext())
        .setTitle("Novo item em ${category.name}")
        .setView(dialogView)
        .setPositiveButton("Adicionar") { _, _ ->
            val nome = nomeEdit.text.toString().ifBlank { "Item" }
            val marca = marcaEdit.text.toString().ifBlank { null }
            val quantidade = quantidadeEdit.text.toString().toIntOrNull() ?: 1
            val preco = precoEdit.text.toString().toDoubleOrNull() ?: 0.0
            val categoria = categoriaEdit.text.toString().ifBlank { category.name }

            // 1) Insere no catálogo
            val itemEntity = ItemEntity(
                nome = nome,
                marca = marca,
                precoPadrao = preco,
                categoria = categoria
            )
            val itemId = repository.inserirItem(itemEntity)

            // 2) Se existir lista ativa, relaciona com a lista
            val lista = currentLista
            if (itemId != -1L && lista != null) {
                val relation = ListaItemRelation(
                    listaId = lista.id,
                    itemId = itemId.toInt(),
                    quantidadeDesejada = quantidade,
                    precoEstimado = preco
                )
                repository.adicionarItemNaLista(relation)
            }

            carregarListaAtiva()
        }
        .setNegativeButton("Cancelar", null)
        .show()
    }

    // --- Diálogo para EDITAR item existente 🔧 NOVO ---
    private fun showEditDialog(item: Item) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val nomeEdit = dialogView.findViewById<EditText>(R.id.editItemName)
        val marcaEdit = dialogView.findViewById<EditText>(R.id.editItemBrand)
        val categoriaEdit = dialogView.findViewById<EditText>(R.id.editItemCategory)
        val quantidadeEdit = dialogView.findViewById<EditText>(R.id.editItemQuantity)
        val precoEdit = dialogView.findViewById<EditText>(R.id.editItemPrice)

        // 🔧 Preenche com dados atuais do item
        nomeEdit.setText(item.nome)
        marcaEdit.setText(item.marca ?: "")
        quantidadeEdit.setText(if (item.quantidade > 0) item.quantidade.toString() else "1")
        precoEdit.setText(item.preco.toString())
        categoriaEdit.setText(item.categoria ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Item")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val novoNome = nomeEdit.text.toString().ifBlank { "Item" }
                val novaMarca = marcaEdit.text.toString().ifBlank { null }
                val novaQuantidade = quantidadeEdit.text.toString().toIntOrNull() ?: 1
                val novoPreco = precoEdit.text.toString().toDoubleOrNull() ?: 0.0
                val novaCategoria = categoriaEdit.text.toString().ifBlank { null }

                // Atualiza no catálogo
                repository.atualizarItemCatalogo(
                    itemId = item.id,
                    novoNome = novoNome,
                    novaMarca = novaMarca,
                    novaCategoria = novaCategoria,
                    novoPrecoPadrao = novoPreco
                )

                // Atualiza na relação com a lista (se existir)
                if (item.relationId > 0) {
                    repository.atualizarItemDaLista(
                        relationId = item.relationId,
                        novaQuantidade = novaQuantidade,
                        novoPrecoEstimado = novoPreco
                    )
                }

                // Atualiza objeto da UI
                item.nome = novoNome
                item.marca = novaMarca
                item.quantidade = novaQuantidade
                item.preco = novoPreco
                item.categoria = novaCategoria

                carregarListaAtiva()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteItem(item: Item) {
        if (item.relationId > 0) {
            repository.deletarItemDaLista(item.relationId)
        }
        carregarListaAtiva()
    }

    override fun onDestroyView() {
        dbHelper.close()
        super.onDestroyView()
        _binding = null
    }
}