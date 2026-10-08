package com.example.hemaremobile

import com.example.hemaremobile.domain.Hemocentro
import com.example.hemaremobile.domain.NivelResultado
import com.example.hemaremobile.domain.TipoConta
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AutenticacaoViewModelTest {
    @get:Rule val regra = RegraDispatcherPrincipal()

    private val repositorio = FakeAutenticacaoRepository()
    private val viewModel by lazy { AutenticacaoViewModel(repositorio) }

    @Test
    fun `login correto abre a conta`() {
        viewModel.entrar("doador@hemare.com", "doador123")
        val conta = viewModel.uiState.value.contaLogada
        assertEquals("Maria Doadora", conta?.nome)
        assertEquals(TipoConta.DOADOR, conta?.tipo)
        assertEquals("", viewModel.uiState.value.erro)
    }

    @Test
    fun `senha errada mostra erro e nao entra`() {
        viewModel.entrar("doador@hemare.com", "errada")
        assertNull(viewModel.uiState.value.contaLogada)
        assertEquals("❌ Email ou senha inválidos.", viewModel.uiState.value.erro)
        assertFalse(viewModel.uiState.value.carregando)
    }

    @Test
    fun `limparErro apaga a mensagem`() {
        viewModel.entrar("", "")
        assertTrue(viewModel.uiState.value.erro.isNotEmpty())
        viewModel.limparErro()
        assertEquals("", viewModel.uiState.value.erro)
    }

    @Test
    fun `cadastro de hospital entra como hospital`() {
        viewModel.cadastrarHospital("Hospital Teste", "novo@hospital.com", "senha1234")
        assertEquals(TipoConta.HOSPITAL, viewModel.uiState.value.contaLogada?.tipo)
    }

    @Test
    fun `cadastro com email repetido falha`() {
        viewModel.cadastrarDoador("Outra", "doador@hemare.com", "senha1234")
        assertNull(viewModel.uiState.value.contaLogada)
        assertEquals("❌ Esse email já está cadastrado.", viewModel.uiState.value.erro)
    }

    @Test
    fun `sair limpa a sessao`() {
        viewModel.entrar("doador@hemare.com", "doador123")
        viewModel.sair()
        assertNull(viewModel.uiState.value.contaLogada)
        assertTrue(repositorio.saiu)
    }
}

class PossoDoarViewModelTest {
    private val viewModel = PossoDoarViewModel()

    @Test
    fun `idade e peso aceitam so numeros e tem limite`() {
        viewModel.alterarIdade("2a5")
        assertEquals("25", viewModel.uiState.value.idadeTexto)
        viewModel.alterarIdade("999")
        assertEquals("120", viewModel.uiState.value.idadeTexto)
        viewModel.alterarPeso("abc")
        assertEquals("", viewModel.uiState.value.pesoTexto)
    }

    @Test
    fun `sem idade e peso pede para preencher`() {
        viewModel.verResultado()
        assertNull(viewModel.uiState.value.resultado)
        assertTrue(viewModel.uiState.value.erro.isNotEmpty())
    }

    @Test
    fun `respostas geram resultado e refazer limpa tudo`() {
        viewModel.alterarIdade("25")
        viewModel.alterarPeso("70")
        viewModel.responder("tatuagemRecente", true)
        viewModel.verResultado()
        assertEquals(NivelResultado.AMARELO, viewModel.uiState.value.resultado?.nivel)

        viewModel.refazer()
        assertNull(viewModel.uiState.value.resultado)
        assertEquals("", viewModel.uiState.value.idadeTexto)
    }

    @Test
    fun `aviso de peso aparece em tempo real`() {
        viewModel.alterarPeso("45")
        assertEquals("Peso mínimo para doação: 50 kg.", viewModel.uiState.value.avisoPeso)
    }
}

class PainelHospitalViewModelTest {
    @get:Rule val regra = RegraDispatcherPrincipal()

