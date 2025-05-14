package com.example.separadorpedidos.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.separadorpedidos.data.model.SetorDisponivel
import com.example.separadorpedidos.ui.components.*
import com.example.separadorpedidos.ui.theme.SeparadorPedidosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetorSelectionScreen(
    numeroPedido: String,
    nomeCliente: String,
    setoresDisponiveis: List<SetorDisponivel> = emptyList(),
    onVoltarClick: () -> Unit = {},
    onContinuarClick: (Set<String>) -> Unit = {}
) {
    var setoresSelecionados by remember { mutableStateOf(setOf<String>()) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App Bar com botão voltar
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Seleção de Setores",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pedido: $numeroPedido",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onVoltarClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        AnimatedScreen {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Card com informações do cliente
                AnimatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Cliente",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = nomeCliente,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Chips dos setores selecionados
                if (setoresSelecionados.isNotEmpty()) {
                    AnimatedCard {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Setores Selecionados (${setoresSelecionados.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(setoresSelecionados.toList()) { setor ->
                                    AssistChip(
                                        onClick = {
                                            setoresSelecionados = setoresSelecionados - setor
                                        },
                                        label = { Text(setor) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            labelColor = MaterialTheme.colorScheme.onPrimary,
                                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Grid de setores
                AnimatedCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Setores Disponíveis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height(400.dp)
                        ) {
                            items(setoresDisponiveis) { setor ->
                                SetorCard(
                                    setor = setor,
                                    isSelected = setoresSelecionados.contains(setor.nome),
                                    onClick = {
                                        if (setor.habilitado) {
                                            setoresSelecionados = if (setoresSelecionados.contains(setor.nome)) {
                                                setoresSelecionados - setor.nome
                                            } else {
                                                setoresSelecionados + setor.nome
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Botão continuar
                ModernButton(
                    onClick = { onContinuarClick(setoresSelecionados) },
                    enabled = setoresSelecionados.isNotEmpty()
                ) {
                    Text("Continuar (${setoresSelecionados.size} selecionados)")
                }
            }
        }
    }
}

// MOVIDO PARA FORA - Função SetorCard separada
@Composable
fun SetorCard(
    setor: SetorDisponivel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !setor.habilitado -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "cardBackgroundColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            !setor.habilitado -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(200),
        label = "cardBorderColor"
    )

    Card(
        onClick = onClick,
        enabled = setor.habilitado,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when {
                        !setor.habilitado -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = setor.nome,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        !setor.habilitado -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetorSelectionScreenPreview() {
    SeparadorPedidosTheme {
        val setoresExemplo = listOf(
            SetorDisponivel("Aramado", "aramado", true),
            SetorDisponivel("Tubo", "tubo", false),
            SetorDisponivel("Chapa", "chapa", true),
            SetorDisponivel("Marcenaria", "marcenaria", true)
        )

        SetorSelectionScreen(
            numeroPedido = "PED-001",
            nomeCliente = "Cliente Exemplo Ltda",
            setoresDisponiveis = setoresExemplo
        )
    }
}