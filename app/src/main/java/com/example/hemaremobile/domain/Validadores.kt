package com.example.hemaremobile.domain

private val PADRAO_EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

fun emailValido(email: String): Boolean = email.isNotEmpty() && PADRAO_EMAIL.matches(email)

/** Força da senha de 0 a 3 (tamanho, maiúsculas+minúsculas, número+símbolo) — igual ao site. */
fun forcaSenha(senha: String): Int {
    var forca = 0
    if (senha.length >= 8) forca++
    if (senha.any { it.isUpperCase() } && senha.any { it.isLowerCase() }) forca++
    if (senha.any { it.isDigit() } && senha.any { !it.isLetterOrDigit() }) forca++
    return forca
}

fun mascaraCnpj(valor: String): String {
    val digitos = valor.filter { it.isDigit() }.take(14)
    val sb = StringBuilder()
    digitos.forEachIndexed { indice, digito ->
        when (indice) {
            2, 5 -> sb.append('.')
            8 -> sb.append('/')
            12 -> sb.append('-')
        }
        sb.append(digito)
    }
    return sb.toString()
}

/** Valida os dois dígitos verificadores do CNPJ. */
fun cnpjValido(valor: String): Boolean {
    val cnpj = valor.filter { it.isDigit() }
    if (cnpj.length != 14 || cnpj.all { it == cnpj[0] }) return false

    fun calcularDigito(base: Int): Int {
        var soma = 0
        var pos = base - 7
        for (i in base downTo 1) {
            soma += (cnpj[base - i] - '0') * pos
            pos--
            if (pos < 2) pos = 9
        }
        val resto = soma % 11
        return if (resto < 2) 0 else 11 - resto
    }

    return calcularDigito(12) == (cnpj[12] - '0') && calcularDigito(13) == (cnpj[13] - '0')
}

fun mascaraCep(valor: String): String {
    val digitos = valor.filter { it.isDigit() }.take(8)
    return if (digitos.length > 5) "${digitos.substring(0, 5)}-${digitos.substring(5)}" else digitos
}
