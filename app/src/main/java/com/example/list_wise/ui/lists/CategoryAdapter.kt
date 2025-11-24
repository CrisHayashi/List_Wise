package com.example.list_wise.ui.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.list_wise.R

class CategoryAdapter(
    private val onAddItem: (Category) -> Unit,
    private val onEditItem: (Item) -> Unit,
    private val onDeleteItem: (Item) -> Unit,
    private val onCheckItem: (Item, Boolean) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtCategoryName: TextView = itemView.findViewById(R.id.txtCategoryName)
        private val btnExpand: ImageView = itemView.findViewById(R.id.btnExpand)
        private val btnAdd: ImageButton = itemView.findViewById(R.id.btnAddItemToCategory)
        private val rvItems: RecyclerView = itemView.findViewById(R.id.recyclerViewItems)
        private val itemAdapter = ItemAdapter(onEditItem, onDeleteItem, onCheckItem)

        init {
            rvItems.layoutManager = LinearLayoutManager(itemView.context)
            rvItems.adapter = itemAdapter
            rvItems.isNestedScrollingEnabled = false
        }

        fun bind(category: Category) {
            txtCategoryName.text = category.name
            btnExpand.rotation = if (category.expanded) 180f else 0f
            rvItems.visibility = if (category.expanded) View.VISIBLE else View.GONE

            /// Exibe os itens se a categoria estiver expandida
            if (category.expanded) {
                val sortedItems = category.items.sortedBy { it.nome.lowercase() }
                itemAdapter.submitList(sortedItems)
            }

            btnExpand.setOnClickListener {
                category.expanded = !category.expanded
                btnExpand.animate().rotation(if (category.expanded) 180f else 0f).setDuration(200)
                rvItems.visibility = if (category.expanded) View.VISIBLE else View.GONE

                if (category.expanded) {
                    val sortedItems = category.items.sortedBy { it.nome.lowercase() }
                    itemAdapter.submitList(sortedItems)
               }
            }

            btnAdd.setOnClickListener {
                onAddItem(category)
            }
        }
    }
    // DiffUtil para Category
    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem.name == newItem.name // usa nome como ID da categoria

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem == newItem
    }
}