package com.example.hemaremobile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import com.example.hemaremobile.ui.theme.HemareVermelho

@Composable
fun CampoAuth(
    valor: String,
    aoAlterar: (String) -> Unit,
    rotulo: String,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    ehSenha: Boolean = false,
    modifier: Modifier = Modifier
) {
    var mostrarSenha by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = aoAlterar,
        label = { Text(rotulo) },
        singleLine = true,
        visualTransformation = if (ehSenha && !mostrarSenha) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (ehSenha) KeyboardType.Password else tipoTeclado),
        trailingIcon = if (ehSenha) {
            {
                IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                    Icon(
                        imageVector = if (mostrarSenha) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (mostrarSenha) "Ocultar senha" else "Mostrar senha"
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HemareVermelho,
            cursorColor = HemareVermelho
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Campo pra valores com máscara (CNPJ, CEP, CNES...). Usa [TextFieldValue] em vez de
 * String pura e força o cursor pro fim do texto formatado a cada edição.
 *
 * Sem isso, o [CampoAuth] comum (baseado só em String) faz o cursor pular pro início
 * a cada tecla: como a máscara reconstrói o texto inteiro (inserindo "." "/" "-" etc.),
 * o Compose não consegue inferir onde o usuário editou e reseta a posição do cursor.
 */
@Composable
fun CampoAuthMascarado(
    valor: String,
    aoAlterar: (String) -> Unit,
    rotulo: String,
    mascara: (String) -> String,
    modifier: Modifier = Modifier
) {
    var campo by remember {
        mutableStateOf(TextFieldValue(text = valor, selection = TextRange(valor.length)))
    }
    // Mantém sincronizado se o valor mudar por fora (ex.: campo limpo após enviar o formulário).
    if (campo.text != valor) {
        campo = TextFieldValue(text = valor, selection = TextRange(valor.length))
    }

    OutlinedTextField(
        value = campo,
        onValueChange = { novo ->
            val mascarado = mascara(novo.text)
            campo = TextFieldValue(text = mascarado, selection = TextRange(mascarado.length))
            aoAlterar(mascarado)
        },
        label = { Text(rotulo) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HemareVermelho,
            cursorColor = HemareVermelho
        ),
        modifier = modifier.fillMaxWidth()
    )
}
