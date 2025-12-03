package com.example.list_wise.ui.lists

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.list_wise.R
import com.example.list_wise.data.database.DatabaseHelper
import com.example.list_wise.data.model.ItemEntity
import com.example.list_wise.data.model.Lista
import com.example.list_wise.data.model.ListaItemRelation
import com.example.list_wise.data.repository.ListRepository
import com.example.list_wise.databinding.FragmentListBinding
import java.text.Normalizer
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
                recarregarMantendoFiltro()
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

            // carrega
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

         // ENTER faz buscar
         binding.editSearchItem.setOnEditorActionListener { v, actionId, _ ->
             if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                 val query = (v as EditText).text.toString()
                 performFilter(query)
                 true
             } else {
                 false
             }
         }

         // Filtro em tempo real conforme digita
         binding.editSearchItem.addTextChangedListener(object : TextWatcher {
             override fun afterTextChanged(s: Editable?) {
                 performFilter(s?.toString().orEmpty())
             }
             override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
             override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
         })

         // Clicar no ícone da lupa para LIMPAR a busca e voltar pra lista inteira
         binding.searchLayout.setEndIconOnClickListener {
             if (!binding.editSearchItem.text.isNullOrBlank()) {
                 // limpa campo e, por causa do TextWatcher, a lista volta ao normal
                 binding.editSearchItem.text?.clear()
             } else {
                 // se já estiver vazio por algum motivo, garante a lista inteira
                 categoryAdapter.submitList(displayCategories.toList())
             }
         }
     }

    // Normaliza string pra busca: tudo minúsculo e sem acento
    private fun String.normalizeForSearch(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        // remove os caracteres de acento (combining marks)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
    }

    private fun performFilter(query: String) {
        val texto = query.trim()

        if (texto.isEmpty()) {
            // Volta para a lista completa
            categoryAdapter.submitList(displayCategories.toList())
            return
        }

        val queryNorm = texto.normalizeForSearch()

        // Cria uma lista filtrada com categorias só com os itens correspondentes
        val filteredCategories = displayCategories.mapNotNull { category ->
            val itensFiltrados = category.items
                .filter { item ->
                    item.nome.normalizeForSearch().contains(queryNorm)
                }
                .toMutableList()

            if (itensFiltrados.isNotEmpty()) {
                Category(
                    name = category.name,
                    items = itensFiltrados,
                    expanded = true  // sempre aberta na busca
                )
            } else {
                null
            }
        }

        categoryAdapter.submitList(filteredCategories)
    }

    // Recarrega a lista do banco e, se tiver texto na busca,
// reaplica o filtro atual
    private fun recarregarMantendoFiltro() {
        // 1) Recarrega do banco e atualiza displayCategories
        carregarListaAtiva()

        // 2) Pega o texto atual do campo de busca
        val query = binding.editSearchItem.text?.toString().orEmpty()

        // 3) Se tiver algo digitado, reaplica o filtro
        if (query.isNotBlank()) {
            performFilter(query)
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

            recarregarMantendoFiltro()
        }
        .setNegativeButton("Cancelar", null)
        .show()
    }

    // Diálogo para EDITAR item existente
    private fun showEditDialog(item: Item) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val nomeEdit = dialogView.findViewById<EditText>(R.id.editItemName)
        val marcaEdit = dialogView.findViewById<EditText>(R.id.editItemBrand)
        val categoriaEdit = dialogView.findViewById<EditText>(R.id.editItemCategory)
        val quantidadeEdit = dialogView.findViewById<EditText>(R.id.editItemQuantity)
        val precoEdit = dialogView.findViewById<EditText>(R.id.editItemPrice)

        // Preenche com dados atuais do item
        nomeEdit.setText(item.nome)
        marcaEdit.setText(item.marca ?: "")
        quantidadeEdit.setText(if (item.quantidade > 0) item.quantidade.toString() else "1")
        precoEdit.setText(
            String.format(Locale.getDefault(), "%.2f", item.preco))
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

                recarregarMantendoFiltro()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteItem(item: Item) {
        val lista = currentLista ?: run {
            Toast.makeText(requireContext(), "Nenhuma lista ativa.", Toast.LENGTH_SHORT).show()
            return
        }

        if (item.relationId > 0) {
            if (item.quantidade > 1) {
                // Só diminui a quantidade na relação lista_itens
                repository.atualizarItemDaLista(
                    relationId = item.relationId,
                    novaQuantidade = item.quantidade - 1,
                    novoPrecoEstimado = item.preco
                )
            } else {
                // Quantidade 1 ou 0: remove da lista, MAS NÃO remove do catálogo
                repository.deletarItemDaLista(item.relationId)
            }
        } else {
        }
        recarregarMantendoFiltro()
    }

    override fun onDestroyView() {
        dbHelper.close()
        super.onDestroyView()
        _binding = null
    }
}
