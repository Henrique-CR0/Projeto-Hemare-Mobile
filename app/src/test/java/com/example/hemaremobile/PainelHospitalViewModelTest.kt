package com.example.hemaremobile

import com.example.hemaremobile.domain.Necessidade
import com.example.hemaremobile.domain.TIPOS_SANGUINEOS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PainelHospitalViewModelTest {
    @get:Rule val regra = RegraDispatcherPrincipal()

    // Criado sob demanda: o init usa viewModelScope, que precisa do Dispatchers.Main trocado pela regra.
    private val viewModel by lazy { PainelHospitalViewModel(FakeHospitalRepository(), "hospital@hemare.com") }

    @Test
    fun oNegativoSoRecebeDeONegativo() {
        assertEquals(listOf("O-"), viewModel.tiposCompativeis("O-"))
    }

    @Test
    fun abPositivoRecebeDeTodosOsTipos() {
        assertEquals(TIPOS_SANGUINEOS.toSet(), viewModel.tiposCompativeis("AB+").toSet())
    }

    @Test
    fun tipoInvalidoDevolveListaVazia() {
        assertTrue(viewModel.tiposCompativeis("XYZ").isEmpty())
    }

    @Test
    fun doadoresDoMatchSoTrazTiposPermitidos() {
        viewModel.verMatch(Necessidade(1, "A-", "alerta"))
        val permitidos = viewModel.tiposCompativeis("A-")
        val doadores = viewModel.uiState.value.doadoresMatch
        assertTrue(doadores.isNotEmpty())
        assertTrue(doadores.all { it.tipoSanguineo in permitidos })
    }

    @Test
    fun publicarSemTipoMostraErroENaoCriaNecessidade() {
        viewModel.publicar()
        assertTrue(viewModel.uiState.value.necessidades.isEmpty())
        assertEquals("❌ Escolha o tipo sanguíneo.", viewModel.uiState.value.mensagem)
    }

    @Test
    fun publicarComTipoCriaNecessidadeELimpaSelecao() {
        viewModel.selecionarTipo("O+")
        viewModel.selecionarUrgencia("critico")
        viewModel.publicar()
        val estado = viewModel.uiState.value
        assertEquals(1, estado.necessidades.size)
        assertEquals("O+", estado.necessidades[0].tipoSanguineo)
        assertEquals("critico", estado.necessidades[0].urgencia)
        assertEquals("", estado.tipoSelecionado)
        assertTrue(estado.mensagem.startsWith("✅"))
    }

    @Test
    fun definirEstoqueAtualizaSoOTipoInformado() {
        viewModel.definirEstoque("B-", "emergencia")
        assertEquals(mapOf("B-" to "emergencia"), viewModel.uiState.value.estoque)
    }

    @Test
    fun confirmarDoacaoRegistraNoHistorico() {
        viewModel.verMatch(Necessidade(1, "O-", "alerta"))
        val doador = viewModel.uiState.value.doadoresMatch.first()
        viewModel.confirmarDoacao(doador.id)
        val estado = viewModel.uiState.value
        assertTrue(doador.id in estado.confirmados)
        assertEquals(1, estado.historico.size)
        assertEquals(doador.nome, estado.historico[0].doadorNome)
    }

    @Test
    fun confirmarDoadorInexistenteNaoAlteraEstado() {
        val antes = viewModel.uiState.value
        viewModel.confirmarDoacao(9999)
        assertEquals(antes, viewModel.uiState.value)
    }

    @Test
    fun fecharMatchLimpaNecessidadeEmMatch() {
        viewModel.verMatch(Necessidade(1, "A+", "alerta"))
        viewModel.fecharMatch()
        assertNull(viewModel.uiState.value.necessidadeEmMatch)
        assertTrue(viewModel.uiState.value.doadoresMatch.isEmpty())
    }
}
