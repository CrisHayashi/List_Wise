package com.example.list_wise.ui.lists

import android.content.Context
import androidx.lifecycle.*
import com.example.list_wise.data.database.DatabaseHelper
import com.example.list_wise.data.model.Lista
import com.example.list_wise.data.repository.ListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoricViewModel(context: Context) : ViewModel() {

    private val repository = ListRepository(DatabaseHelper(context))

    private val _gastos = MutableLiveData<Double>()
    val gastos: LiveData<Double> = _gastos

    private val _totalListas = MutableLiveData<Int>()
    val totalListas: LiveData<Int> = _totalListas

    private val _itensMaisComprados = MutableLiveData<List<String>>()
    val itensMaisComprados: LiveData<List<String>> = _itensMaisComprados

    private val _listasFinalizadas = MutableLiveData<List<Lista>>()
    val listasFinalizadas: LiveData<List<Lista>> = _listasFinalizadas

    fun carregarDados() {
        viewModelScope.launch(Dispatchers.IO) {
            val listas = repository.obterTodasListasFinalizadas()
            val totalGasto = listas.sumOf { it.totalGasto }
            val maisComprados = repository.obterItensMaisComprados()

            _gastos.postValue(totalGasto)
            _totalListas.postValue(listas.size)
            _listasFinalizadas.postValue(listas)
            _itensMaisComprados.postValue(maisComprados)
        }
    }
}