    // Criado sob demanda: o init usa viewModelScope, que precisa do Dispatchers.Main trocado pela regra.
    private val viewModel by lazy { PainelHospitalViewModel(FakeHospitalRepository(), "hospital@hemare.com") }

    @Test
    fun `publicar sem tipo mostra erro`() {
        viewModel.publicar()
        assertEquals("❌ Escolha o tipo sanguíneo.", viewModel.uiState.value.mensagem)
        assertTrue(viewModel.uiState.value.necessidades.isEmpty())
    }

    @Test
    fun `publicar adiciona necessidade e limpa o tipo`() {
        viewModel.selecionarTipo("A+")
        viewModel.selecionarUrgencia("critico")
        viewModel.publicar()
        val estado = viewModel.uiState.value
        assertEquals(1, estado.necessidades.size)
        assertEquals("critico", estado.necessidades.first().urgencia)
        assertEquals("", estado.tipoSelecionado)
        assertTrue(estado.mensagem.startsWith("✅"))
    }

    @Test
    fun `match mostra so doadores compativeis`() {
        viewModel.selecionarTipo("A+")
        viewModel.publicar()
        viewModel.verMatch(viewModel.uiState.value.necessidades.first())
        val estado = viewModel.uiState.value
        assertEquals(listOf("O-", "O+", "A-", "A+"), estado.tiposCompativeisMatch)
        assertEquals(listOf("Ana", "Carlos", "Fernanda"), estado.doadoresMatch.map { it.nome })

        viewModel.fecharMatch()
        assertNull(viewModel.uiState.value.necessidadeEmMatch)
    }

    @Test
    fun `estoque e confirmacao de doacao`() {
        viewModel.definirEstoque("O-", "critico")
        viewModel.confirmarDoacao(1)
        assertEquals("critico", viewModel.uiState.value.estoque["O-"])
        assertTrue(1 in viewModel.uiState.value.confirmados)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OndeDoarViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @get:Rule val regra = RegraDispatcherPrincipal(dispatcher)

    private val repositorio = FakeHemocentroRepository(
        listOf(
            Hemocentro(1, "Fundação Hemope", "Recife", "PE", "R. Joaquim Nabuco, 171", "(81) 3182-4600"),
            Hemocentro(2, "HEMORIO", "Rio de Janeiro", "RJ", "R. Frei Caneca, 8", "(21) 3916-8300")
        )
    )

    @Test
    fun `carrega todos ao abrir e filtra pela busca`() = runTest(dispatcher) {
        val viewModel = OndeDoarViewModel(repositorio)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.hemocentros.size)
        assertFalse(viewModel.uiState.value.carregando)

        viewModel.alterarBusca("recife")
        advanceUntilIdle()
        assertEquals(listOf("Fundação Hemope"), viewModel.uiState.value.hemocentros.map { it.nome })
    }

    @Test
    fun `espera parar de digitar antes de buscar`() = runTest(dispatcher) {
        val viewModel = OndeDoarViewModel(repositorio)
        advanceUntilIdle()
        viewModel.alterarBusca("r")
        viewModel.alterarBusca("re")
        viewModel.alterarBusca("rec")
        advanceTimeBy(OndeDoarViewModel.ESPERA_DIGITACAO_MS + 1)
        advanceUntilIdle()
        assertEquals(listOf("", "rec"), repositorio.buscasFeitas)
    }
}

class ConfiguracaoViewModelTest {
    @get:Rule val regra = RegraDispatcherPrincipal()

    @Test
    fun `tema escuro comeca ligado e pode ser desligado`() {
        val viewModel = ConfiguracaoViewModel(FakePreferenciasRepository())
        assertTrue(viewModel.uiState.value.temaEscuro)
        viewModel.alternarTemaEscuro(false)
        viewModel.alternarNotificacoes(false)
        assertFalse(viewModel.uiState.value.temaEscuro)
        assertFalse(viewModel.uiState.value.notificacoesAtivas)
    }
}
