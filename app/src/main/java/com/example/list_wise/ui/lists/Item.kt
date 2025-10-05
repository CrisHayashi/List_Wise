package com.example.list_wise.ui.lists

data class Item (
    var nome: String,
    var marca: String?,
    var quantidade: Int,
    var preco: Double,
    var comprado: Boolean = false
)