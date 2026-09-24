package com.example.hemaremobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hemaremobile.navigation.RotasHospital
import com.example.hemaremobile.ui.theme.HemareRosaClaro
import com.example.hemaremobile.ui.theme.HemareVermelho

private data class ItemPainel(val rota: String, val icone: ImageVector, val titulo: String, val descricao: String)

private val itensPainel = listOf(
    ItemPainel(RotasHospital.ESTOQUE, Icons.Filled.Inventory, "Estoque e necessidades", "Termômetro, publicar necessidade e buscar doadores"),
    ItemPainel(RotasHospital.HISTORICO, Icons.Filled.History, "Histórico de doações", "Doações já confirmadas por este hospital"),
    ItemPainel(RotasHospital.PERFIL, Icons.Filled.Storefront, "Perfil da instituição", "CNPJ, CNES e endereço cadastrados"),
    ItemPainel(RotasHospital.PLANO, Icons.Filled.Star, "Plano", "Assinatura atual e uso do mês")
)

/**
 * Hub do hospital: resumo rápido (necessidades, doações, tipos em alerta) + selo de
 * verificação + atalhos pras sub-telas. Compartilha o PainelHospitalViewModel com as
 * sub-telas (escopado ao grafo de navegação em Navegacao.kt), então os números aqui
 * refletem o que foi feito em qualquer uma delas.
 */
@Composable
fun PainelHospitalHubScreen(
    nomeHospital: String,
    aprovado: Boolean,
    onSair: () -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: PainelHospitalViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val tiposEmAlerta = TIPOS_SANGUINEOS.count { tipo ->
        val nivel = uiState.estoque[tipo] ?: "estavel"
        nivel == "critico" || nivel == "emergencia"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { CabecalhoVermelho(titulo = "Hemare Hospital") }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = nomeHospital,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SeloVerificacao(aprovado = aprovado)
                }
                TextButton(onClick = onSair) {
                    Text("Sair")
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EstatisticaPainel(
                    valor = uiState.necessidades.size.toString(),
                    rotulo = "Necessidades",
                    modifier = Modifier.weight(1f)
                )
                EstatisticaPainel(
                    valor = uiState.confirmados.size.toString(),
                    rotulo = "Doações",
                    modifier = Modifier.weight(1f)
                )
                EstatisticaPainel(
                    valor = tiposEmAlerta.toString(),
                    rotulo = "Em alerta",
                    destaque = tiposEmAlerta > 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        items(itensPainel) { item ->
            Card(
                onClick = { onItemClick(item.rota) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                LinhaItemPainel(item)
            }
        }
    }
}

@Composable
private fun EstatisticaPainel(
    valor: String,
    rotulo: String,
    modifier: Modifier = Modifier,
    destaque: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (destaque) HemareVermelho else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = rotulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LinhaItemPainel(item: ItemPainel) {
    Row(
        modifier = Modifier
            .padding(14.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HemareRosaClaro,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = item.icone,
                    contentDescription = null,
                    tint = HemareVermelho,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
