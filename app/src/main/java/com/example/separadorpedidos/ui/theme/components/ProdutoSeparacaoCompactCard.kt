package com.example.separadorpedidos.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.separadorpedidos.data.model.ProdutoSeparacao

@Composable
fun ProdutoSeparacaoCompactCard(
    produto: ProdutoSeparacao,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onViewImage: () -> Unit = {}
) {
    val jaSeparado = produto.jaSeparado()
    val podeSelecionar = produto.podeSelecionar()

    // Removido o controle de estado do dialog e sua renderização dentro do card

    AnimatedCard(
        onClick = if (podeSelecionar) onToggleSelection else { {} },
        enabled = podeSelecionar,
        colors = CardDefaults.cardColors(
            containerColor = when {
                jaSeparado -> Color(0xFF4CAF50).copy(alpha = 0.1f) // Verde claro para separados
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier.border(
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            shape = RoundedCornerShape(12.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = if (podeSelecionar) { _ -> onToggleSelection() } else null,
                enabled = podeSelecionar
            )

            // Informações do produto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Código e status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = produto.produto,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = if (podeSelecionar) 1f else 0.6f
                        )
                    )

                    if (jaSeparado) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Separado", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF4CAF50),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                // Descrição
                Text(
                    text = produto.descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (podeSelecionar) 0.9f else 0.5f
                    )
                )

                // Quantidades em linha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Orig: ${produto.getQtdOriginalFormatted()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (podeSelecionar) 1f else 0.6f
                        )
                    )
                    Text(
                        text = "Saldo: ${produto.getSaldoFormatted()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (jaSeparado) FontWeight.Medium else FontWeight.Normal,
                        color = if (jaSeparado)
                            Color(0xFF4CAF50).copy(alpha = if (podeSelecionar) 1f else 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (podeSelecionar) 1f else 0.6f
                            )
                    )
                    Text(
                        text = "A Sep: ${produto.getQtdaSepararFormatted()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (podeSelecionar)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Localização e Setor em linha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Localização
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (podeSelecionar) 1f else 0.6f
                            )
                        )
                        Text(
                            text = if (produto.local.isBlank()) "S/ Local" else produto.getEnderecoCompleto(),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (podeSelecionar) 1f else 0.6f
                            )
                        )
                    }

                    // Setor
                    Text(
                        text = "Setor: ${produto.setor}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (podeSelecionar) 1f else 0.6f
                        )
                    )
                }
            }

            // BOTÃO VER FOTO - SEMPRE PRESENTE
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botão de visualizar/tirar foto - agora só chama onViewImage
                IconButton(
                    onClick = { onViewImage() },  // Removida a lógica interna de mostrar diálogo
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.RemoveRedEye,
                        contentDescription = "Ver/Tirar foto do produto",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Texto indicativo
                Text(
                    text = "Ver foto",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}