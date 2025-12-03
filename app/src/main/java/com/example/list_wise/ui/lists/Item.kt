package com.example.list_wise.ui.lists

data class Item (
    val id: Int = 0, // ID do Item (Catálogo)
    val relationId: Int = 0, // [IMPORTANTE] ID da tabela lista_itens para updates
    var nome: String,
    var marca: String? = null,
    var quantidade: Int,
    var preco: Double,
    var isSelected: Boolean = false, // Representa 'comprado'
    var categoria: String? = null    // categoria do item (ex: Mercearia, Frios)
)
