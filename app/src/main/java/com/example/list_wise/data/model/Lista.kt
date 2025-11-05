package com.example.list_wise.data.model

data class Lista(
    val id: Int = 0,                // ID gerado automaticamente pelo banco
    val nome: String,
    val dataFinalizacao: String,   // Pode ser armazenada como texto (ex: "2025-10-29")
    val totalGasto: Double = 0.0,
    val local: String?,           // opcional
    val endereco: String?,        // opcional
    val quantidadeItens: Int      // total de itens na lista
)