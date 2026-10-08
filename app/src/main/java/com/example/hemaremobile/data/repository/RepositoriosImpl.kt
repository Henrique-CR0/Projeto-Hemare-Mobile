package com.example.hemaremobile.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.hemaremobile.data.local.ContaDao
import com.example.hemaremobile.data.local.ContaEntity
import com.example.hemaremobile.data.local.DoacaoConfirmadaEntity
import com.example.hemaremobile.data.local.DoadorDao
import com.example.hemaremobile.data.local.EstoqueEntity
import com.example.hemaremobile.data.local.HashSenha
import com.example.hemaremobile.data.local.HemocentroDao
import com.example.hemaremobile.data.local.HemocentroEntity
import com.example.hemaremobile.data.local.HospitalDao
import com.example.hemaremobile.data.local.NecessidadeEntity
import com.example.hemaremobile.data.remote.CadastroRequest
import com.example.hemaremobile.data.remote.ConfirmarDoacaoRequest
import com.example.hemaremobile.data.remote.EstoqueDto
import com.example.hemaremobile.data.remote.HemareApi
import com.example.hemaremobile.data.remote.LoginRequest
import com.example.hemaremobile.data.remote.NecessidadeRequest
import com.example.hemaremobile.data.remote.PerfilHospitalRequest
import com.example.hemaremobile.data.remote.Sessao
import com.example.hemaremobile.domain.ContaUsuario
import com.example.hemaremobile.domain.DoacaoConfirmada
import com.example.hemaremobile.domain.DoadorCompativel
import com.example.hemaremobile.domain.Hemocentro
import com.example.hemaremobile.domain.Necessidade
import com.example.hemaremobile.domain.PerfilHospital
import com.example.hemaremobile.domain.Resultado
import com.example.hemaremobile.domain.TipoConta
import com.example.hemaremobile.domain.tiposCompativeis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

/*
 * Estratégia "online primeiro, com plano B offline":
 * tenta a API do Hemare; se o servidor não responder, usa o banco local (Room).
 */

/** Executa uma chamada de rede; devolve null se o servidor estiver fora do ar ou recusar. */
internal suspend fun <T> tentarRede(bloco: suspend () -> T): T? = try {
    bloco()
} catch (e: CancellationException) {
    throw e
} catch (e: IOException) {
    null
} catch (e: HttpException) {
    null
}

private fun TipoConta.paraApi(): String = if (this == TipoConta.HOSPITAL) "hospital" else "doador"

private fun tipoDaApi(texto: String): TipoConta? = when (texto.lowercase()) {
    "doador" -> TipoConta.DOADOR
    "hospital" -> TipoConta.HOSPITAL
    else -> null
}

private fun ContaEntity.perfil(): PerfilHospital? = if (cnpj == null) null else PerfilHospital(
    cnpj = cnpj, cnes = cnes.orEmpty(), cep = cep.orEmpty(), endereco = endereco.orEmpty(),
    numero = numero.orEmpty(), bairro = bairro.orEmpty(), complemento = complemento.orEmpty(),
    cidade = cidade.orEmpty(), estado = estado.orEmpty(), aprovado = aprovado
)

private fun ContaEntity.paraModelo() = ContaUsuario(nome, email, TipoConta.valueOf(tipo), perfil())

private fun ContaEntity.comPerfil(p: PerfilHospital?): ContaEntity = if (p == null) this else copy(
    cnpj = p.cnpj, cnes = p.cnes, cep = p.cep, endereco = p.endereco, numero = p.numero,
    bairro = p.bairro, complemento = p.complemento, cidade = p.cidade, estado = p.estado, aprovado = p.aprovado
)

private fun PerfilHospital.paraApi() =
    PerfilHospitalRequest(cnpj, cnes, cep, endereco, numero, bairro, complemento, cidade, estado)

