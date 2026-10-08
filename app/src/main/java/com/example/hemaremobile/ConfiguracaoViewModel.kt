package com.example.hemaremobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hemaremobile.data.repository.Preferencias
import com.example.hemaremobile.data.repository.PreferenciasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConfiguracaoUiState(
    val temaEscuro: Boolean = true,
    val notificacoesAtivas: Boolean = true
)

/** Preferências salvas no aparelho (DataStore): continuam valendo ao fechar e abrir o app. */
class ConfiguracaoViewModel(private val repositorio: PreferenciasRepository) : ViewModel() {

    val uiState: StateFlow<ConfiguracaoUiState> = repositorio.preferencias
        .map { it.paraUiState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConfiguracaoUiState())

    fun alternarTemaEscuro(ativo: Boolean) {
        viewModelScope.launch { repositorio.definirTemaEscuro(ativo) }
    }

    fun alternarNotificacoes(ativo: Boolean) {
        viewModelScope.launch { repositorio.definirNotificacoes(ativo) }
    }

    private fun Preferencias.paraUiState() = ConfiguracaoUiState(temaEscuro, notificacoesAtivas)
}
