package com.example.hemaremobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hemaremobile.di.HemareApplication
import com.example.hemaremobile.navigation.HemareRaiz
import com.example.hemaremobile.ui.theme.HemareMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as HemareApplication).container
        setContent {
            val configuracaoViewModel: ConfiguracaoViewModel = viewModel(factory = container.fabricaConfiguracao)
            val autenticacaoViewModel: AutenticacaoViewModel = viewModel(factory = container.fabricaAutenticacao)
            val configuracaoState by configuracaoViewModel.uiState.collectAsState()

            HemareMobileTheme(darkTheme = configuracaoState.temaEscuro) {
                HemareRaiz(
                    configuracaoViewModel = configuracaoViewModel,
                    autenticacaoViewModel = autenticacaoViewModel
                )
            }
        }
    }
}
