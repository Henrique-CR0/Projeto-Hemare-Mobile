package com.example.hemaremobile.data.repository

import com.example.hemaremobile.domain.ContaUsuario
import com.example.hemaremobile.domain.DoacaoConfirmada
import com.example.hemaremobile.domain.DoadorCompativel
import com.example.hemaremobile.domain.Hemocentro
import com.example.hemaremobile.domain.Necessidade
import com.example.hemaremobile.domain.PerfilHospital
import com.example.hemaremobile.domain.Resultado
import com.example.hemaremobile.domain.TipoConta
import kotlinx.coroutines.flow.Flow

/*
 * Contratos da camada de dados. Os ViewModels só conhecem estas interfaces,
 * o que permite testá-los com implementações falsas (ver src/test).
 */

interface AutenticacaoRepository {
    suspend fun entrar(email: String, senha: String): Resultado<ContaUsuario>
    suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        tipo: TipoConta,
        perfil: PerfilHospital? = null
    ): Resultado<ContaUsuario>
    fun sair()
}

interface HemocentroRepository {
    /** Busca por nome, cidade ou estado (busca vazia = todos). */
    suspend fun buscar(termo: String): List<Hemocentro>
}

interface HospitalRepository {
    fun observarEstoque(hospital: String): Flow<Map<String, String>>
    suspend fun definirEstoque(hospital: String, tipo: String, nivel: String)
    fun observarNecessidades(hospital: String): Flow<List<Necessidade>>
    suspend fun publicar(hospital: String, tipo: String, urgencia: String): Resultado<String>
    suspend fun doadoresCompativeis(necessidade: Necessidade): List<DoadorCompativel>
    fun observarConfirmados(hospital: String): Flow<Set<Int>>
    fun observarHistorico(hospital: String): Flow<List<DoacaoConfirmada>>
    suspend fun confirmarDoacao(hospital: String, doador: DoadorCompativel)
}

data class Preferencias(val temaEscuro: Boolean = true, val notificacoesAtivas: Boolean = true)

interface PreferenciasRepository {
    val preferencias: Flow<Preferencias>
    suspend fun definirTemaEscuro(ativo: Boolean)
    suspend fun definirNotificacoes(ativo: Boolean)
}
