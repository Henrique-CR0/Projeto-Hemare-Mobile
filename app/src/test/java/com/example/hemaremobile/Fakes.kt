package com.example.hemaremobile

import com.example.hemaremobile.data.repository.AutenticacaoRepository
import com.example.hemaremobile.data.repository.HemocentroRepository
import com.example.hemaremobile.data.repository.HospitalRepository
import com.example.hemaremobile.data.repository.Preferencias
import com.example.hemaremobile.data.repository.PreferenciasRepository
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Troca o Dispatchers.Main (Android) por um dispatcher de teste, para rodar ViewModels na JVM. */
class RegraDispatcherPrincipal(val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeAutenticacaoRepository : AutenticacaoRepository {
    private class Registro(val conta: ContaUsuario, val senha: String)

    /** Sem contas de exemplo: cada teste cadastra o que precisa (como no app real). */
    private val contas = mutableMapOf<String, Registro>()
    var saiu = false

    fun semear(nome: String, email: String, senha: String, tipo: TipoConta = TipoConta.DOADOR) {
        contas[email] = Registro(ContaUsuario(nome, email, tipo), senha)
    }

    override suspend fun entrar(email: String, senha: String): Resultado<ContaUsuario> {
        if (email.isBlank() || senha.isBlank()) return Resultado.Falha("❌ Preencha email e senha.")
        val registro = contas[email.trim().lowercase()]
        return if (registro != null && registro.senha == senha) {
            Resultado.Sucesso(registro.conta)
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
        val chave = email.trim().lowercase()
        if (contas.containsKey(chave)) return Resultado.Falha("❌ Esse email já está cadastrado.")
        val conta = ContaUsuario(nome, chave, tipo, perfil)
        contas[chave] = Registro(conta, senha)
        return Resultado.Sucesso(conta)
    }

    override fun sair() {
        saiu = true
    }
}

class FakeHemocentroRepository(private val todos: List<Hemocentro>) : HemocentroRepository {
    val buscasFeitas = mutableListOf<String>()

    override suspend fun buscar(termo: String): List<Hemocentro> {
        buscasFeitas += termo
        return todos.filter {
            termo.isBlank() || it.cidade.contains(termo, true) || it.estado.contains(termo, true) || it.nome.contains(termo, true)
        }
    }
}

class FakeHospitalRepository : HospitalRepository {
    private val estoque = MutableStateFlow<Map<String, String>>(emptyMap())
    private val necessidades = MutableStateFlow<List<Necessidade>>(emptyList())
    private val confirmados = MutableStateFlow<Set<Int>>(emptySet())
    private val historico = MutableStateFlow<List<DoacaoConfirmada>>(emptyList())
    private val doadores = listOf(
        DoadorCompativel(1, "Ana", "O-", "São Paulo, SP", "(11) 9999-0000", true),
        DoadorCompativel(2, "Carlos", "O+", "São Paulo, SP", "", false),
        DoadorCompativel(3, "Fernanda", "A+", "Campinas, SP", "(19) 9999-0000", true),
        DoadorCompativel(4, "Bruno", "B+", "Guarulhos, SP", "", false)
    )

    override fun observarEstoque(hospital: String): Flow<Map<String, String>> = estoque
    override suspend fun definirEstoque(hospital: String, tipo: String, nivel: String) {
        estoque.value = estoque.value + (tipo to nivel)
    }

    override fun observarNecessidades(hospital: String): Flow<List<Necessidade>> = necessidades
    override suspend fun publicar(hospital: String, tipo: String, urgencia: String): Resultado<String> {
        if (tipo.isBlank()) return Resultado.Falha("❌ Escolha o tipo sanguíneo.")
        necessidades.value = necessidades.value + Necessidade(necessidades.value.size + 1, tipo, urgencia)
        return Resultado.Sucesso("✅ Necessidade publicada com sucesso.")
    }

    override suspend fun doadoresCompativeis(necessidade: Necessidade): List<DoadorCompativel> =
        doadores.filter { it.tipoSanguineo in tiposCompativeis(necessidade.tipoSanguineo) }

    override fun observarConfirmados(hospital: String): Flow<Set<Int>> = confirmados
    override fun observarHistorico(hospital: String): Flow<List<DoacaoConfirmada>> = historico
    override suspend fun confirmarDoacao(hospital: String, doador: DoadorCompativel) {
        confirmados.value = confirmados.value + doador.id
        historico.value = listOf(DoacaoConfirmada(doador.nome, doador.tipoSanguineo, "01/01 às 10:00")) + historico.value
    }
}

class FakePreferenciasRepository : PreferenciasRepository {
    private val estado = MutableStateFlow(Preferencias())
    override val preferencias: Flow<Preferencias> = estado
    override suspend fun definirTemaEscuro(ativo: Boolean) { estado.value = estado.value.copy(temaEscuro = ativo) }
    override suspend fun definirNotificacoes(ativo: Boolean) { estado.value = estado.value.copy(notificacoesAtivas = ativo) }
}
