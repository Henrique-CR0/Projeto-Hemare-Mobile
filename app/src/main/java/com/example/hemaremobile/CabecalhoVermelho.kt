package com.example.hemaremobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hemaremobile.ui.theme.HemareVermelho

/**
 * Cabeçalho vermelho fixo, usado no topo das telas Lista, Configuração e Painel do Hospital.
 * Sem statusBarsPadding() de propósito: as telas que usam este cabeçalho já
 * rodam dentro do Scaffold de HemareApp/HospitalApp, que não tem topBar — o
 * innerPadding do Scaffold já reserva o espaço da status bar sozinho. Somar
 * um statusBarsPadding() aqui duplicava esse espaço (gap extra no topo só
 * nessas telas, ausente em InicioScreen, que não usa este cabeçalho).
 *
 * @param onVoltar quando informado, mostra uma seta de voltar antes do título
 *   (usado pelas sub-telas da aba Lista — Posso doar, Onde doar, Guia, Mitos —
 *   pra voltar ao hub da Lista). Deixe nulo para o cabeçalho simples (Lista,
 *   Configuração, Painel do Hospital).
 */
@Composable
fun CabecalhoVermelho(titulo: String, onVoltar: (() -> Unit)? = null) {
    if (onVoltar == null) {
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
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HemareVermelho)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = titulo,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
