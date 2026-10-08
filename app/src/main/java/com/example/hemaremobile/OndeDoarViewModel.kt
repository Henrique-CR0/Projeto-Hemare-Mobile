package com.example.hemaremobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hemaremobile.data.repository.HemocentroRepository
import com.example.hemaremobile.domain.Hemocentro
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OndeDoarUiState(
    val busca: String = "",
    val hemocentros: List<Hemocentro> = emptyList(),
    val carregando: Boolean = true
)

/** Lista de hemocentros: vem da API do Hemare (GET /locais) ou, sem internet, do banco do aparelho. */
class OndeDoarViewModel(private val repositorio: HemocentroRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(OndeDoarUiState())
    val uiState: StateFlow<OndeDoarUiState> = _uiState.asStateFlow()

    private var buscaAtual: Job? = null

    init {
        carregar("", espera = 0)
    }

    fun alterarBusca(texto: String) {
        _uiState.update { it.copy(busca = texto) }
        carregar(texto, espera = ESPERA_DIGITACAO_MS)
    }

    /** Espera a pessoa parar de digitar antes de buscar, para não chamar a API a cada letra. */
    private fun carregar(termo: String, espera: Long) {
        buscaAtual?.cancel()
        buscaAtual = viewModelScope.launch {
            if (espera > 0) delay(espera)
            _uiState.update { it.copy(carregando = true) }
            val lista = repositorio.buscar(termo)
            _uiState.update { it.copy(hemocentros = lista, carregando = false) }
        }
    }

    companion object {
        const val ESPERA_DIGITACAO_MS = 300L
    }
}
