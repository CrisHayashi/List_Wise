package com.example.list_wise.data.repository

import android.content.ContentValues
import com.example.list_wise.data.database.DatabaseHelper
import com.example.list_wise.data.database.getStringOrNull
import com.example.list_wise.data.model.ItemEntity
import com.example.list_wise.data.model.Lista
import com.example.list_wise.data.model.ListaItemRelation
import android.util.Log


// Constantes úteis
private const val TABLE_LISTAS = "listas"
private const val TABLE_ITENS = "itens"
private const val TABLE_LISTA_ITENS = "lista_itens"

private const val DEFAULT_TOP_ITEMS_LIMIT = 5

class ListRepository(private val dbHelper: DatabaseHelper) {

    //Retorna a lista desejada ativa (finalizada = 0), se existir
    fun getListaDesejadaAtiva(): Lista? {
        val db = dbHelper.readableDatabase
        // FILTRO: finalizada = 0 (false)
        val cursor = db.rawQuery("SELECT * FROM $TABLE_LISTAS WHERE finalizada = 0 ORDER BY id DESC LIMIT 1", null)
        var lista: Lista? = null

        if (cursor.moveToFirst()) {
            val idxTotalGasto = cursor.getColumnIndexOrThrow("totalGasto")
            lista = Lista(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                dataCriacao = cursor.getString(cursor.getColumnIndexOrThrow("dataCriacao")),
                dataFinalizacao = cursor.getStringOrNull("dataFinalizacao"),
                finalizada = (cursor.getInt(cursor.getColumnIndexOrThrow("finalizada")) != 0), // Converte INTEGER 0/1 para Boolean
                totalGasto = if (!cursor.isNull(idxTotalGasto)) cursor.getDouble(idxTotalGasto) else 0.0,
                local = cursor.getStringOrNull("local"),
                endereco = cursor.getStringOrNull("endereco"),
                quantidadeItens = cursor.getInt(cursor.getColumnIndexOrThrow("quantidadeItens"))
            )
        }
        cursor.close()
        return lista
    }

