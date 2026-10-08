package com.example.hemaremobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hemaremobile.data.repository.HospitalRepository
import com.example.hemaremobile.domain.DoacaoConfirmada
import com.example.hemaremobile.domain.DoadorCompativel
import com.example.hemaremobile.domain.Necessidade
import com.example.hemaremobile.domain.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PainelHospitalUiState(
    val estoque: Map<String, String> = emptyMap(),
    val necessidades: List<Necessidade> = emptyList(),
    val tipoSelecionado: String = "",
    val urgenciaSelecionada: String = "alerta",
    val mensagem: String = "",
    val necessidadeEmMatch: Necessidade? = null,
    val doadoresMatch: List<DoadorCompativel> = emptyList(),
    val confirmados: Set<Int> = emptySet(),
    val historico: List<DoacaoConfirmada> = emptyList()
)

/**
 * Painel do hospital (hub, estoque/necessidades, histórico e plano compartilham esta instância).
 * Estoque, necessidades e doações confirmadas ficam no banco do aparelho e são enviados
 * para a API do Hemare quando há sessão com o servidor.
 */
class PainelHospitalViewModel(
    private val repositorio: HospitalRepository,
    private val hospital: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PainelHospitalUiState())
    val uiState: StateFlow<PainelHospitalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repositorio.observarEstoque(hospital).collect { estoque -> _uiState.update { it.copy(estoque = estoque) } }
        }
        viewModelScope.launch {
            repositorio.observarNecessidades(hospital).collect { lista -> _uiState.update { it.copy(necessidades = lista) } }
        }
        viewModelScope.launch {
            repositorio.observarConfirmados(hospital).collect { ids -> _uiState.update { it.copy(confirmados = ids) } }
        }
        viewModelScope.launch {
            repositorio.observarHistorico(hospital).collect { lista -> _uiState.update { it.copy(historico = lista) } }
        }
    }

    fun definirEstoque(tipo: String, nivel: String) {
        _uiState.update { it.copy(estoque = it.estoque + (tipo to nivel)) }
        viewModelScope.launch { repositorio.definirEstoque(hospital, tipo, nivel) }
    }

    fun selecionarTipo(tipo: String) {
        _uiState.update { it.copy(tipoSelecionado = tipo) }
    }

    fun selecionarUrgencia(urgencia: String) {
        _uiState.update { it.copy(urgenciaSelecionada = urgencia) }
    }

    fun publicar() {
        val estado = _uiState.value
        viewModelScope.launch {
            when (val r = repositorio.publicar(hospital, estado.tipoSelecionado, estado.urgenciaSelecionada)) {
                is Resultado.Sucesso -> _uiState.update { it.copy(tipoSelecionado = "", mensagem = r.valor) }
                is Resultado.Falha -> _uiState.update { it.copy(mensagem = r.mensagem) }
            }
        }
    }

    /** Abre o match: busca os doadores compatíveis (API do Hemare ou diretório do aparelho). */
    fun verMatch(necessidade: Necessidade) {
        _uiState.update { it.copy(necessidadeEmMatch = necessidade, doadoresMatch = emptyList()) }
        viewModelScope.launch {
            val doadores = repositorio.doadoresCompativeis(necessidade)
            _uiState.update { if (it.necessidadeEmMatch == necessidade) it.copy(doadoresMatch = doadores) else it }
        }
    }

    fun fecharMatch() {
        _uiState.update { it.copy(necessidadeEmMatch = null, doadoresMatch = emptyList()) }
    }

    /** Confirma a doação de um doador do match aberto (ids desconhecidos são ignorados). */
    fun confirmarDoacao(doadorId: Int) {
        val doador = _uiState.value.doadoresMatch.find { it.id == doadorId } ?: return
        _uiState.update { it.copy(confirmados = it.confirmados + doadorId) }
        viewModelScope.launch { repositorio.confirmarDoacao(hospital, doador) }
    }

    fun tiposCompativeis(tipoReceptor: String): List<String> =
        com.example.hemaremobile.domain.tiposCompativeis(tipoReceptor)
}
