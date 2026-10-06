package com.example.hemaremobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PossoDoarViewModelTest {

    private lateinit var viewModel: PossoDoarViewModel

    @Before
    fun setUp() {
        viewModel = PossoDoarViewModel()
    }

    private fun avaliar(idade: String, peso: String, respostas: Map<String, Boolean> = emptyMap()): ResultadoTriagem {
        viewModel.alterarIdade(idade)
        viewModel.alterarPeso(peso)
        respostas.forEach { (campo, valor) -> viewModel.responder(campo, valor) }
        viewModel.verResultado()
        return viewModel.uiState.value.resultado!!
    }

    @Test
    fun adultoSemRestricoes_resultadoVerde() {
        val r = avaliar("30", "70")
        assertEquals(NivelResultado.VERDE, r.nivel)
        assertTrue(r.motivos.isEmpty())
    }

    @Test
    fun pesoAbaixoDe50_resultadoVermelho() {
        val r = avaliar("30", "45")
        assertEquals(NivelResultado.VERMELHO, r.nivel)
    }

    @Test
    fun idadeAbaixoDe16_resultadoVermelho() {
        val r = avaliar("15", "60")
        assertEquals(NivelResultado.VERMELHO, r.nivel)
    }

    @Test
    fun idade17_resultadoAmareloPorAutorizacaoDoResponsavel() {
        val r = avaliar("17", "60")
        assertEquals(NivelResultado.AMARELO, r.nivel)
        assertTrue(r.motivos.any { it.contains("responsável") })
    }

    @Test
    fun idade65_resultadoAmareloPorAvaliacaoMedica() {
        val r = avaliar("65", "70")
        assertEquals(NivelResultado.AMARELO, r.nivel)
    }

    @Test
    fun hiv_resultadoVermelhoMesmoComDemaisDadosOk() {
        val r = avaliar("30", "70", mapOf("temHIV" to true))
        assertEquals(NivelResultado.VERMELHO, r.nivel)
    }

    @Test
    fun tatuagemRecente_resultadoAmarelo() {
        val r = avaliar("30", "70", mapOf("tatuagemRecente" to true))
        assertEquals(NivelResultado.AMARELO, r.nivel)
    }

    @Test
    fun impedimentoTemPrioridadeSobreAtencao() {
        val r = avaliar("30", "70", mapOf("tatuagemRecente" to true, "temChagas" to true))
        assertEquals(NivelResultado.VERMELHO, r.nivel)
    }

    @Test
    fun semIdadeOuPeso_naoGeraResultadoEMostraErro() {
        viewModel.alterarIdade("30")
        viewModel.verResultado()
        assertNull(viewModel.uiState.value.resultado)
        assertTrue(viewModel.uiState.value.erro.isNotEmpty())
    }

    @Test
    fun campoIdadeIgnoraLetrasELimitaA120() {
        viewModel.alterarIdade("abc999")
        assertEquals("120", viewModel.uiState.value.idadeTexto)
    }

    @Test
    fun refazerLimpaOFormulario() {
        avaliar("30", "70")
        viewModel.refazer()
        assertEquals(PossoDoarUiState(), viewModel.uiState.value)
    }
}
