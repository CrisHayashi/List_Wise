package com.example.list_wise.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.list_wise.data.database.DatabaseHelper
import com.example.list_wise.data.database.getStringOrNull
import com.example.list_wise.data.model.ItemEntity
import com.example.list_wise.data.model.Lista
import com.example.list_wise.data.model.ListaItemRelation

class ListRepository(private val dbHelper: DatabaseHelper) {

    // Obter todas as listas desejadas (não finalizadas)
    fun obterTodasListasDesejadas(): List<Lista> {
        val db = dbHelper.readableDatabase
        // FILTRO: Apenas listas onde 'finalizada' é 0
        val cursor = db.rawQuery("SELECT id, nome, dataFinalizacao, totalGasto, local, endereco, quantidadeItens, 0 AS finalizada FROM listas WHERE finalizada = 0 ORDER BY id DESC", null)
        val listas = mutableListOf<Lista>()

        while (cursor.moveToNext()) {
            val lista = Lista(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                dataFinalizacao = cursor.getStringOrNull("dataFinalizacao"), // Usando Extension
                totalGasto = cursor.getDouble(cursor.getColumnIndexOrThrow("totalGasto")),
                local = cursor.getStringOrNull("local"),
                endereco = cursor.getStringOrNull("endereco"),
                quantidadeItens = cursor.getInt(cursor.getColumnIndexOrThrow("quantidadeItens")),
                finalizada = false, // Filtro garante que é 0
                dataCriacao = ""
            )
            listas.add(lista)
        }
        cursor.close()
        return listas
    }

    // Inserir uma nova lista
    fun inserirLista(lista: Lista): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nome", lista.nome)
            put("dataFinalizacao", lista.dataFinalizacao)
            put("totalGasto", lista.totalGasto)
            put("local", lista.local)
            put("endereco", lista.endereco)
            put("quantidadeItens", lista.quantidadeItens)
            put("finalizada", lista.finalizada)
        }
        return db.insert("listas", null, values)
    }

    // Inserir um novo item (caso ainda não exista)
    fun inserirItem(item: ItemEntity): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nome", item.nome)
            put("marca", item.marca)
            put("preco", item.preco)
            put("categoria", item.categoria)
        }
        return db.insert("itens", null, values)
    }

    // Relacionar item a uma lista (com quantidade)
    fun adicionarItemNaLista(relation: ListaItemRelation): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("listaId", relation.listaId)
            put("itemId", relation.itemId)
            put("quantidade", relation.quantidade)
        }
        return db.insert("lista_itens", null, values)
    }

    // Obter todos os itens de uma lista
    fun obterItensDaLista(listaId: Int): List<Pair<ItemEntity, Int>> {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT i.id, i.nome, i.marca, i.preco, i.categoria, li.quantidade, li.comprado_na_lista
            FROM itens i
            INNER JOIN lista_itens li ON i.id = li.itemId
            WHERE li.listaId = ?
        """
        val cursor = db.rawQuery(query, arrayOf(listaId.toString()))
        val itens = mutableListOf<Pair<ItemEntity, Int>>()

        while (cursor.moveToNext()) {
            val item = ItemEntity(
                id = cursor.getInt(0),
                nome = cursor.getString(1),
                marca = cursor.getString(2),
                preco = cursor.getDouble(3),
                categoria = cursor.getString(4),
                quantidade = cursor.getInt(5)
            )
            val quantidade = cursor.getInt(5)
            itens.add(item to quantidade)
        }

        cursor.close()
        return itens
    }

    // Calcular total gasto em uma lista
    fun calcularTotalGastoDaLista(listaId: Int): Double {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT SUM(i.preco * li.quantidade)
            FROM itens i
            INNER JOIN lista_itens li ON i.id = li.itemId
            WHERE li.listaId = ?
        """
        val cursor = db.rawQuery(query, arrayOf(listaId.toString()))
        val total = if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        cursor.close()
        return total
    }

    // Obter os itens mais comprados (por frequência de uso)
    fun obterItensMaisComprados(limit: Int = 5): List<String> {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT i.nome, COUNT(*) as total
            FROM lista_itens li
            INNER JOIN itens i ON i.id = li.itemId
            GROUP BY li.itemId
            ORDER BY total DESC
            LIMIT ?
        """
        val cursor = db.rawQuery(query, arrayOf(limit.toString()))
        val itens = mutableListOf<String>()
        while (cursor.moveToNext()) {
            itens.add(cursor.getString(0))
        }
        cursor.close()
        return itens
    }

    fun obterTodosItens(): List<ItemEntity> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, nome, marca, preco, categoria, quantidade FROM itens", null)
        val itens = mutableListOf<ItemEntity>()

        while (cursor.moveToNext()) {
            val item = ItemEntity(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                marca = cursor.getString(cursor.getColumnIndexOrThrow("marca")),
                preco = cursor.getDouble(cursor.getColumnIndexOrThrow("preco")),
                categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
                quantidade = cursor.getInt(cursor.getColumnIndexOrThrow("quantidade"))
            )
            itens.add(item)
        }

        cursor.close()
        return itens
    }

    fun obterUltimaLista(): Lista? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM listas ORDER BY id DESC LIMIT 1", null)

        val lista = if (cursor.moveToFirst()) {
            Lista(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                dataFinalizacao = cursor.getString(cursor.getColumnIndexOrThrow("dataFinalizacao")),
                totalGasto = cursor.getDouble(cursor.getColumnIndexOrThrow("totalGasto")),
                local = if (!cursor.isNull(cursor.getColumnIndexOrThrow("local")))
                    cursor.getString(cursor.getColumnIndexOrThrow("local")) else null,
                endereco = if (!cursor.isNull(cursor.getColumnIndexOrThrow("endereco")))
                    cursor.getString(cursor.getColumnIndexOrThrow("endereco")) else null,
                quantidadeItens = cursor.getInt(cursor.getColumnIndexOrThrow("quantidadeItens")),
                dataCriacao = ""
            )
        } else null

        cursor.close()
        return lista
    }

    fun obterTodasListasFinalizadas(): List<Lista> {
        val db = dbHelper.readableDatabase
        val query = """
        SELECT l.id, l.nome, l.dataFinalizacao, l.totalGasto, l.local, l.endereco,
               COUNT(li.itemId) AS quantidadeItens
        FROM listas l
        LEFT JOIN lista_itens li ON l.id = li.listaId
        GROUP BY l.id
        ORDER BY l.dataFinalizacao DESC
    """
        val cursor = db.rawQuery(query, null)
        val listas = mutableListOf<Lista>()

        while (cursor.moveToNext()) {
            val lista = Lista(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                dataFinalizacao = cursor.getString(cursor.getColumnIndexOrThrow("dataFinalizacao")),
                totalGasto = cursor.getDouble(cursor.getColumnIndexOrThrow("totalGasto")),
                local = if (!cursor.isNull(cursor.getColumnIndexOrThrow("local")))
                    cursor.getString(cursor.getColumnIndexOrThrow("local")) else null,
                endereco = if (!cursor.isNull(cursor.getColumnIndexOrThrow("endereco")))
                    cursor.getString(cursor.getColumnIndexOrThrow("endereco")) else null,
                quantidadeItens = cursor.getInt(cursor.getColumnIndexOrThrow("quantidadeItens")),
                dataCriacao = ""
            )
            listas.add(lista)
        }

        cursor.close()
        return listas
    }
}
