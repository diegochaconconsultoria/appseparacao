package com.example.separadorpedidos.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.separadorpedidos.ui.components.*
import com.example.separadorpedidos.ui.theme.SeparadorPedidosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSeparacaoScreen(
    numeroPedido: String,
    nomeCliente: String,
    onVoltarClick: () -> Unit = {},
    onSepararMaterialClick: () -> Unit = {},
    onRegistrarEntregaClick: () -> Unit = {},
    onVisualizarHistoricoClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Status da Separação",
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
                // Card do cliente
                AnimatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column {
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
                }

                // Cards de ação (REMOVIDO O CARD DE PROGRESSO)
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Separar Material
                    ActionCard(
                        title = "Separar Material",
                        description = "Iniciar processo de separação dos itens",
                        icon = Icons.Default.Inventory2,
                        onClick = onSepararMaterialClick,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Registrar Entrega
                    ActionCard(
                        title = "Registrar Entrega",
                        description = "Confirmar entrega dos materiais",
                        icon = Icons.Default.LocalShipping,
                        onClick = onRegistrarEntregaClick,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    // Visualizar Histórico
                    ActionCard(
                        title = "Visualizar Histórico",
                        description = "Ver histórico completo do pedido",
                        icon = Icons.Default.History,
                        onClick = onVisualizarHistoricoClick,
                        color = MaterialTheme.colorScheme.secondary,
                        isPrimary = false
                    )
                }
            }
        }
    }
}

// As funções ProgressItem e ActionCard permanecem iguais...

@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color,
    isPrimary: Boolean = true
) {
    AnimatedCard(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary)
                color.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = color
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = color
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatusSeparacaoScreenPreview() {
    SeparadorPedidosTheme {
        StatusSeparacaoScreen(
            numeroPedido = "PED-001",
            nomeCliente = "Cliente Exemplo Ltda"
        )
    }
}