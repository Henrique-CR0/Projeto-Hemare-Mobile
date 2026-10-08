package com.example.hemaremobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Conta salva no aparelho (permite entrar sem internet). A senha fica só como hash.
 * Contas de hospital também guardam o perfil da instituição (CNPJ, CNES e endereço).
 */
@Entity(tableName = "contas")
data class ContaEntity(
    @PrimaryKey val email: String,
    val nome: String,
    val senhaHash: String,
    val tipo: String,
    val cnpj: String? = null,
    val cnes: String? = null,
    val cep: String? = null,
    val endereco: String? = null,
    val numero: String? = null,
    val bairro: String? = null,
    val complemento: String? = null,
    val cidade: String? = null,
    val estado: String? = null,
    /** Hospital cadastrado no servidor começa pendente até um administrador aprovar. */
    val aprovado: Boolean = true
)

/** Cache dos hemocentros (vindos da API do Hemare ou dos dados iniciais). */
@Entity(tableName = "hemocentros")
data class HemocentroEntity(
    @PrimaryKey val id: Int,
    val nome: String,
    val cidade: String,
    val estado: String,
    val endereco: String,
    val telefone: String
)

/** Nível de estoque de um tipo sanguíneo, por hospital. */
@Entity(tableName = "estoque", primaryKeys = ["hospitalEmail", "tipoSanguineo"])
data class EstoqueEntity(
    val hospitalEmail: String,
    val tipoSanguineo: String,
    val nivel: String
)

@Entity(tableName = "necessidades")
data class NecessidadeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hospitalEmail: String,
    val tipoSanguineo: String,
    val urgencia: String,
    val criadaEm: Long,
    val idRemoto: Int? = null
)

/** Diretório de doadores usado no match quando o app está sem conexão com o servidor. */
@Entity(tableName = "doadores")
data class DoadorEntity(
    @PrimaryKey val id: Int,
    val nome: String,
    val tipoSanguineo: String,
    val cidade: String,
    val telefone: String,
    val identificado: Boolean
)

/** Histórico de doações confirmadas pelo hospital. */
@Entity(tableName = "doacoes_confirmadas", primaryKeys = ["hospitalEmail", "doadorId"])
data class DoacaoConfirmadaEntity(
    val hospitalEmail: String,
    val doadorId: Int,
    val doadorNome: String,
    val tipoSanguineo: String,
    val confirmadaEm: Long
)
