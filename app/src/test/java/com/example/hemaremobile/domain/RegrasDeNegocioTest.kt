package com.example.hemaremobile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompatibilidadeTest {

    @Test
    fun `O negativo so recebe de O negativo`() {
        assertEquals(listOf("O-"), tiposCompativeis("O-"))
    }

    @Test
    fun `AB positivo e receptor universal`() {
        assertEquals(TIPOS_SANGUINEOS.toSet(), tiposCompativeis("AB+").toSet())
    }

    @Test
    fun `O negativo e doador universal`() {
        TIPOS_SANGUINEOS.forEach { receptor -> assertTrue("O- deveria doar para $receptor", podeDoar("O-", receptor)) }
    }

    @Test
    fun `Rh positivo nao doa para Rh negativo`() {
        assertFalse(podeDoar("A+", "A-"))
        assertFalse(podeDoar("O+", "AB-"))
    }

    @Test
    fun `tipo invalido nao tem compativeis`() {
        assertTrue(tiposCompativeis("X+").isEmpty())
        assertFalse(podeDoar("O-", "X+"))
    }
}

class ValidadoresTest {

    @Test
    fun `email valido e invalido`() {
        assertTrue(emailValido("maria@email.com"))
        assertFalse(emailValido("maria@email"))
        assertFalse(emailValido("maria email.com"))
        assertFalse(emailValido(""))
    }

    @Test
    fun `forca da senha vai de 0 a 3`() {
        assertEquals(0, forcaSenha("abc"))
        assertEquals(1, forcaSenha("abcdefgh"))
        assertEquals(2, forcaSenha("Abcdefgh"))
        assertEquals(3, forcaSenha("Abcdef1!"))
    }

    @Test
    fun `CNPJ com digitos verificadores corretos e aceito`() {
        assertTrue(cnpjValido("11.222.333/0001-81"))
        assertTrue(cnpjValido("11222333000181"))
    }

    @Test
    fun `CNPJ invalido e recusado`() {
        assertFalse(cnpjValido("11.222.333/0001-80"))
        assertFalse(cnpjValido("11.111.111/1111-11"))
        assertFalse(cnpjValido("123"))
    }

    @Test
    fun `mascaras de CNPJ e CEP`() {
        assertEquals("11.222.333/0001-81", mascaraCnpj("11222333000181"))
        assertEquals("11.222", mascaraCnpj("11222"))
        assertEquals("01310-100", mascaraCep("01310100"))
        assertEquals("013", mascaraCep("013"))
        assertEquals("01310-100", mascaraCep("01310-100abc999"))
    }
}

class RegrasTriagemTest {

    private val semRespostas = emptyMap<String, Boolean>()

    @Test
    fun `sem impedimentos o resultado e verde`() {
        val r = avaliarTriagem(idade = 25, peso = 70, r = semRespostas)
        assertEquals(NivelResultado.VERDE, r.nivel)
        assertTrue(r.motivos.isEmpty())
    }

    @Test
    fun `tatuagem recente gera atencao amarela`() {
        val r = avaliarTriagem(25, 70, mapOf("tatuagemRecente" to true))
        assertEquals(NivelResultado.AMARELO, r.nivel)
        assertEquals(1, r.motivos.size)
    }

    @Test
    fun `impedimento definitivo gera vermelho mesmo com atencoes`() {
        val r = avaliarTriagem(25, 70, mapOf("temHIV" to true, "gripeResfriado" to true))
        assertEquals(NivelResultado.VERMELHO, r.nivel)
        assertEquals(listOf("Você marcou HIV/AIDS."), r.motivos)
    }

    @Test
    fun `limites de idade e peso`() {
        // abaixo do minimo e impedimento (vermelho)
        assertEquals(NivelResultado.VERMELHO, avaliarTriagem(15, 70, semRespostas).nivel)
        assertEquals(NivelResultado.VERMELHO, avaliarTriagem(30, 49, semRespostas).nivel)
        // 16-17 anos exigem autorizacao e 60-69 avaliacao medica (amarelo)
        assertEquals(NivelResultado.AMARELO, avaliarTriagem(IDADE_MIN, PESO_MIN, semRespostas).nivel)
        assertEquals(NivelResultado.AMARELO, avaliarTriagem(IDADE_MAX, PESO_MIN, semRespostas).nivel)
        // acima da idade maxima: confirmar no hemocentro (amarelo)
        assertEquals(NivelResultado.AMARELO, avaliarTriagem(70, 70, semRespostas).nivel)
        // faixa sem restricoes
        assertEquals(NivelResultado.VERDE, avaliarTriagem(18, PESO_MIN, semRespostas).nivel)
        assertEquals(NivelResultado.VERDE, avaliarTriagem(59, 90, semRespostas).nivel)
    }

    @Test
    fun `avisos em tempo real de idade e peso`() {
        assertEquals("Idade para doação: 16 a 69 anos.", avisoIdade(15))
        assertEquals("", avisoIdade(30))
        assertEquals("", avisoIdade(null))
        assertEquals("Peso mínimo para doação: 50 kg.", avisoPeso(49))
        assertEquals("", avisoPeso(50))
    }
}
