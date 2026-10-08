package com.example.hemaremobile

import com.example.hemaremobile.domain.ResultadoTriagem
import com.example.hemaremobile.domain.avaliarTriagem
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PerguntaSimNao(val campo: String, val texto: String)

/** Presentation Layer: campos e perguntas adaptados de frontend/src/paginas/Triagem.jsx do site Hemare. */
val perguntasSituacoesRecentes = listOf(
    PerguntaSimNao("tatuagemRecente", "Fez tatuagem ou micropigmentação nos últimos 12 meses?"),
    PerguntaSimNao("gripeResfriado", "Está com gripe ou resfriado (ou teve há poucos dias)?"),
    PerguntaSimNao("bebidaAlcoolica", "Ingeriu bebida alcoólica nas últimas 12 horas?"),
    PerguntaSimNao("gravidezOuPosParto", "Está grávida ou teve parto recentemente?")
)

val perguntasSaude = listOf(
    PerguntaSimNao("temHIV", "Você tem HIV/AIDS?"),
    PerguntaSimNao("temHepatiteB", "Você tem Hepatite B?"),
    PerguntaSimNao("temHepatiteC", "Você tem Hepatite C?"),
    PerguntaSimNao("temHTLV", "Você tem HTLV?"),
    PerguntaSimNao("temChagas", "Você tem Doença de Chagas?"),
    PerguntaSimNao("hepatiteAposOnzeAnos", "Teve hepatite após os 11 anos de idade?"),
    PerguntaSimNao("usaDrogasInjetaveis", "Faz uso de drogas injetáveis?")
)

val perguntasAtencao = listOf(
    PerguntaSimNao("temDiabetes", "Você tem diabetes?"),
    PerguntaSimNao("temHipertensao", "Você tem hipertensão (pressão alta)?"),
    PerguntaSimNao("usaMedicacaoContinua", "Você usa algum medicamento controlado ou de uso contínuo?")
)

data class PossoDoarUiState(
    val idadeTexto: String = "",
    val pesoTexto: String = "",
    val respostas: Map<String, Boolean> = emptyMap(),
    val resultado: ResultadoTriagem? = null,
    val erro: String = ""
) {
    val avisoIdade: String get() = com.example.hemaremobile.domain.avisoIdade(idadeTexto.toIntOrNull())

    val avisoPeso: String get() = com.example.hemaremobile.domain.avisoPeso(pesoTexto.toIntOrNull())
}

/**
 * Triagem "Posso doar?": as regras ficam em domain/RegrasTriagem.kt (adaptadas de
 * frontend/src/regras/triagem.js do site Hemare). Não substitui a triagem clínica.
 */
class PossoDoarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PossoDoarUiState())
    val uiState: StateFlow<PossoDoarUiState> = _uiState.asStateFlow()

    fun alterarIdade(texto: String) {
        val valor = texto.filter { it.isDigit() }.take(3).toIntOrNull()?.coerceAtMost(120)
        _uiState.update { it.copy(idadeTexto = valor?.toString() ?: "") }
    }

    fun alterarPeso(texto: String) {
        val valor = texto.filter { it.isDigit() }.take(3).toIntOrNull()?.coerceAtMost(300)
        _uiState.update { it.copy(pesoTexto = valor?.toString() ?: "") }
    }

    fun responder(campo: String, resposta: Boolean) {
        _uiState.update { it.copy(respostas = it.respostas + (campo to resposta)) }
    }

    fun verResultado() {
        val estado = _uiState.value
        val idade = estado.idadeTexto.toIntOrNull()
        val peso = estado.pesoTexto.toIntOrNull()
        if (idade == null || peso == null) {
            _uiState.update {
                it.copy(resultado = null, erro = "⚠️ Preencha pelo menos sua idade e seu peso para ver o resultado.")
            }
            return
        }
        _uiState.update { it.copy(erro = "", resultado = avaliarTriagem(idade, peso, it.respostas)) }
    }

    fun refazer() {
        _uiState.value = PossoDoarUiState()
    }
}
