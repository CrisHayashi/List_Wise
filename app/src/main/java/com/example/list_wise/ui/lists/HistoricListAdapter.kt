package com.example.list_wise.ui.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.list_wise.R
import com.example.list_wise.data.model.Lista
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoricListAdapter : ListAdapter<Lista, HistoricListAdapter.ListaViewHolder>(DiffCallback()) {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historic_list, parent, false)
        return ListaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ListaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNome = itemView.findViewById<TextView>(R.id.txtListName)
        private val txtLocal = itemView.findViewById<TextView>(R.id.txtListLocation)
        private val txtEndereco = itemView.findViewById<TextView>(R.id.txtListAddress)
        private val txtValor = itemView.findViewById<TextView>(R.id.txtListTotal)
        private val txtData = itemView.findViewById<TextView>(R.id.txtListDate)
        private val txtStatus = itemView.findViewById<TextView>(R.id.txtListStatus)

        fun bind(lista: Lista) {
            txtNome.text = lista.nome
            txtLocal.text = lista.local ?: "Local não informado"
            txtEndereco.text = lista.endereco ?: "Endereço não informado"
            txtValor.text = currencyFormatter.format(lista.totalGasto)
            txtData.text = lista.dataFinalizacao
            txtStatus.text = "${lista.quantidadeItens} Itens"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Lista>() {
        override fun areItemsTheSame(oldItem: Lista, newItem: Lista): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Lista, newItem: Lista): Boolean = oldItem == newItem
    }
}