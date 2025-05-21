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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.separadorpedidos.ui.components.*
import com.example.separadorpedidos.ui.theme.SeparadorPedidosTheme
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.separadorpedidos.R

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

    // Carregar a composição da animação Lottie
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.working_animation)
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App Bar com número do pedido maior
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Status da Separação",
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Pedido:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                        Text(
                            text = numeroPedido,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Espaçamento reduzido
            ) {
                // Card do cliente
                AnimatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    // Conteúdo existente do card do cliente...
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

                // Cards de ação em uma única coluna sem padding adicional
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp) // Espaçamento reduzido
                ) {
                    // Separar Material
                    LargeActionCard(
                        title = "Separar Material",
                        description = "Iniciar processo de separação dos itens do pedido por setor",
                        icon = Icons.Default.Inventory2,
                        onClick = onSepararMaterialClick,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        iconColor = MaterialTheme.colorScheme.primary
                    )

                    // Registrar Entrega
                    LargeActionCard(
                        title = "Registrar Entrega",
                        description = "Confirmar entrega dos materiais com validação de senha",
                        icon = Icons.Default.LocalShipping,
                        onClick = onRegistrarEntregaClick,
                        backgroundColor = Color(0xFF2196F3),
                        iconColor = Color(0xFF2196F3)
                    )

                    // Visualizar Histórico
                    LargeActionCard(
                        title = "Visualizar Histórico",
                        description = "Ver histórico completo e status detalhado do pedido",
                        icon = Icons.Default.History,
                        onClick = onVisualizarHistoricoClick,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        isPrimary = false
                    )
                }

                // Adicionar um margin negativo para subir a animação
                Spacer(modifier = Modifier.height((-50).dp))

                // Animação Lottie grande, com posicionamento ajustado para subir
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 350.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(750.dp)
                            .offset(y = (-80).dp), // Deslocamento maior para cima
                        iterations = LottieConstants.IterateForever,
                        isPlaying = true
                    )
                }
            }
        }
    }
}
@Composable
fun LargeActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    backgroundColor: androidx.compose.ui.graphics.Color,
    iconColor: androidx.compose.ui.graphics.Color,
    isPrimary: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "cardScale"
    )

    AnimatedCard(
        onClick = {
            isPressed = true
            onClick()
            isPressed = false
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary)
                backgroundColor.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Linha superior com ícone e título
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Ícone grande em um círculo colorido
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = iconColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = iconColor
                        )
                    }
                }

                // Título e descrição
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }

                // Seta indicando ação
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = iconColor
                )
            }

            // Linha inferior com detalhes adicionais (opcional)
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Toque para ${title.lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = iconColor,
                    fontWeight = FontWeight.Medium
                )
            }
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