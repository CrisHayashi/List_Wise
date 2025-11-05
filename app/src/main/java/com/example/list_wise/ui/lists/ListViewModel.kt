package com.example.list_wise.ui.lists

import android.content.Context
import androidx.lifecycle.*
import com.example.list_wise.data.database.DatabaseHelper
import com.example.list_wise.data.model.*
import com.example.list_wise.data.repository.ListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    fun finalizarLista(nomeLista: String, itensSelecionados: List<Item>, local: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dataFinalizacao = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val lista = Lista(
                nome = nomeLista,
                dataFinalizacao = dataFinalizacao,
                totalGasto = itensSelecionados.sumOf { it.preco * it.quantidade },
                local = local,
                endereco = null, // futuramente via Google Maps
                quantidadeItens = itensSelecionados.size
            )
            val listaId = repository.inserirLista(lista).toInt()

            itensSelecionados.forEach { item ->
                val itemEntity = item.toEntity()
                val itemId = repository.inserirItem(itemEntity).toInt()
                val relation = ListaItemRelation(listaId, itemId, item.quantidade)
                repository.adicionarItemNaLista(relation)
            }
        }
    }
}