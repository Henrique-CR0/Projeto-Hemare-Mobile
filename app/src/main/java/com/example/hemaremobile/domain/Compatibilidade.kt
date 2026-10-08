package com.example.hemaremobile.domain

val TIPOS_SANGUINEOS = listOf("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+")

/**
 * Quem pode doar para cada tipo receptor (regra real de compatibilidade ABO/Rh).
 * Mesma tabela do backend do site (backend/regras/compatibilidade.js).
 */
private val COMPATIBILIDADE = mapOf(
    "O-" to listOf("O-"),
    "O+" to listOf("O-", "O+"),
    "A-" to listOf("O-", "A-"),
    "A+" to listOf("O-", "O+", "A-", "A+"),
    "B-" to listOf("O-", "B-"),
    "B+" to listOf("O-", "O+", "B-", "B+"),
    "AB-" to listOf("O-", "A-", "B-", "AB-"),
    "AB+" to listOf("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+")
)

/** Tipos de doador aceitos por um receptor (lista vazia para tipo inválido). */
fun tiposCompativeis(tipoReceptor: String): List<String> = COMPATIBILIDADE[tipoReceptor] ?: emptyList()

fun podeDoar(tipoDoador: String, tipoReceptor: String): Boolean = tipoDoador in tiposCompativeis(tipoReceptor)
