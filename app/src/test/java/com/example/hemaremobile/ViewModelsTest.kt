package com.example.hemaremobile

import com.example.hemaremobile.domain.Hemocentro
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OndeDoarViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @get:Rule val regra = RegraDispatcherPrincipal(dispatcher)

    private val repositorio = FakeHemocentroRepository(
        listOf(
            Hemocentro(1, "Fundação Hemope", "Recife", "PE", "R. Joaquim Nabuco, 171", "(81) 3182-4600"),
            Hemocentro(2, "HEMORIO", "Rio de Janeiro", "RJ", "R. Frei Caneca, 8", "(21) 3916-8300")
        )
    )

    @Test
    fun `carrega todos ao abrir e filtra pela busca`() = runTest(dispatcher) {
        val viewModel = OndeDoarViewModel(repositorio)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.hemocentros.size)
        assertFalse(viewModel.uiState.value.carregando)

        viewModel.alterarBusca("recife")
        advanceUntilIdle()
        assertEquals(listOf("Fundação Hemope"), viewModel.uiState.value.hemocentros.map { it.nome })
    }

    @Test
    fun `espera parar de digitar antes de buscar`() = runTest(dispatcher) {
        val viewModel = OndeDoarViewModel(repositorio)
        advanceUntilIdle()
        viewModel.alterarBusca("r")
        viewModel.alterarBusca("re")
        viewModel.alterarBusca("rec")
        advanceTimeBy(OndeDoarViewModel.ESPERA_DIGITACAO_MS + 1)
        advanceUntilIdle()
        assertEquals(listOf("", "rec"), repositorio.buscasFeitas)
    }
}

class ConfiguracaoViewModelTest {
    @get:Rule val regra = RegraDispatcherPrincipal()

    @Test
    fun `tema escuro comeca ligado e pode ser desligado`() {
        val viewModel = ConfiguracaoViewModel(FakePreferenciasRepository())
        assertTrue(viewModel.uiState.value.temaEscuro)
        viewModel.alternarTemaEscuro(false)
        viewModel.alternarNotificacoes(false)
        assertFalse(viewModel.uiState.value.temaEscuro)
        assertFalse(viewModel.uiState.value.notificacoesAtivas)
    }
}
