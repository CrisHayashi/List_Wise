package com.example.list_wise.ui.lists

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.list_wise.data.database.DatabaseHelper
import com.example.list_wise.data.repository.ListRepository

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    private val repository: ListRepository by lazy {
        val dbHelper = DatabaseHelper(appContext)
        ListRepository(dbHelper)
    }
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ListViewModel::class.java) ->
                ListViewModel(repository) as T

            modelClass.isAssignableFrom(HistoricViewModel::class.java) ->
                HistoricViewModel(repository) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