class AutenticacaoRepositoryImpl(
    private val api: HemareApi,
    private val contas: ContaDao,
    private val sessao: Sessao
) : AutenticacaoRepository {

    override suspend fun entrar(email: String, senha: String): Resultado<ContaUsuario> {
        val emailLimpo = email.trim().lowercase()
        if (emailLimpo.isBlank() || senha.isBlank()) return Resultado.Falha("❌ Preencha email e senha.")

        val resposta = tentarRede { api.login(LoginRequest(emailLimpo, senha)) }
        if (resposta != null) {
            val tipo = tipoDaApi(resposta.usuario.tipo)
                ?: return Resultado.Falha("❌ Esse tipo de conta usa só o site do Hemare.")
            sessao.token = resposta.token
            // Guarda a conta no aparelho (mantendo o perfil do hospital) para entrar sem internet depois.
            val hash = HashSenha.gerar(emailLimpo, senha)
            val salva = contas.buscar(emailLimpo)?.copy(nome = resposta.usuario.nome, senhaHash = hash, tipo = tipo.name)
                ?: ContaEntity(emailLimpo, resposta.usuario.nome, hash, tipo.name)
            contas.salvar(salva)
            return Resultado.Sucesso(salva.paraModelo())
        }

        // Servidor fora do ar ou conta só local: confere no banco do aparelho.
        sessao.token = null
        val conta = contas.buscar(emailLimpo)
        return if (conta != null && conta.senhaHash == HashSenha.gerar(emailLimpo, senha)) {
            Resultado.Sucesso(conta.paraModelo())
        } else {
            Resultado.Falha("❌ Email ou senha inválidos.")
        }
    }

    override suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        tipo: TipoConta,
        perfil: PerfilHospital?
    ): Resultado<ContaUsuario> {
        val emailLimpo = email.trim().lowercase()
        val nomeLimpo = nome.trim()
        var perfilFinal = perfil
        sessao.token = null
        try {
            api.cadastrar(CadastroRequest(nomeLimpo, emailLimpo, senha, tipo.paraApi()))
            val token = tentarRede { api.login(LoginRequest(emailLimpo, senha)) }?.token
            sessao.token = token
            if (perfil != null && token != null) {
                tentarRede { api.salvarPerfilHospital("Bearer $token", perfil.paraApi()) }
                // No servidor, todo hospital novo fica pendente até um administrador aprovar.
                perfilFinal = perfil.copy(aprovado = false)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == 409) return Resultado.Falha("❌ Esse email já está cadastrado.")
            if (e.code() == 400) return Resultado.Falha("❌ Dados inválidos. Confira os campos.")
        } catch (e: IOException) {
            // Sem servidor: a conta é criada só no aparelho.
        }
        val conta = ContaEntity(emailLimpo, nomeLimpo, HashSenha.gerar(emailLimpo, senha), tipo.name).comPerfil(perfilFinal)
        contas.salvar(conta)
        return Resultado.Sucesso(conta.paraModelo())
    }

    override fun sair() {
        sessao.token = null
    }
}

class HemocentroRepositoryImpl(
    private val api: HemareApi,
    private val dao: HemocentroDao
) : HemocentroRepository {

    override suspend fun buscar(termo: String): List<Hemocentro> {
        val busca = termo.trim()
        val remotos = tentarRede { api.listarLocais(busca.ifBlank { null }) }
        if (remotos != null) {
            val entidades = remotos.map {
                HemocentroEntity(it.id, it.nome, it.cidade, it.estado, it.endereco.orEmpty(), it.telefone.orEmpty())
            }
            if (busca.isBlank()) dao.salvarTodos(entidades) // atualiza o cache offline
            return entidades.map { it.paraModelo() }
        }
        val locais = if (busca.isBlank()) dao.listar() else dao.buscar("%$busca%")
        return locais.map { it.paraModelo() }
    }

    private fun HemocentroEntity.paraModelo() = Hemocentro(id, nome, cidade, estado, endereco, telefone)
}

