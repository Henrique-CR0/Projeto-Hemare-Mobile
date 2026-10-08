package com.example.hemaremobile.data.local

import java.security.MessageDigest

/** Hash SHA-256 da senha, com o email como "sal" — a senha nunca é gravada em texto no aparelho. */
object HashSenha {
    fun gerar(email: String, senha: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("hemare|${email.trim().lowercase()}|$senha".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
