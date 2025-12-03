package com.example.list_wise.data.model

data class ListaItemRelation(
    val id: Int = 0,
    val listaId: Int,
    val itemId: Int,
    val quantidadeDesejada: Int = 1,   // Quantidade que o usuário quer comprar
    val precoEstimado: Double = 0.0,  // Preço que o usuário inseriu/estimou
    val comprado: Boolean = false,    // ESSENCIAL: Status do item
    val precoPago: Double? = null,    // Preço real pago
)
