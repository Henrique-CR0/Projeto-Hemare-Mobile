package com.example.hemaremobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AutenticacaoViewModelTest {

    private lateinit var viewModel: AutenticacaoViewModel

    @Before
    fun setUp() {
        viewModel = AutenticacaoViewModel()
    }

    private fun cadastrarHospitalPadrao() {
        viewModel.cadastrarHospital(
            nome = "Hospital Teste", email = "hosp@teste.com", senha = "Senha123",
            cnpj = "11222333000181", cnes = "1234567", cep = "50000-000",
            endereco = "Rua A", numero = "10", bairro = "Boa Vista",
            complemento = "", cidade = "Recife", estado = "PE"
        )
    }

    @Test
    fun semCadastroNaoExisteContaDeExemplo() {
        viewModel.entrar("doador@hemare.com", "doador123")
        assertNull(viewModel.uiState.value.contaLogada)
        assertTrue(viewModel.uiState.value.erro.isNotEmpty())
    }

    @Test
    fun loginComCamposVaziosMostraErro() {
        viewModel.entrar("", "")
        assertEquals("❌ Preencha email e senha.", viewModel.uiState.value.erro)
    }

    @Test
    fun cadastroDeDoadorJaDeixaLogado() {
        viewModel.cadastrarDoador("Ana", "ana@teste.com", "Senha123")
        assertEquals(TipoConta.DOADOR, viewModel.uiState.value.contaLogada?.tipo)
    }

    @Test
    fun loginAposCadastroIgnoraMaiusculasNoEmail() {
        viewModel.cadastrarDoador("Ana", "ana@teste.com", "Senha123")
        viewModel.sair()
        viewModel.entrar("  ANA@Teste.com ", "Senha123")
        assertEquals("Ana", viewModel.uiState.value.contaLogada?.nome)
    }

    @Test
    fun senhaErradaNaoLoga() {
        viewModel.cadastrarDoador("Ana", "ana@teste.com", "Senha123")
        viewModel.sair()
        viewModel.entrar("ana@teste.com", "outra")
        assertNull(viewModel.uiState.value.contaLogada)
        assertEquals("❌ Email ou senha inválidos.", viewModel.uiState.value.erro)
    }

    @Test
    fun cadastroDeHospitalGuardaPerfilDaInstituicao() {
        cadastrarHospitalPadrao()
        val conta = viewModel.uiState.value.contaLogada
        assertEquals(TipoConta.HOSPITAL, conta?.tipo)
        assertNotNull(conta?.perfilHospital)
        assertEquals("11222333000181", conta?.perfilHospital?.cnpj)
        assertEquals("Recife", conta?.perfilHospital?.cidade)
    }

    @Test
    fun recadastroComMesmoEmailSubstituiAContaAnterior() {
        viewModel.cadastrarDoador("Ana", "ana@teste.com", "Senha123")
        viewModel.cadastrarDoador("Ana Nova", "ana@teste.com", "Nova456")
        viewModel.sair()
        viewModel.entrar("ana@teste.com", "Senha123")
        assertNull(viewModel.uiState.value.contaLogada)
        viewModel.entrar("ana@teste.com", "Nova456")
        assertEquals("Ana Nova", viewModel.uiState.value.contaLogada?.nome)
    }

    @Test
    fun sairEncerraASessao() {
        cadastrarHospitalPadrao()
        viewModel.sair()
        assertNull(viewModel.uiState.value.contaLogada)
    }

    @Test
    fun limparErroApagaMensagem() {
        viewModel.entrar("", "")
        viewModel.limparErro()
        assertEquals("", viewModel.uiState.value.erro)
    }
}
