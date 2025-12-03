package com.example.list_wise.data.model

import com.example.list_wise.ui.lists.Item

fun ItemEntity.toUI(quantidade: Int): Item = Item(
    nome = this.nome,
    marca = this.marca ?: "",
    quantidade = quantidade,
    preco = this.precoPadrao
)

//fun Item.toEntity(): ItemEntity = ItemEntity(
    //nome = this.nome,
    //marca = this.marca,
    //preco = this.preco, // categoria pode ser definida à parte
    //quantidade = this.quantidade
//)

