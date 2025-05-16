package com.example.separadorpedidos.ui.components

import androidx.compose.foundation.background
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
import com.example.separadorpedidos.data.model.ProdutoEntrega

@Composable
fun ProdutoCompactCard(
    produto: ProdutoEntrega,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    val podeEntregar = produto.podeEntregar()
    val jaEntregue = produto.jaEntregue()

    AnimatedCard(
        onClick = if (podeEntregar) onToggleSelection else { {} },
        enabled = podeEntregar,
        colors = CardDefaults.cardColors(
            containerColor = when {
                jaEntregue -> Color(0xFF2196F3).copy(alpha = 0.1f)
                isSelected -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier.border(
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent,
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
                onCheckedChange = if (podeEntregar) { _ -> onToggleSelection() } else null,
                enabled = podeEntregar
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
                        color = MaterialTheme.colorScheme.tertiary.copy(
                            alpha = if (podeEntregar) 1f else 0.6f
                        )
                    )

                    if (jaEntregue) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Entregue", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF2196F3),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                // Descrição - FONTE AUMENTADA
                Text(
                    text = produto.descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (podeEntregar) 0.9f else 0.5f
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
                            alpha = if (podeEntregar) 1f else 0.6f
                        )
                    )
                    Text(
                        text = "Sep: ${produto.getQtdSeparadaFormatted()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (podeEntregar) 1f else 0.6f
                        )
                    )
                    Text(
                        text = "Entregar: ${produto.getSaldoAEntregarFormatted()}",
                        style = MaterialTheme.typography.titleMedium, // FONTE AUMENTADA
                        fontWeight = FontWeight.Bold,
                        color = if (podeEntregar)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Setor e Origem em linha - ADICIONADO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Setor
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (podeEntregar) 1f else 0.6f
                            )
                        )
                        Text(
                            text = "Setor: ${produto.setor}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (podeEntregar) 1f else 0.6f
                            )
                        )
                    }

                    // Origem
                    Text(
                        text = "Origem: ${produto.origem}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (podeEntregar) 1f else 0.6f
                        )
                    )
                }
            }
        }
    }
}