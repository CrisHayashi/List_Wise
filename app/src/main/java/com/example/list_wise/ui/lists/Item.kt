package com.example.list_wise.ui.lists

data class Item (
    var nome: String,
    var marca: String? = null,
    var quantidade: Int = 1,
    var preco: Double = 0.0,
    var isSelected: Boolean = false
)