package com.example.hemaremobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hemaremobile.ui.theme.HemareVerde
import com.example.hemaremobile.ui.theme.HemareVermelhoAcao

private fun mascaraCnpjExibicao(digitos: String): String {
    if (digitos.length != 14) return digitos
    return "${digitos.substring(0, 2)}.${digitos.substring(2, 5)}.${digitos.substring(5, 8)}/" +
        "${digitos.substring(8, 12)}-${digitos.substring(12, 14)}"
}

@Composable
fun PerfilInstituicaoScreen(
    nomeHospital: String,
    email: String,
    perfil: PerfilHospital?,
    onVoltar: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CabecalhoVermelho(titulo = "Perfil da instituição", onVoltar = onVoltar)
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = nomeHospital,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SeloVerificacao(aprovado = perfil?.aprovado ?: true)
                }
            }
        }

        if (perfil == null) {
            item {
                Text(
                    text = "Nenhum dado de cadastro encontrado nesta sessão.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LinhaPerfil("CNPJ", mascaraCnpjExibicao(perfil.cnpj))
                        LinhaPerfil("CNES", perfil.cnes)
                        LinhaPerfil(
                            "Endereço",
                            "${perfil.endereco}, ${perfil.numero}" +
                                if (perfil.complemento.isNotBlank()) " — ${perfil.complemento}" else ""
                        )
                        LinhaPerfil("Bairro", perfil.bairro)
                        LinhaPerfil("Cidade / UF", "${perfil.cidade} - ${perfil.estado}")
                        LinhaPerfil("CEP", perfil.cep)
                    }
                }
            }
        }

        item {
            Text(
                text = "Esses dados foram informados no cadastro da instituição. A edição de perfil chega junto com a camada de dados real (Domain/Data layer).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
private fun LinhaPerfil(rotulo: String, valor: String) {
    Column {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Selo de verificação da instituição — reaproveitado no hub e no perfil. */
@Composable
fun SeloVerificacao(aprovado: Boolean) {
    val cor = if (aprovado) HemareVerde else HemareVermelhoAcao
    val texto = if (aprovado) "Verificado" else "Verificação pendente"
    val icone = if (aprovado) Icons.Filled.CheckCircle else Icons.Filled.HourglassEmpty

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = cor.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icone, contentDescription = null, tint = cor, modifier = Modifier.padding(end = 4.dp))
            Text(text = texto, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = cor)
        }
    }
}
