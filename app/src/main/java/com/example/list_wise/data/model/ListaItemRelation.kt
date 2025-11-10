package com.example.list_wise.data.model

data class ListaItemRelation(
    val id: Int = 0,
    val listaId: Int,
    val itemId: Int,
    val quantidade: Int = 1,
    val comprado: Boolean = false,    // ESSENCIAL: Status do item
    val precoPago: Double? = null,    // Preço real pago
)