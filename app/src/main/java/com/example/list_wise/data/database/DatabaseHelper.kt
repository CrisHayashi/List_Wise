package com.example.list_wise.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "listwise.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        //tabela de listas
        val createListasTable = """
            CREATE TABLE listas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                dataCriacao TEXT NOT NULL,        -- Novo campo para ordenação
                dataFinalizacao TEXT,
                finalizada INTEGER DEFAULT 0,     -- 0=Desejada, 1=Comprada (ESSENCIAL)
                totalGasto REAL DEFAULT 0.0,
                local TEXT,
                endereco TEXT,
                quantidadeItens INTEGER DEFAULT 0
            );
        """.trimIndent()

        // Tabela de itens é um catálogo. Removido 'quantidade'
        val createItensTable = """
            CREATE TABLE itens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                marca TEXT,
                precoPadrao REAL DEFAULT 0.0,   -- Preço padrão para sugestão
                categoria TEXT
            );
        """.trimIndent()

        // Tabela de ligação com dados específicos da compra, relação lista-itens
        val createListaItensTable = """
            CREATE TABLE lista_itens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                listaId INTEGER NOT NULL,
                itemId INTEGER NOT NULL,
                quantidadeDesejada INTEGER DEFAULT 1,   -- Quantidade que o usuário quer
                precoEstimado REAL DEFAULT 0.0,        -- Preço estimado pelo usuário
                comprado INTEGER DEFAULT 0,            -- 0=Não, 1=Sim (ESSENCIAL)
                precoPago REAL,                        -- Preço real pago (pode ser NULL)
                FOREIGN KEY(listaId) REFERENCES listas(id) ON DELETE CASCADE,
                FOREIGN KEY(itemId) REFERENCES itens(id)
            );
        """.trimIndent()

        db.execSQL(createListasTable)
        db.execSQL(createItensTable)
        db.execSQL(createListaItensTable)

        //Inserindo alguns dados default
        db.execSQL("""
        INSERT INTO itens (nome, marca, precoPadrao, categoria) VALUES
        ('Arroz Branco', 'Tio Joao', 8.90, 'Mercearia'),
        ('Feijão Preto', 'Turquesa', 9.90, 'Mercearia'),
        ('Azeite de Oliva Extra Virgem', 'Galo', 29.90, 'Mercearia'),
        ('Farinha de Mandioca', 'Yoki', 5.90, 'Mercearia'),
        ('Farinha de Trigo', 'Sao Braz', 6.00, 'Mercearia'),
        ('Macarrao Fettuccine', 'Paganini', 13.00, 'Mercearia'),
        ('Café em Pó', 'Pilão', 18.90, 'Mercearia'),
        ('Açúcar Refinado', 'União', 4.50, 'Mercearia'),
        ('Óleo de Soja', 'Soya', 7.50, 'Mercearia'),

        ('Banana', 'Prata', 8.00, 'Frutas e Legumes'),
        ('Maçã', 'Gala', 14.00, 'Frutas e Legumes'),
        ('Abacaxi', 'Pérola', 10.00, 'Frutas e Legumes'),
        ('Laranja', 'Pera', 6.00, 'Frutas e Legumes'),
        ('Limao', 'Taiti', 5.00, 'Frutas e Legumes'),
        ('Mamao', 'Formosa', 7.00, 'Frutas e Legumes'),
        ('Maracuja', '', 18.00, 'Frutas e Legumes'),
        ('Melao', 'Japonês', 8.00, 'Frutas e Legumes'),
        ('Morango', '', 7.00, 'Frutas e Legumes'),
        ('Batata Inglesa', '', 5.00, 'Frutas e Legumes'),
        ('Tomate', 'Cereja', 8.00, 'Frutas e Legumes'),
        ('Cebola', '', 4.00, 'Frutas e Legumes'),

        ('Manteiga', 'Itacolomy', 39.00, 'Frios e Congelados'),
        ('Iogurte', 'Natural', 4.00, 'Frios e Congelados'),
        ('Requeijao', 'Poço de Caldas', 10.00, 'Frios e Congelados'),
        ('Queijo Minas', 'Frescal', 55.00, 'Frios e Congelados'),
        ('Queijo Coalho', 'Faco', 30.00, 'Frios e Congelados'),
        ('Presunto', 'Sadia', 10.00, 'Frios e Congelados'),
        ('Carne Moida', '', 30.00, 'Frios e Congelados'),
        ('Carne Picanha', '', 100.00, 'Frios e Congelados'),
        ('Carne Musculo', '', 45.00, 'Frios e Congelados'),
        ('Carne Suina', 'Pernil sem Osso', 30.00, 'Frios e Congelados'),
        ('Frango Inteiro', 'Natto', 15.00, 'Frios e Congelados'),
        ('Frango Filet Peito', 'Natto', 25.00, 'Frios e Congelados'),

        ('Sabão em pó', 'OMO', 22.00, 'Limpeza e Higiene'),
        ('Detergente', 'Ypê', 2.50, 'Limpeza e Higiene'),
        ('Papel Higiênico', 'Neve', 25.00, 'Limpeza e Higiene'),
        ('Creme Dental', 'Colgate', 6.00, 'Limpeza e Higiene'),
        ('Sabonete', 'Protex', 3.50, 'Bebidas'),

        ('Sal', 'Cisne', 2.00, 'Temperos'),
        ('Pimenta-do-reino', 'Kitano', 4.50, 'Temperos'),

        ('Água mineral', 'Crystal', 2.00, 'Bebidas'),
        ('Suco de uva', 'Aurora', 8.00, 'Bebidas'),
        
        ('Pão de Forma Tradicional', 'Plusvita', 9.00, 'Padaria'),
        ('Pão Francês', '', 2.00, 'Padaria'),
        ('Farinha de Rosca', '', 4.00, 'Padaria'),
        ('Mini Coxinha', '', 6.00, 'Padaria')
        
    """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS lista_itens")
        db.execSQL("DROP TABLE IF EXISTS itens")
        db.execSQL("DROP TABLE IF EXISTS listas")
        onCreate(db)
    }
}
