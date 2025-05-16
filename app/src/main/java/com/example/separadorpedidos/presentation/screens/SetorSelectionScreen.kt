package com.example.separadorpedidos.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
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

                /*// Chips dos setores selecionados - SEM BARRA DE PROGRESSO
                if (setoresSelecionados.isNotEmpty()) {
                    AnimatedCard {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Setores Selecionados (${setoresSelecionados.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(setoresSelecionados.toList()) { setor ->
                                    ModernSelectedChip(
                                        text = setor,
                                        onRemove = {
                                            setoresSelecionados = setoresSelecionados - setor
                                        }
                                    )
                                }
                            }
                        }
                    }
                }*/

                // Grid de setores expandido - SEM ALTURA FIXA
                AnimatedCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Widgets,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Setores Disponíveis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // GRID EXPANDIDO - altura dinâmica baseada no conteúdo
                        val gridHeight = remember(setoresDisponiveis.size) {
                            val rows = kotlin.math.ceil(setoresDisponiveis.size / 2.0).toInt()
                            (rows * 110 + (rows - 1) * 12).dp
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height(gridHeight),
                            userScrollEnabled = false // Desabilita scroll do grid
                        ) {
                            items(setoresDisponiveis) { setor ->
                                ModernSetorCard(
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

                // Botão continuar melhorado
                ModernButton(
                    onClick = { onContinuarClick(setoresSelecionados) },
                    enabled = setoresSelecionados.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continuar (${setoresSelecionados.size} selecionados)")
                }
            }
        }
    }
}

// Componente modernizado para chip selecionado
@Composable
fun ModernSelectedChip(
    text: String,
    onRemove: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "chipScale"
    )

    Surface(
        onClick = onRemove,
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Icon(
                Icons.Default.Close,
                contentDescription = "Remover $text",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// Card modernizado para setores
@Composable
fun ModernSetorCard(
    setor: SetorDisponivel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = when {
            !setor.habilitado -> 0.95f
            isPressed -> 0.98f
            isSelected -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cardScale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            !setor.habilitado -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        },
        animationSpec = tween(200),
        label = "borderColor"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            !setor.habilitado -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "containerColor"
    )

    Card(
        onClick = onClick,
        enabled = setor.habilitado,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .scale(scale)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Gradiente sutil para cards selecionados
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                // Ícone baseado no setor
                val setorIcon = getSetorIcon(setor.nome)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        !setor.habilitado -> MaterialTheme.colorScheme.surface
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = setorIcon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = when {
                                !setor.habilitado -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                Text(
                    text = setor.nome,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        !setor.habilitado -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2
                )

                // Indicador visual de status
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selecionado",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Função para obter ícone baseado no nome do setor
@Composable
fun getSetorIcon(nomeSetor: String): ImageVector {
    return when (nomeSetor.lowercase()) {
        "aramado" -> Icons.Default.GridOn
        "tubo" -> Icons.Default.Architecture
        "chapa" -> Icons.Default.ViewModule
        "solda" -> Icons.Default.Construction
        "marcenaria" -> Icons.Default.Handyman
        "pintura" -> Icons.Default.Palette
        "comunicação visual" -> Icons.Default.Campaign
        "embalagem" -> Icons.Default.Inventory2
        "pré montagem" -> Icons.Default.BuildCircle
        "laser" -> Icons.Default.FlashOn
        "vaccumm forming" -> Icons.Default.Plumbing
        "usinagem mdf" -> Icons.Default.Engineering
        "laser tubo" -> Icons.Default.Bolt
        "sem cadastro" -> Icons.Default.HelpOutline
        else -> Icons.Default.Business
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
            SetorDisponivel("Marcenaria", "marcenaria", true),
            SetorDisponivel("Pintura", "pintura", true),
            SetorDisponivel("Laser", "laser", true)
        )

        SetorSelectionScreen(
            numeroPedido = "PED-001",
            nomeCliente = "Cliente Exemplo Ltda",
            setoresDisponiveis = setoresExemplo
        )
    }
}