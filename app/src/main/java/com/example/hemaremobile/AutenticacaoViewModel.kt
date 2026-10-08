package com.example.hemaremobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hemaremobile.data.repository.AutenticacaoRepository
import com.example.hemaremobile.domain.ContaUsuario
import com.example.hemaremobile.domain.PerfilHospital
import com.example.hemaremobile.domain.Resultado
import com.example.hemaremobile.domain.TipoConta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AutenticacaoUiState(
    val contaLogada: ContaUsuario? = null,
    val erro: String = "",
    val carregando: Boolean = false
)

/**
 * Login e cadastro. O repositório tenta a API do Hemare e, sem servidor, usa as contas
 * salvas no banco do aparelho. Sem contas de exemplo: para entrar é preciso se cadastrar
 * primeiro (como doador ou como hospital).
 */
class AutenticacaoViewModel(private val repositorio: AutenticacaoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AutenticacaoUiState())
    val uiState: StateFlow<AutenticacaoUiState> = _uiState.asStateFlow()

    fun entrar(email: String, senha: String) = executar { repositorio.entrar(email, senha) }

    fun cadastrarDoador(nome: String, email: String, senha: String) =
        executar { repositorio.cadastrar(nome, email, senha, TipoConta.DOADOR) }

    fun cadastrarHospital(
        nome: String,
        email: String,
        senha: String,
        cnpj: String,
        cnes: String,
        cep: String,
        endereco: String,
        numero: String,
        bairro: String,
        complemento: String,
        cidade: String,
        estado: String
    ) {
        val perfil = PerfilHospital(
            cnpj = cnpj,
            cnes = cnes,
            cep = cep,
            endereco = endereco,
            numero = numero,
            bairro = bairro,
            complemento = complemento,
            cidade = cidade,
            estado = estado
        )
        executar { repositorio.cadastrar(nome, email, senha, TipoConta.HOSPITAL, perfil) }
    }

    fun sair() {
        repositorio.sair()
        _uiState.value = AutenticacaoUiState()
    }

    fun limparErro() {
        if (_uiState.value.erro.isNotEmpty()) {
            _uiState.update { it.copy(erro = "") }
        }
    }

    private fun executar(acao: suspend () -> Resultado<ContaUsuario>) {
        if (_uiState.value.carregando) return
        _uiState.update { it.copy(carregando = true, erro = "") }
        viewModelScope.launch {
            when (val resultado = acao()) {
                is Resultado.Sucesso -> _uiState.value = AutenticacaoUiState(contaLogada = resultado.valor)
                is Resultado.Falha -> _uiState.update { it.copy(carregando = false, erro = resultado.mensagem) }
            }
        }
    }
}
