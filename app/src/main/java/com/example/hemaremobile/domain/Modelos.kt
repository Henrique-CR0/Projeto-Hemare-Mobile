package com.example.hemaremobile.domain

/** Tipo de conta: define se o app abre a área do doador ou a do hospital. */
enum class TipoConta { DOADOR, HOSPITAL }

/** Dados do hospital coletados no cadastro (CadastroHospitalScreen). */
data class PerfilHospital(
    val cnpj: String,
    val cnes: String,
    val cep: String,
    val endereco: String,
    val numero: String,
    val bairro: String,
    val complemento: String,
    val cidade: String,
    val estado: String,
    val aprovado: Boolean = true
)

/** Conta logada na sessão (sem senha: ela fica só no banco do aparelho, como hash). */
data class ContaUsuario(
    val nome: String,
    val email: String,
    val tipo: TipoConta,
    val perfilHospital: PerfilHospital? = null
)

/** Doação confirmada pelo hospital (histórico). */
data class DoacaoConfirmada(
    val doadorNome: String,
    val tipoSanguineo: String,
    val dataHora: String
)

data class Hemocentro(
    val id: Int,
    val nome: String,
    val cidade: String,
    val estado: String,
    val endereco: String,
    val telefone: String
)

data class Necessidade(
    val id: Int,
    val tipoSanguineo: String,
    val urgencia: String,
    /** Id da necessidade no servidor Hemare (null quando foi publicada sem internet). */
    val idRemoto: Int? = null
)

data class DoadorCompativel(
    val id: Int,
    val nome: String,
    val tipoSanguineo: String,
    val cidade: String,
    val telefone: String,
    val identificado: Boolean
)

/** Resultado de uma operação de repositório: sucesso com valor ou falha com mensagem para a tela. */
sealed class Resultado<out T> {
    data class Sucesso<T>(val valor: T) : Resultado<T>()
    data class Falha(val mensagem: String) : Resultado<Nothing>()
}

val NIVEIS_ESTOQUE = listOf(
    "estavel" to "🟢 Estável",
    "alerta" to "🟡 Alerta",
    "critico" to "🔴 Crítico",
    "emergencia" to "⚫ Emergência"
)

val URGENCIAS_DETALHADAS = listOf(
    "estavel" to "🟢 Estável — reposição de rotina",
    "alerta" to "🟡 Alerta — estoque baixo",
    "critico" to "🔴 Crítico — poucos dias de estoque",
    "emergencia" to "⚫ Emergência — situação extrema"
)
