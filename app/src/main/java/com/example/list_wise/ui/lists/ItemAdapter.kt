package com.example.list_wise.ui.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.list_wise.R
import java.text.NumberFormat
import java.util.Locale

class ItemAdapter(
    private val onEditItem: (Item) -> Unit,
    private val onDeleteItem: (Item) -> Unit
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(ItemDiffCallback()) {

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chkSelect: CheckBox = itemView.findViewById(R.id.chkSelectItem)
        private val txtItemName: TextView = itemView.findViewById(R.id.txtItemName)
        private val txtItemDetails: TextView = itemView.findViewById(R.id.txtItemDetails)
        private val txtItemPrice: TextView = itemView.findViewById(R.id.txtItemPrice)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditItem)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteItem)

        fun bind(item: Item) {
            // Nome do item
            txtItemName.text = item.nome

            // Detalhes opcionais (marca e quantidade)
            val detalhes = mutableListOf<String>()
            item.marca?.takeIf { it.isNotBlank() }?.let { detalhes.add(it) }
            if (item.quantidade > 1) detalhes.add("Qtd ${item.quantidade}")
            txtItemDetails.text = detalhes.joinToString(" • ")

            // Preço formatado com símbolo do real
            val precoText = if (item.preco > 0.0) currencyFormatter.format(item.preco) else "—"
            txtItemPrice.text = precoText

            // CheckBox
            chkSelect.setOnCheckedChangeListener(null) // evita reciclagem bug
            chkSelect.isChecked = item.isSelected
            chkSelect.setOnCheckedChangeListener { _, isChecked ->
                item.isSelected = isChecked
            }

            // Botões
            btnEdit.setOnClickListener { onEditItem(item) }
            btnDelete.setOnClickListener { onDeleteItem(item) }
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            // Identifica item unicamente pelo nome + marca
            return oldItem.nome == newItem.nome && oldItem.marca == newItem.marca
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }
}