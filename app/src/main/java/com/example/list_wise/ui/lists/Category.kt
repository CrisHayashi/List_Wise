package com.example.list_wise.ui.lists

data class Category(
    var name: String,
    val items: MutableList<Item>,
    var expanded: Boolean = false
)