package com.example.hemaremobile.domain

/** Regras da triagem "Posso doar?", adaptadas de frontend/src/regras/triagem.js do site Hemare. */

const val IDADE_MIN = 16
const val IDADE_MAX = 69
const val PESO_MIN = 50

enum class NivelResultado { VERDE, AMARELO, VERMELHO }

data class ResultadoTriagem(val nivel: NivelResultado, val titulo: String, val motivos: List<String>)

fun avisoIdade(idade: Int?): String =
    if (idade != null && idade > 0 && (idade < IDADE_MIN || idade > IDADE_MAX)) "Idade para doação: $IDADE_MIN a $IDADE_MAX anos." else ""

fun avisoPeso(peso: Int?): String =
    if (peso != null && peso > 0 && peso < PESO_MIN) "Peso mínimo para doação: $PESO_MIN kg." else ""

/**
 * Avalia a triagem: qualquer impedimento → vermelho; só pontos de atenção → amarelo; nada → verde.
 * Peso abaixo de 50 kg e idade abaixo de 16 anos são impedimentos (alinhado com o site).
 */
fun avaliarTriagem(idade: Int, peso: Int, r: Map<String, Boolean>): ResultadoTriagem {
    val impedimentos = mutableListOf<String>()
    val atencoes = mutableListOf<String>()

    if (r["temHIV"] == true) impedimentos += "Você marcou HIV/AIDS."
    if (r["temHepatiteB"] == true || r["temHepatiteC"] == true) impedimentos += "Você marcou Hepatite B ou C."
    if (r["temHTLV"] == true) impedimentos += "Você marcou HTLV."
    if (r["temChagas"] == true) impedimentos += "Você marcou Doença de Chagas."
    if (r["usaDrogasInjetaveis"] == true) impedimentos += "Você marcou uso de drogas injetáveis."
    if (r["hepatiteAposOnzeAnos"] == true) impedimentos += "Você marcou hepatite após os 11 anos de idade."

    if (peso > 0 && peso < PESO_MIN) impedimentos += "Seu peso está abaixo de 50 kg, que é o mínimo para doar."
    if (idade > 0 && idade < IDADE_MIN) impedimentos += "A idade mínima para doar é 16 anos."

    if (idade in IDADE_MIN..17) atencoes += "Entre 16 e 17 anos, a doação exige autorização de um responsável legal."
    if (idade in 60..IDADE_MAX) atencoes += "Acima de 60 anos, a primeira doação exige avaliação médica antes de doar."
    if (idade > IDADE_MAX) atencoes += "A idade máxima para doar é 69 anos — confirme com o hemocentro."

    if (r["tatuagemRecente"] == true) atencoes += "Tatuagem/micropigmentação nos últimos 12 meses (1 ano) pede um tempo de espera."
    if (r["gripeResfriado"] == true) atencoes += "Gripe ou resfriado recente pede aguardar alguns dias."
    if (r["bebidaAlcoolica"] == true) atencoes += "Bebida alcoólica nas últimas 12 horas impede a doação hoje."
    if (r["gravidezOuPosParto"] == true) atencoes += "Gravidez ou pós-parto recente pede um período de espera."

    if (r["temDiabetes"] == true) atencoes += "Diabetes: se controlada, geralmente não impede — confirme na triagem."
    if (r["temHipertensao"] == true) atencoes += "Hipertensão: se controlada, geralmente não impede — confirme na triagem."
    if (r["usaMedicacaoContinua"] == true) {
        atencoes += "Você usa medicação contínua/controlada. Muitos remédios não impedem a doação, mas alguns pedem um tempo de espera. NUNCA pare um remédio por conta própria para doar — entre em contato com o hemocentro onde vai doar para confirmar."
    }

    return when {
        impedimentos.isNotEmpty() -> ResultadoTriagem(NivelResultado.VERMELHO, "Há um ponto importante a verificar", impedimentos)
        atencoes.isNotEmpty() -> ResultadoTriagem(NivelResultado.AMARELO, "Atenção: confirme alguns pontos no hemocentro", atencoes)
        else -> ResultadoTriagem(NivelResultado.VERDE, "Tudo indica que você pode doar!", emptyList())
    }
}
