package com.example.list_wise.data.model

data class ItemEntity(
    val id: Int = 0,                // ID gerado automaticamente pelo banco
    val nome: String,
    val marca: String? = null,
    val preco: Double = 0.0,
    val quantidade: Int,
    val categoria: String? = null               // Chave estrangeira que liga o item à lista
)