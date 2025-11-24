package com.example.list_wise.ui.lists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.list_wise.data.repository.ListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.list_wise.data.model.ItemEntity
import com.example.list_wise.data.model.Lista
import com.example.list_wise.data.model.ListaItemRelation

class ListViewModel(private val repository: ListRepository) : ViewModel() {

    private val _listaDesejada = MutableLiveData<Lista?>()
    val listaDesejada: LiveData<Lista?> = _listaDesejada

    private val _categorias = MutableLiveData<List<Category>>(emptyList())
    val categorias: LiveData<List<Category>> = _categorias

    private val _historicoListas = MutableLiveData<List<Lista>>(emptyList())
    val historicoListas: LiveData<List<Lista>> = _historicoListas

    private val _totalGasto = MutableLiveData(0.0)
    val totalGasto: LiveData<Double> = _totalGasto

    //catálogo de itens (todos os itens cadastrados)
    private val _catalogoItens = MutableLiveData<List<ItemEntity>>(emptyList())
    val catalogoItens: LiveData<List<ItemEntity>> = _catalogoItens

    init {
        carregarListaDesejada()
        carregarHistorico()
        carregarCatalogo()
    }

    fun carregarListaDesejada() {
        viewModelScope.launch (Dispatchers.IO) {
            val lista = repository.getListaDesejadaAtiva()
            _listaDesejada.postValue(lista)

            if (lista != null) {
                carregarItensDaListaInterno(lista.id, isHistorico = false)
            } else {
                _categorias.postValue(emptyList())
                _totalGasto.postValue(0.0)
            }
        }
    }
    private fun carregarItensDaListaInterno(listaId: Int, isHistorico: Boolean) {
        val triples = repository.obterItensDaLista(listaId)

        val grouped = triples.groupBy { triple ->
            val itemEntity = triple.first
            itemEntity.categoria ?: "Sem Categoria"
        }

        val categoriasUi = grouped.map { (categoriaNome, listaTriples) ->
            val items = listaTriples.map { (entity, relation, comprado) ->
                Item(
                    id = entity.id,
                    relationId = relation.id,
                    nome = entity.nome,
                    marca = entity.marca,
                    quantidade = relation.quantidadeDesejada,
                    preco = if (isHistorico) {
                        relation.precoPago ?: relation.precoEstimado
                    } else {
                        relation.precoEstimado
                    },
                    isSelected = comprado
                )
            }.toMutableList()

            Category(
                name = categoriaNome,
                items = items,
                expanded = true
            )
        }.sortedBy { it.name.lowercase() }

        _categorias.postValue(categoriasUi)
        val total = repository.calcularTotalGastoDaLista(listaId, isHistorico)
        _totalGasto.postValue(total)
    }

    fun carregarItensDaListaSelecionada(listaId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            carregarItensDaListaInterno(listaId, isHistorico = false)
        }
    }

    fun carregarItensDeListaFinalizada(listaId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            carregarItensDaListaInterno(listaId, isHistorico = true)
        }
    }

    fun marcarItemComoComprado(relationId: Int, comprado: Boolean, precoPago: Double?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.marcarItemComoComprado(relationId, comprado, precoPago)
            // Recarrega a lista desejada ativa
            carregarListaDesejada()
        }
    }

    fun finalizarLista(
        listaId: Int,
        dataFinalizacao: String,
        local: String?,
        endereco: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val total = repository.calcularTotalGastoDaLista(listaId, isHistorico = true)
            val ok = repository.finalizarListaEMigrar(
                listaId = listaId,
                dataFinalizacao = dataFinalizacao,
                local = local,
                endereco = endereco,
                totalGasto = total
            )
            if (ok) {
                carregarListaDesejada()
                carregarHistorico()
            }
        }
    }

    fun carregarHistorico() {
        viewModelScope.launch(Dispatchers.IO) {
            val listas = repository.obterTodasListasFinalizadas()
            _historicoListas.postValue(listas)
        }
    }

    fun adicionarItem(
        listaId: Int,
        itemEntity: ItemEntity,
        quantidade: Int,
        precoEstimado: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.adicionarItemNaLista(
                ListaItemRelation(
                    listaId = listaId,
                    itemId = itemEntity.id,
                    quantidadeDesejada = quantidade,
                    precoEstimado = precoEstimado
                )
            )
            carregarItensDaListaInterno(listaId, isHistorico = false)
        }
    }

    // carrega todos os itens do catálogo a partir do repositório
    fun carregarCatalogo() {
        viewModelScope.launch(Dispatchers.IO) {
            val itens = repository.obterTodosItensCatalogo()
            _catalogoItens.postValue(itens)
        }
    }
}