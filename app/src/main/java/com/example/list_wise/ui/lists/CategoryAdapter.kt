package com.example.list_wise.ui.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.list_wise.R

class CategoryAdapter(
    private var categories: MutableList<Category>,
    private val onAddItem: (Category) -> Unit,
    private val onEditItem: (Item) -> Unit,
    private val onDeleteItem: (Item) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    fun submitList(newList: List<Category>) {
        categories = newList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtCategoryName: TextView = itemView.findViewById(R.id.txtCategoryName)
        private val btnExpand: ImageView = itemView.findViewById(R.id.btnExpand)
        private val btnAdd: ImageButton = itemView.findViewById(R.id.btnAddItemToCategory)
        private val rvItems: RecyclerView = itemView.findViewById(R.id.recyclerViewItems)
        private val itemAdapter = ItemAdapter(onEditItem, onDeleteItem)

        init {
            rvItems.layoutManager = LinearLayoutManager(itemView.context)
            rvItems.adapter = itemAdapter
        }

        fun bind(category: Category) {
            txtCategoryName.text = category.name
            btnExpand.rotation = if (category.expanded) 180f else 0f

            /// Exibe os itens se a categoria estiver expandida
            if (category.expanded) {
                val sorted = category.items.sortedBy { it.nome.lowercase() }
                itemAdapter.submitList(sorted.toMutableList())
                rvItems.visibility = View.VISIBLE
            } else {
                rvItems.visibility = View.GONE
            }

            btnExpand.setOnClickListener {
                category.expanded = !category.expanded
                btnExpand.animate().rotation(if (category.expanded) 180f else 0f).setDuration(200)

                if (category.expanded) {
                    val sorted = category.items.sortedBy { it.nome.lowercase() }
                    itemAdapter.submitList(sorted.toMutableList())
                    rvItems.visibility = View.VISIBLE
                } else {
                    rvItems.visibility = View.GONE
                }
            }

            btnAdd.setOnClickListener {
                onAddItem(category)
            }
        }
    }
}