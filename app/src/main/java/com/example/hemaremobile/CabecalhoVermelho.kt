package com.example.hemaremobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hemaremobile.ui.theme.HemareVermelho

/**
 * Cabeçalho vermelho fixo, usado no topo das telas Lista e Configuração.
 * Sem statusBarsPadding() de propósito: as telas que usam este cabeçalho já
 * rodam dentro do Scaffold de HemareApp/HospitalApp, que não tem topBar — o
 * innerPadding do Scaffold já reserva o espaço da status bar sozinho. Somar
 * um statusBarsPadding() aqui duplicava esse espaço (gap extra no topo só
 * nessas telas, ausente em InicioScreen, que não usa este cabeçalho).
 */
@Composable
fun CabecalhoVermelho(titulo: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HemareVermelho)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = titulo,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
