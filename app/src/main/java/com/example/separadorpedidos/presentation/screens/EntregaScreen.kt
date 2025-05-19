@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.separadorpedidos.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.separadorpedidos.data.model.ProdutoEntrega
import com.example.separadorpedidos.presentation.viewmodel.EntregaViewModel
import com.example.separadorpedidos.ui.components.*
import com.example.separadorpedidos.ui.theme.SeparadorPedidosTheme

// Cores azuis customizadas para a tela de entrega
val BlueContainer = Color(0xFFE3F2FD)
val BluePrimary = Color(0xFF2196F3)
val BlueOnContainer = Color(0xFF0D47A1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntregaLoadingModal(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = { }, // Não permite fechar
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EntregaLoadingContent()
            }
        }
    }
}

@Composable
private fun EntregaLoadingContent() {
    // Animações para os círculos pulsantes
    val infiniteTransition = rememberInfiniteTransition(label = "entregaLoading")

    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale1"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale2"
    )

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha1"
    )

    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha2"
    )

    // Animação de rotação para o ícone
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .size(280.dp)
            .scale(1.0f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 16.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Círculos pulsantes de fundo
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale1)
                    .background(
                        color = BluePrimary.copy(alpha = alpha1 * 0.2f),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale2)
                    .background(
                        color = BluePrimary.copy(alpha = alpha2 * 0.4f),
                        shape = CircleShape
                    )
            )

            // Conteúdo central
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Ícone rotativo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(rotation)
                        .background(
                            color = BluePrimary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = BluePrimary
                    )
                }

                // Textos
                Text(
                    text = "Realizando Entrega",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )

                Text(
                    text = "Aguarde enquanto processamos a entrega...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                // Barra de progresso indeterminada
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(120.dp)
                        .height(4.dp),
                    color = BluePrimary,
                    trackColor = BluePrimary.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
fun ProdutoEntregaCard(
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
                jaEntregue -> BluePrimary.copy(alpha = 0.1f) // Azul claro para entregues
                isSelected -> BlueContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier.border(
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) BluePrimary else Color.Transparent,
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
                enabled = podeEntregar,
                colors = CheckboxDefaults.colors(
                    checkedColor = BluePrimary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
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
                        color = BluePrimary.copy(alpha = if (podeEntregar) 1f else 0.6f) // AZUL aqui
                    )

                    if (jaEntregue) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Entregue", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = BluePrimary,
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
                            BluePrimary // AZUL aqui em vez de rosa
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

@Composable
fun EntregaScreen(
    numeroPedido: String,
    nomeCliente: String,
    setoresSelecionados: Set<String>,
    onVoltarClick: () -> Unit = {},
    onRealizarEntregaClick: (Set<String>) -> Unit = {},
    viewModel: EntregaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    var senhaDigitada by remember { mutableStateOf("") }

    // Estado para controlar a visibilidade do modal de sucesso e loading
    var showSucessoModal by remember { mutableStateOf(false) }
    var showLoadingModal by remember { mutableStateOf(false) }

    // Buscar produtos quando a tela for carregada
    LaunchedEffect(numeroPedido, setoresSelecionados) {
        if (numeroPedido.isNotBlank() && setoresSelecionados.isNotEmpty()) {
            viewModel.buscarProdutosEntrega(numeroPedido, setoresSelecionados)
        }
    }

    // Verificar se todos os produtos foram entregues
    val produtosParaEntregar = uiState.produtos.filter { it.podeEntregar() }
    val todosEntregues = produtosParaEntregar.isEmpty() && uiState.produtos.isNotEmpty()

    // Mostrar modal quando todos estiverem entregues (apenas uma vez)
    LaunchedEffect(todosEntregues) {
        if (todosEntregues && !showSucessoModal) {
            showSucessoModal = true
        }
    }

    // Box principal que ocupa toda a tela
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar com tema azul
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Registrar Entrega",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pedido: $numeroPedido",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVoltarClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = Color.White
                )
            )

            AnimatedScreen {
                if (uiState.isLoading) {
                    // Loading State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                strokeWidth = 6.dp,
                                color = BluePrimary
                            )
                            Text(
                                text = "Carregando produtos para entrega...",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                    val currentError = uiState.error

                    if (currentError != null) {
                        // Error State
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AnimatedCard(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "Erro",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    Text(
                                        text = currentError,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }

                            ModernButton(
                                onClick = onVoltarClick,
                                isPrimary = false
                            ) {
                                Text("Voltar")
                            }
                        }
                    } else {
                        // Success State - Layout principal
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Header com informações
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Card do cliente com tema azul
                                AnimatedCard(
                                    colors = CardDefaults.cardColors(
                                        containerColor = BlueContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = BlueOnContainer
                                        )
                                        Text(
                                            text = nomeCliente,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BlueOnContainer
                                        )
                                    }
                                }

                                // Card com estatísticas
                                AnimatedCard {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        EntregaStatisticItem(
                                            icon = Icons.Default.Inventory,
                                            label = "Total",
                                            value = uiState.produtos.size.toString()
                                        )
                                        EntregaStatisticItem(
                                            icon = Icons.Default.CheckCircle,
                                            label = "Selecionados",
                                            value = uiState.produtosSelecionados.size.toString()
                                        )
                                        EntregaStatisticItem(
                                            icon = Icons.Default.LocalShipping,
                                            label = "Para Entregar",
                                            value = uiState.produtos.count { it.podeEntregar() }.toString()
                                        )
                                        EntregaStatisticItem(
                                            icon = Icons.Default.Done,
                                            label = "Entregues",
                                            value = uiState.produtos.count { it.jaEntregue() }.toString()
                                        )
                                    }
                                }
                            }

                            // Lista de produtos
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(
                                    start = 24.dp,
                                    end = 24.dp,
                                    top = 8.dp,
                                    bottom = 140.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.produtos) { produto ->
                                    // Estado local para controlar se mostra a imagem para este produto específico
                                    var showProductImageDialog by remember { mutableStateOf(false) }

                                    // O dialog associado a este item específico
                                    if (showProductImageDialog) {
                                        ProductImageDialogBase64(
                                            isVisible = true,
                                            codigoProduto = produto.produto,
                                            productName = produto.descricao,
                                            onDismiss = { showProductImageDialog = false }
                                        )
                                    }

                                    ProdutoCompactCard(
                                        produto = produto,
                                        isSelected = uiState.produtosSelecionados.contains(produto.produto),
                                        onToggleSelection = {
                                            viewModel.toggleProdutoSelecionado(produto.produto)
                                        },
                                        onViewImage = {
                                            showProductImageDialog = true
                                        }
                                    )



                                }
                            }
                        }
                    }
                }
            }
        }

        // BOTÃO FLUTUANTE com tema azul
        if (uiState.produtos.isNotEmpty() && uiState.produtosSelecionados.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                shadowElevation = 16.dp,
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Informações de seleção com tema azul
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = BlueContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${uiState.produtosSelecionados.size} produto(s) selecionado(s) para entrega",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BlueOnContainer
                            )
                        }
                    }

                    // Botão principal com tema azul
                    Button(
                        onClick = {
                            showLoadingModal = true
                            viewModel.mostrarDialogValidacao()
                        },
                        enabled = !dialogState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary
                        )
                    ) {
                        if (dialogState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Realizando...")
                        } else {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Realizar Entrega",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog de validação de senha
    ValidacaoSenhaDialog(
        isVisible = dialogState.isVisible,
        isLoading = dialogState.isLoading,
        nomeUsuario = dialogState.nomeUsuario,
        erro = dialogState.erro,
        onSenhaChange = { senhaDigitada = it },
        onConfirmar = {
            if (dialogState.nomeUsuario != null) {
                viewModel.confirmarEntrega(
                    onSuccess = {
                        showLoadingModal = false
                        onRealizarEntregaClick(uiState.produtosSelecionados)
                    },
                    onError = { error ->
                        showLoadingModal = false
                        // Erro será tratado pelo dialog
                    }
                )
            } else {
                viewModel.validarSenha(senhaDigitada)
            }
        },
        onCancelar = {
            showLoadingModal = false
            viewModel.ocultarDialogValidacao()
            senhaDigitada = ""
        },
        onDismiss = {
            showLoadingModal = false
            viewModel.ocultarDialogValidacao()
            senhaDigitada = ""
        }
    )

    // Modal de loading personalizado para entrega
    EntregaLoadingModal(
        isVisible = showLoadingModal && dialogState.isLoading && dialogState.nomeUsuario != null,
        onDismiss = { }
    )

    // Modal de sucesso para todos entregues
    TodosEntreguesDialog(
        isVisible = showSucessoModal,
        onDismiss = { showSucessoModal = false }
    )

    // Dialog de sucesso da entrega
    EntregaSucessoDialog(
        isVisible = uiState.showEntregaSucesso,
        quantidadeProdutos = uiState.quantidadeEntregaRealizada,
        onDismiss = { viewModel.limparEntregaSucesso() }
    )
}

@Composable
fun EntregaStatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = BluePrimary // Mudado para azul
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BluePrimary // Mudado para azul
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EntregaScreenPreview() {
    SeparadorPedidosTheme {
        EntregaScreen(
            numeroPedido = "PED-001",
            nomeCliente = "Cliente Exemplo Ltda",
            setoresSelecionados = setOf("Pré Montagem", "Laser")
        )
    }
}