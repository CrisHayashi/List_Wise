package com.example.list_wise.ui.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.list_wise.R

data class Category(
    val name: String,
    val items: MutableList<Item>,
    var isExpanded: Boolean = true
)

class CategoryAdapter(
    private val categories: MutableList<Category>,
    private val onAddItem: (Category) -> Unit,
    private val onEditItem: (Item) -> Unit,
    private val onDeleteItem: (Item) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtCategoryTitle)
        val btnAdd: ImageButton = view.findViewById(R.id.btnAddItemCategory)
        val recyclerViewSubItems: RecyclerView = view.findViewById(R.id.recyclerViewSubItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.title.text = category.name

        // Sub-lista de itens da categoria
        val itemAdapter = ItemAdapter(category.items, onEditItem, onDeleteItem)
        holder.recyclerViewSubItems.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.recyclerViewSubItems.adapter = itemAdapter

        // Exibir/ocultar lista de itens
        holder.recyclerViewSubItems.visibility =
            if (category.isExpanded) View.VISIBLE else View.GONE

        holder.title.setOnClickListener {
            category.isExpanded = !category.isExpanded
            notifyItemChanged(position)
        }

        holder.btnAdd.setOnClickListener {
            onAddItem(category)
        }
    }

    override fun getItemCount(): Int = categories.size
}