    // Obter uma lista específica pelo ID
    fun obterListaPorId(listaId: Int): Lista? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_LISTAS WHERE id = ?",
            arrayOf(listaId.toString())
        )
        var lista: Lista? = null

        if (cursor.moveToFirst()) {
            val idxTotalGasto = cursor.getColumnIndexOrThrow("totalGasto")
            lista = Lista(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                dataCriacao = cursor.getString(cursor.getColumnIndexOrThrow("dataCriacao")),
                dataFinalizacao = cursor.getStringOrNull("dataFinalizacao"),
                finalizada = cursor.getInt(cursor.getColumnIndexOrThrow("finalizada")) != 0,
                totalGasto = if (!cursor.isNull(idxTotalGasto)) cursor.getDouble(idxTotalGasto) else 0.0,
                local = cursor.getStringOrNull("local"),
                endereco = cursor.getStringOrNull("endereco"),
                quantidadeItens = cursor.getInt(cursor.getColumnIndexOrThrow("quantidadeItens"))
            )
        }
        cursor.close()
        return lista
    }

    // Inserir uma nova lista
    fun inserirLista(lista: Lista): Long {
        val db = dbHelper.writableDatabase
        // A REGRA: Se a lista não for finalizada, verificamos se já existe uma ativa.
        if (!lista.finalizada && getListaDesejadaAtiva() != null) {
            // Já existe uma lista desejada ativa -> regra de negócio
            return -1L
        }

        val values = ContentValues().apply {
            put("nome", lista.nome)
            put("dataCriacao", lista.dataCriacao)
            put("dataFinalizacao", lista.dataFinalizacao)
            put("totalGasto", lista.totalGasto)
            put("local", lista.local)
            put("endereco", lista.endereco)
            put("quantidadeItens", lista.quantidadeItens)
            put("finalizada", if (lista.finalizada) 1 else 0)
        }
        return db.insert(TABLE_LISTAS, null, values)
    }

    // Atualizar apenas o nome da lista (para renomear a lista desejada)
    fun atualizarNomeLista(listaId: Int, novoNome: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nome", novoNome)
        }
        return db.update(TABLE_LISTAS, values, "id = ?", arrayOf(listaId.toString()))
    }

    private fun inserirItemCatalogo(item: ItemEntity): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nome", item.nome)
            put("marca", item.marca)
            put("precoPadrao", item.precoPadrao)
            put("categoria", item.categoria)
        }
        return db.insert(TABLE_ITENS, null, values)
    }

    fun inserirItem(item: ItemEntity): Long = inserirItemCatalogo(item)

    // Atualizar apenas a categoria de um item do catálogo
    fun atualizarCategoriaDoItem(itemId: Int, novaCategoria: String?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("categoria", novaCategoria)
        }
        return db.update(TABLE_ITENS, values, "id = ?", arrayOf(itemId.toString()))
    }

    // Atualiza item no catálogo (nome, marca, categoria, preço padrão)
    fun atualizarItemCatalogo(
        itemId: Int,
        novoNome: String,
        novaMarca: String?,
        novaCategoria: String?,
        novoPrecoPadrao: Double
    ): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nome", novoNome)
            put("marca", novaMarca)
            put("categoria", novaCategoria)
            put("precoPadrao", novoPrecoPadrao)
        }
        return db.update(TABLE_ITENS, values, "id = ?", arrayOf(itemId.toString()))
    }

    // Atualiza quantidade e preço estimado na relação lista_itens
    fun atualizarItemDaLista(
        relationId: Int,
        novaQuantidade: Int,
        novoPrecoEstimado: Double
    ): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("quantidadeDesejada", novaQuantidade)
            put("precoEstimado", novoPrecoEstimado)
        }
        return db.update(TABLE_LISTA_ITENS, values, "id = ?", arrayOf(relationId.toString()))
    }

    companion object {
        private const val TAG = "ListaRepository"
    }


    // Deleta a relação lista_itens (item sai da lista)
    fun deletarItemDaLista(relationId: Int): Int {
        val db = dbHelper.writableDatabase

        // Log antes de tentar deletar
        Log.d(TAG, "Tentando deletar item da lista com relationId=$relationId")

        val rowsDeleted = db.delete(
            TABLE_LISTA_ITENS,
            "id = ?",
            arrayOf(relationId.toString())
        )

        // Log depois do delete
        Log.d(TAG, "Resultado delete: rowsDeleted=$rowsDeleted para relationId=$relationId")

        // Se quiser, loga um aviso se nada foi apagado
        if (rowsDeleted == 0) {
            Log.w(TAG, "Nenhum registro deletado. Verifique se o relationId=$relationId existe na tabela.")
        }
        return rowsDeleted
    }



    // Traz TODO o catálogo de itens + info se ele já está na lista ativa
    fun obterCatalogoComStatus(listaId: Int): List<Triple<ItemEntity, ListaItemRelation?, Boolean>> {
        val db = dbHelper.readableDatabase
        val sql = """
        SELECT
            i.id          AS itemId,
            i.nome        AS itemNome,
            i.marca       AS itemMarca,
            i.precoPadrao AS itemPrecoPadrao,
            i.categoria   AS itemCategoria,
            li.id         AS relId,
            li.quantidadeDesejada,
            li.precoEstimado,
            li.comprado
        FROM $TABLE_ITENS i
        LEFT JOIN $TABLE_LISTA_ITENS li
               ON li.itemId = i.id
              AND li.listaId = ?
        ORDER BY i.categoria, i.nome
    """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(listaId.toString()))
        val result = mutableListOf<Triple<ItemEntity, ListaItemRelation?, Boolean>>()

        cursor.use { c ->
            if (!c.moveToFirst()) return result

            val idxItemId        = c.getColumnIndexOrThrow("itemId")
            val idxItemNome      = c.getColumnIndexOrThrow("itemNome")
            val idxItemMarca     = c.getColumnIndexOrThrow("itemMarca")
            val idxItemPreco     = c.getColumnIndexOrThrow("itemPrecoPadrao")
            val idxItemCategoria = c.getColumnIndexOrThrow("itemCategoria")

            val idxRelId         = c.getColumnIndexOrThrow("relId")
            val idxQtd           = c.getColumnIndexOrThrow("quantidadeDesejada")
            val idxPrecoEst      = c.getColumnIndexOrThrow("precoEstimado")
            val idxComprado      = c.getColumnIndexOrThrow("comprado")
            do {
                val itemEntity = ItemEntity(
                    id          = c.getInt(idxItemId),
                    nome        = c.getString(idxItemNome),
                    marca       = if (!c.isNull(idxItemMarca)) c.getString(idxItemMarca) else null,
                    precoPadrao = c.getDouble(idxItemPreco),
                    categoria   = if (!c.isNull(idxItemCategoria)) c.getString(idxItemCategoria) else null
                )

                val temRelacao = !c.isNull(idxRelId)

                val relation = if (temRelacao) {

                    ListaItemRelation(
                        id                  = c.getInt(idxRelId),
                        listaId             = listaId,
                        itemId              = c.getInt(idxItemId),
                        quantidadeDesejada  = c.getInt(idxQtd),
                        precoEstimado       = c.getDouble(idxPrecoEst),
                        comprado            = c.getInt(idxComprado) != 0,
                        precoPago           = null
                    )
                } else {
                    null
                }

                val inLista = temRelacao  // se tem linha em lista_itens, ele pertence à lista

                result.add(Triple(itemEntity, relation, inLista))
            } while (c.moveToNext())
        }

        return result
    }

    // Remove a relação lista-itens (item deixa de pertencer à lista)
    fun removerItemDaLista(relationId: Int) {
        val db = dbHelper.writableDatabase
        db.delete("lista_itens", "id = ?", arrayOf(relationId.toString()))
    }

    // Finaliza a lista atual e migra itens não comprados para nova lista desejada
    fun finalizarListaEMigrar(
        listaId: Int,
        dataFinalizacao: String,
        local: String?,
        endereco: String?,
        totalGasto: Double
    ): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        return try {
            // Atualizar a lista atual para finalizada (Histórico)
            val updateValues = ContentValues().apply {
                put("finalizada", 1) // 1 = Comprada/Finalizada
                put("dataFinalizacao", dataFinalizacao)
                put("local", local)
                put("endereco", endereco)
                put("totalGasto", totalGasto)
            }
            db.update(TABLE_LISTAS, updateValues, "id = ?", arrayOf(listaId.toString()))

            // Criar a Nova Lista Desejada (com itens migrados)
            val novaListaId = inserirLista(
                Lista(
                    nome = "Nova Lista - ${dataFinalizacao.take(10)}", // Nome sugerido
                    dataCriacao = dataFinalizacao,
                    finalizada = false,
                    quantidadeItens = 0 // Será atualizado após a migração
                )
            )

            if (novaListaId != -1L) {
                val novaListaIdInt = novaListaId.toInt()
                // Migrar itens NÃO comprados para a nova lista
                val queryItensNaoComprados = """
                    SELECT itemId, quantidadeDesejada, precoEstimado 
                    FROM $TABLE_LISTA_ITENS 
                    WHERE listaId = ? AND comprado = 0
                    """.trimIndent()

                val cursor = db.rawQuery(queryItensNaoComprados, arrayOf(listaId.toString()))

                var itensMigrados = 0

                while (cursor.moveToNext()) {
                    val itemId = cursor.getInt(0)
                    val qtdDesejada = cursor.getInt(1)
                    val precoEst = cursor.getDouble(2)

                    val relationValues = ContentValues().apply {
                        put("listaId", novaListaIdInt)
                        put("itemId", itemId)
                        put("quantidadeDesejada", qtdDesejada)
                        put("precoEstimado", precoEst)
                        put("comprado", 0) // Sempre não comprado na nova lista
                        putNull("precoPago")
                    }
                    db.insert(TABLE_LISTA_ITENS, null, relationValues)
                    itensMigrados++
                }
                cursor.close()

                // Atualiza a quantidadeItens na nova lista desejada
                val updateNovaLista = ContentValues().apply {
                    put("quantidadeItens", itensMigrados)
                }
                db.update(TABLE_LISTAS, updateNovaLista, "id = ?", arrayOf(novaListaId.toString()))

                db.setTransactionSuccessful()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    // Relacionar item a uma lista (com quantidade, preço estimado)
    fun adicionarItemNaLista(relation: ListaItemRelation): Long {
        val db = dbHelper.writableDatabase
        // 1) Verifica se já existe uma relação listaId + itemId
        val cursor = db.rawQuery(
            "SELECT id, quantidadeDesejada FROM $TABLE_LISTA_ITENS WHERE listaId = ? AND itemId = ?",
            arrayOf(relation.listaId.toString(), relation.itemId.toString())
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                // Já existe: só atualiza a quantidade (soma)
                val relationId = c.getInt(0)
                val quantidadeAtual = c.getInt(1)

                val values = ContentValues().apply {
                    put("quantidadeDesejada", quantidadeAtual + relation.quantidadeDesejada)
                    put("precoEstimado", relation.precoEstimado)
                    // mantemos os outros campos como estavam (comprado, precoPago)
                }

                db.update(TABLE_LISTA_ITENS, values, "id = ?", arrayOf(relationId.toString()))
                return relationId.toLong()
            }
        }

        // 2) Não existia: insere normal
        val valuesInsert = ContentValues().apply {
            put("listaId", relation.listaId)
            put("itemId", relation.itemId)
            put("quantidadeDesejada", relation.quantidadeDesejada)
            put("precoEstimado", relation.precoEstimado)
            put("comprado", if (relation.comprado) 1 else 0)
            if (relation.precoPago != null) put("precoPago", relation.precoPago) else putNull("precoPago")
        }

        return db.insert(TABLE_LISTA_ITENS, null, valuesInsert)
    }

    // Atualiza status/preço pago do item durante a compra (RF004)
    fun marcarItemComoComprado(relationId: Int, comprado: Boolean, precoPago: Double?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("comprado", if (comprado) 1 else 0)
            if (precoPago != null) put("precoPago", precoPago) else putNull("precoPago")
            }
        return db.update(TABLE_LISTA_ITENS, values, "id = ?", arrayOf(relationId.toString()))
    }

    // Itens que pertencem a uma lista específica
    fun obterItensDaLista(listaId: Int): List<Triple<ItemEntity, ListaItemRelation, Boolean>> {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT i.id, i.nome, i.marca, i.precoPadrao, i.categoria,
                   li.id as relationId, li.quantidadeDesejada, li.precoEstimado, li.comprado, li.precoPago
            FROM $TABLE_ITENS i
            INNER JOIN $TABLE_LISTA_ITENS li ON i.id = li.itemId
            WHERE li.listaId = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(listaId.toString()))
        val itensComRelacao = mutableListOf<Triple<ItemEntity, ListaItemRelation, Boolean>>()

        while (cursor.moveToNext()) {
            val itemEntity = ItemEntity(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                marca = cursor.getStringOrNull("marca"),
                precoPadrao = cursor.getDouble(cursor.getColumnIndexOrThrow("precoPadrao")),
                categoria = cursor.getStringOrNull("categoria")
            )

            val relationId = cursor.getInt(cursor.getColumnIndexOrThrow("relationId"))
            val quantidadeDesejada =
                cursor.getInt(cursor.getColumnIndexOrThrow("quantidadeDesejada"))
            val precoEstimado = cursor.getDouble(cursor.getColumnIndexOrThrow("precoEstimado"))
            val comprado = cursor.getInt(cursor.getColumnIndexOrThrow("comprado")) != 0

            val precoPago = if (!cursor.isNull(cursor.getColumnIndexOrThrow("precoPago"))) {
                cursor.getDouble(cursor.getColumnIndexOrThrow("precoPago"))
        } else {
            null
        }

            val relation = ListaItemRelation(
                id = relationId,
                listaId = listaId,
                itemId = itemEntity.id,
                quantidadeDesejada = quantidadeDesejada,
                precoEstimado = precoEstimado,
                comprado = comprado,
                precoPago = precoPago
            )
            itensComRelacao.add(Triple(itemEntity, relation, relation.comprado))
        }

        cursor.close()
        return itensComRelacao
    }

    // Obter todas as listas finalizadas (Histórico)
    fun obterTodasListasFinalizadas(): List<Lista> {
        val db = dbHelper.readableDatabase
        // FILTRO: finalizada = 1
        val cursor = db.rawQuery("SELECT * FROM $TABLE_LISTAS WHERE finalizada = 1 ORDER BY dataFinalizacao DESC", null)
        val listas = mutableListOf<Lista>()

        while (cursor.moveToNext()) {
            val idxTotal = cursor.getColumnIndexOrThrow("totalGasto")
            val lista = Lista(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                dataCriacao = cursor.getString(cursor.getColumnIndexOrThrow("dataCriacao")),
                dataFinalizacao = cursor.getStringOrNull("dataFinalizacao"),
                finalizada = (cursor.getInt(cursor.getColumnIndexOrThrow("finalizada")) != 0),
                totalGasto = if (!cursor.isNull(idxTotal)) cursor.getDouble(idxTotal) else 0.0,
                local = cursor.getStringOrNull("local"),
                endereco = cursor.getStringOrNull("endereco"),
                quantidadeItens = cursor.getInt(cursor.getColumnIndexOrThrow("quantidadeItens"))
            )
            listas.add(lista)
        }
        cursor.close()
        return listas
    }

    // Calcular total gasto em uma lista (Usado para estimativa ou histórico)
    fun calcularTotalGastoDaLista(listaId: Int, isHistorico: Boolean = false): Double {
        val db = dbHelper.readableDatabase
        // Se for histórico, usa o precoPago (se não nulo). Se não, usa o precoEstimado.
        val precoCampo = if (isHistorico) {
            "IFNULL(li.precoPago, li.precoEstimado)"
        } else {
            "li.precoEstimado"
        }
        val whereComprado = if (isHistorico) "AND li.comprado = 1" else ""

        val query = """
            SELECT SUM($precoCampo * li.quantidadeDesejada) 
            FROM $TABLE_LISTA_ITENS li 
            WHERE li.listaId = ?$whereComprado
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(listaId.toString()))
        val total = if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        cursor.close()
        return total
    }

    // Obter os itens mais comprados (por frequência de uso)
    fun obterItensMaisComprados(limit: Int = DEFAULT_TOP_ITEMS_LIMIT): List<String> {
        val db = dbHelper.readableDatabase
        // Frequência baseada em ITENS MARCADOS COMO COMPRADOS (comprado = 1) no histórico.
        val query = """
            SELECT i.nome, COUNT(li.itemId) as total_compras
            FROM $TABLE_LISTA_ITENS li
            INNER JOIN $TABLE_ITENS i ON i.id = li.itemId
            WHERE li.comprado = 1
            GROUP BY li.itemId
            ORDER BY total_compras DESC
            LIMIT ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(limit.toString()))
        val itens = mutableListOf<String>()
        while (cursor.moveToNext()) {
            itens.add(cursor.getString(0))
        }
        cursor.close()
        return itens
    }

    // Obter todos os itens do catálogo
    fun obterTodosItensCatalogo(): List<ItemEntity> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, nome, marca, precoPadrao, categoria FROM $TABLE_ITENS", null)
        val itens = mutableListOf<ItemEntity>()

        while (cursor.moveToNext()) {
            val item = ItemEntity(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                marca = cursor.getStringOrNull("marca"),
                precoPadrao = cursor.getDouble(cursor.getColumnIndexOrThrow("precoPadrao")),
                categoria = cursor.getStringOrNull("categoria")
            )
            itens.add(item)
        }

        cursor.close()
        return itens
    }
}
