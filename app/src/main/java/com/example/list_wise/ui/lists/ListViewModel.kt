package com.example.list_wise.ui.lists

import android.content.Context
import androidx.lifecycle.*
import com.example.list_wise.data.database.DatabaseHelper
import com.example.list_wise.data.model.*
import com.example.list_wise.data.repository.ListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListViewModel(context: Context) : ViewModel() {

    private val repository = ListRepository(DatabaseHelper(context))

    private val _categorias = MutableLiveData<List<Category>>()
    val categorias: LiveData<List<Category>> = _categorias

    fun carregarItensAgrupadosPorCategoria() {
        viewModelScope.launch(Dispatchers.IO) {
            val itens = repository.obterTodosItens()
            val agrupados = itens.groupBy { it.categoria ?: "Sem categoria" }

            val categoriasConvertidas = agrupados.map { (categoria, lista) ->
                Category(
                    name = categoria,
                    items = lista.map { it.toUI(it.quantidade) }.toMutableList()
                )
            }

            _categorias.postValue(categoriasConvertidas)
        }
    }

    private val _listasDesejadas = MutableLiveData<List<Lista>>()
    val listasDesejadas: LiveData<List<Lista>> = _listasDesejadas

    private val _listaSelecionadaId = MutableLiveData<Int>() // O ID da lista que está sendo visualizada/editada
    val listaSelecionadaId: LiveData<Int> = _listaSelecionadaId

    fun carregarTodasListasDesejadas() {
        viewModelScope.launch(Dispatchers.IO) {
            val listas = repository.obterTodasListasDesejadas()
            _listasDesejadas.postValue(listas)
            // Se houver listas, selecione a primeira ou a mais recente
            if (listas.isNotEmpty()) {
                _listaSelecionadaId.postValue(listas.first().id)
                carregarItensDaListaSelecionada(listas.first().id)
            }
        }
    }

    // Carregar itens de uma lista específica (desejada ou comprada)
    fun carregarItensDaListaSelecionada(listaId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val itensRelacionados = repository.obterItensDaLista(listaId)
            val agrupados = itensRelacionados.groupBy { it.first.categoria ?: "Sem categoria" }

            val categorias = agrupados.map { (categoria, lista) ->
                Category(
                    name = categoria,
                    items = lista.map { (itemEntity, quantidade) -> itemEntity.toUI(quantidade) }.toMutableList()
                )
            }
            _categorias.postValue(categorias)
            _listaSelecionadaId.postValue(listaId) // Atualiza qual lista estamos vendo
        }
    }

    // Criar uma nova Lista Desejada
    fun criarNovaListaDesejada(nome: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val lista = Lista(nome = nome, finalizada = false, dataCriacao = "")
            repository.inserirLista(lista) // Insere uma nova lista não finalizada
            carregarTodasListasDesejadas() // Recarrega a lista de listas
        }
    }

    // Finalizar Lista (adaptado para receber o ID)
    fun finalizarLista(listaId: Int, nomeLista: List<Item>, local: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Calcular total gasto apenas dos itens comprados
            val totalGasto = repository.calcularTotalGastoDaLista(listaId)

            // 2. Marcar a lista como finalizada (finalizada=1)
            // TODO criar função
            //repository.finalizarListaAtual(listaId, nomeLista, totalGasto, local)

            // 3. Recarregar o estado do ViewModel (a lista finalizada sai das "desejadas")
            carregarTodasListasDesejadas()
        }
    }

    //AJUSTE na função toggleItemComprado (usando o ID da lista selecionada)
    fun toggleItemComprado(item: Item, comprado: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _listaSelecionadaId.value?.let { listaId ->
                // TODO criar funç~so
                //repository.marcarItemNaLista(item.id, listaId, comprado)
                // Opcional: Recarregar apenas os itens da lista para atualizar a UI, se necessário
                carregarItensDaListaSelecionada(listaId)
            }
        }
    }

    fun carregarItensDeListaFinalizada(listaId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val itensRelacionados = repository.obterItensDaLista(listaId)
            val agrupados = itensRelacionados.groupBy { it.first.categoria ?: "Sem categoria" }

            val categorias = agrupados.map { (categoria, lista) ->
                Category(
                    name = categoria,
                    items = lista.map { (itemEntity, quantidade) -> itemEntity.toUI(quantidade) }.toMutableList()
                )
            }

            _categorias.postValue(categorias)
        }
    }

    fun carregarUltimaListaFinalizada() {
        viewModelScope.launch(Dispatchers.IO) {
            val ultima = repository.obterUltimaLista()
            ultima?.let { carregarItensDeListaFinalizada(it.id) }
        }
    }

    fun adicionarItem(item: Item, categoria: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val itemEntity = item.toEntity().copy(categoria = categoria)
            repository.inserirItem(itemEntity)
            carregarItensAgrupadosPorCategoria()
        }
    }
}