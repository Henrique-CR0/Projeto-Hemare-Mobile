package com.example.hemaremobile

import com.example.hemaremobile.domain.TipoConta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AutenticacaoViewModelTest {
    @get:Rule val regra = RegraDispatcherPrincipal()

    private val repositorio = FakeAutenticacaoRepository()

    // Criado sob demanda: viewModelScope precisa do Dispatchers.Main trocado pela regra.
    private val viewModel by lazy { AutenticacaoViewModel(repositorio) }

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
        assertFalse(viewModel.uiState.value.carregando)
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
    fun cadastroComEmailRepetidoFalha() {
        repositorio.semear("Maria", "maria@teste.com", "Senha123")
        viewModel.cadastrarDoador("Outra", "maria@teste.com", "Senha456")
        assertNull(viewModel.uiState.value.contaLogada)
        assertEquals("❌ Esse email já está cadastrado.", viewModel.uiState.value.erro)
    }

    @Test
    fun sairEncerraASessao() {
        cadastrarHospitalPadrao()
        viewModel.sair()
        assertNull(viewModel.uiState.value.contaLogada)
        assertTrue(repositorio.saiu)
    }

    @Test
    fun limparErroApagaMensagem() {
        viewModel.entrar("", "")
        assertTrue(viewModel.uiState.value.erro.isNotEmpty())
        viewModel.limparErro()
        assertEquals("", viewModel.uiState.value.erro)
    }
}
