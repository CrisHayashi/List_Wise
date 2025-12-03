package com.example.list_wise.data.model

data class Lista(
    val id: Int = 0,                // ID gerado automaticamente pelo banco
    val nome: String,
    val dataCriacao: String,        // Novo campo para saber quando foi criada
    val dataFinalizacao: String? = null,   // Pode ser nula se estiver 'desejada'
    val finalizada: Boolean = false, // ESSENCIAL: FALSE=Desejada (ativa), TRUE=Finalizada
    val totalGasto: Double = 0.0,
    val local: String? = null,           // opcional
    val endereco: String? = null,        // opcional
    val quantidadeItens: Int = 0
)