class HospitalRepositoryImpl(
    private val api: HemareApi,
    private val dao: HospitalDao,
    private val doadores: DoadorDao,
    private val sessao: Sessao,
    private val relogio: () -> Long = System::currentTimeMillis
) : HospitalRepository {

    /** Ids de doadores que vieram do servidor (só esses são confirmados também na API). */
    private val doadoresRemotos = mutableSetOf<Int>()

    override fun observarEstoque(hospital: String): Flow<Map<String, String>> =
        dao.observarEstoque(hospital).map { lista -> lista.associate { it.tipoSanguineo to it.nivel } }

    override suspend fun definirEstoque(hospital: String, tipo: String, nivel: String) {
        dao.salvarEstoque(EstoqueEntity(hospital, tipo, nivel))
        sessao.cabecalho?.let { token -> tentarRede { api.salvarEstoque(token, EstoqueDto(tipo, nivel)) } }
    }

    override fun observarNecessidades(hospital: String): Flow<List<Necessidade>> =
        dao.observarNecessidades(hospital).map { lista ->
            lista.map { Necessidade(it.id, it.tipoSanguineo, it.urgencia, it.idRemoto) }
        }

    override suspend fun publicar(hospital: String, tipo: String, urgencia: String): Resultado<String> {
        if (tipo.isBlank()) return Resultado.Falha("❌ Escolha o tipo sanguíneo.")

        var mensagem = "✅ Necessidade publicada com sucesso."
        var idRemoto: Int? = null
        val token = sessao.cabecalho
        if (token != null) {
            val enviada = tentarRede { api.publicarNecessidade(token, NecessidadeRequest(tipo, urgencia)) }
            if (enviada != null) {
                idRemoto = tentarRede { api.necessidades(token) }?.firstOrNull { it.tipoSanguineo == tipo }?.id
            } else {
                mensagem = "✅ Necessidade salva no aparelho (servidor indisponível)."
            }
        }
        dao.inserirNecessidade(
            NecessidadeEntity(hospitalEmail = hospital, tipoSanguineo = tipo, urgencia = urgencia, criadaEm = relogio(), idRemoto = idRemoto)
        )
        return Resultado.Sucesso(mensagem)
    }

    override suspend fun doadoresCompativeis(necessidade: Necessidade): List<DoadorCompativel> {
        val token = sessao.cabecalho
        val idRemoto = necessidade.idRemoto
        if (token != null && idRemoto != null) {
            val match = tentarRede { api.match(token, idRemoto) }
            if (match != null) {
                return match.doadores.mapIndexed { i, d ->
                    val id = d.doadorId ?: -(i + 1) // anônimos não têm id público
                    if (d.doadorId != null) doadoresRemotos += id
                    DoadorCompativel(id, d.nome, d.tipoSanguineo, d.cidade.orEmpty(), d.telefone.orEmpty(), d.identificado)
                }
            }
        }
        return doadores.porTipos(tiposCompativeis(necessidade.tipoSanguineo)).map {
            DoadorCompativel(it.id, it.nome, it.tipoSanguineo, it.cidade, it.telefone, it.identificado)
        }
    }

    override fun observarConfirmados(hospital: String): Flow<Set<Int>> =
        dao.observarDoacoes(hospital).map { lista -> lista.map { it.doadorId }.toSet() }

    override fun observarHistorico(hospital: String): Flow<List<DoacaoConfirmada>> =
        dao.observarDoacoes(hospital).map { lista ->
            val formato = SimpleDateFormat("dd/MM 'às' HH:mm", Locale("pt", "BR"))
            lista.map { DoacaoConfirmada(it.doadorNome, it.tipoSanguineo, formato.format(Date(it.confirmadaEm))) }
        }

    override suspend fun confirmarDoacao(hospital: String, doador: DoadorCompativel) {
        dao.confirmarDoacao(DoacaoConfirmadaEntity(hospital, doador.id, doador.nome, doador.tipoSanguineo, relogio()))
        val token = sessao.cabecalho
        if (token != null && doador.id in doadoresRemotos) {
            tentarRede { api.confirmarDoacao(token, ConfirmarDoacaoRequest(doador.id)) }
        }
    }
}

class PreferenciasRepositoryImpl(private val dataStore: DataStore<Preferences>) : PreferenciasRepository {
    private val chaveTema = booleanPreferencesKey("tema_escuro")
    private val chaveNotificacoes = booleanPreferencesKey("notificacoes_ativas")

    override val preferencias: Flow<Preferencias> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { Preferencias(temaEscuro = it[chaveTema] ?: true, notificacoesAtivas = it[chaveNotificacoes] ?: true) }

    override suspend fun definirTemaEscuro(ativo: Boolean) {
        dataStore.edit { it[chaveTema] = ativo }
    }

    override suspend fun definirNotificacoes(ativo: Boolean) {
        dataStore.edit { it[chaveNotificacoes] = ativo }
    }
}
