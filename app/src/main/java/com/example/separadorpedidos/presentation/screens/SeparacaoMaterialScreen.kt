@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.separadorpedidos.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.separadorpedidos.data.model.FiltroLocal
import com.example.separadorpedidos.presentation.viewmodel.SeparacaoViewModel
import com.example.separadorpedidos.ui.components.*
import com.example.separadorpedidos.ui.theme.SeparadorPedidosTheme

// Cores para tema de separação
val GreenPrimary = Color(0xFF4CAF50)
val GreenContainer = Color(0xFFE8F5E9)

@Composable
fun SeparacaoMaterialScreen(
    numeroPedido: String,
    nomeCliente: String,
    setoresSelecionados: Set<String>,
    onVoltarClick: () -> Unit = {},
    onRealizarBaixaClick: (Set<String>) -> Unit = {},
    viewModel: SeparacaoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estado para controlar a animação de loading
    var showLoadingModal by remember { mutableStateOf(false) }

    // Estado para controlar a visibilidade do modal de sucesso
    var showSucessoModal by remember { mutableStateOf(false) }

    // Estados para o dialog de imagem
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<com.example.separadorpedidos.data.model.ProdutoSeparacao?>(null) }

    // Buscar produtos quando a tela for carregada
    LaunchedEffect(numeroPedido, setoresSelecionados) {
        if (numeroPedido.isNotBlank() && setoresSelecionados.isNotEmpty()) {
            viewModel.buscarProdutosSeparacao(numeroPedido, setoresSelecionados)
        }
    }

    // Verificar se todos os produtos foram separados
    val produtosParaSeparar = uiState.produtos.filter { it.podeSelecionar() }
    val todosSeparados = produtosParaSeparar.isEmpty() && uiState.produtos.isNotEmpty()

    // Mostrar modal quando todos estiverem separados (apenas uma vez)
    LaunchedEffect(todosSeparados) {
        if (todosSeparados && !showSucessoModal) {
            showSucessoModal = true
        }
    }

    // Observar o estado de loading da baixa
    LaunchedEffect(uiState.isBaixaLoading) {
        showLoadingModal = uiState.isBaixaLoading
    }

    // Observar erros de baixa
    uiState.baixaError?.let { error ->
        LaunchedEffect(error) {
            showLoadingModal = false
            viewModel.limparBaixaError()
        }
    }

    // Box principal que ocupa toda a tela
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Separação de Material",
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
                                strokeWidth = 6.dp
                            )
                            Text(
                                text = "Carregando produtos...",
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
                                // Card do cliente
                                AnimatedCard(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = nomeCliente,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
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
                                        StatisticItem(
                                            icon = Icons.Default.Inventory,
                                            label = "Total",
                                            value = uiState.produtosTodos.size.toString()
                                        )
                                        StatisticItem(
                                            icon = Icons.Default.CheckCircle,
                                            label = "Selecionados",
                                            value = uiState.produtosSelecionados.size.toString()
                                        )
                                        StatisticItem(
                                            icon = Icons.Default.Done,
                                            label = "Separados",
                                            value = uiState.produtos.count { it.jaSeparado() }.toString()
                                        )
                                        StatisticItem(
                                            icon = Icons.Default.FilterList,
                                            label = "Filtrados",
                                            value = uiState.produtos.size.toString()
                                        )
                                    }
                                }

                                // Filtros de Local
                                if (uiState.filtrosLocais.isNotEmpty()) {
                                    AnimatedCard {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "Filtrar por Local",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                items(uiState.filtrosLocais) { filtro ->
                                                    FilterChip(
                                                        onClick = {
                                                            viewModel.aplicarFiltroLocal(filtro.codigo)
                                                        },
                                                        label = {
                                                            Text("${filtro.descricao} (${filtro.quantidadeProdutos})")
                                                        },
                                                        selected = uiState.filtroSelecionado == filtro.codigo,
                                                        leadingIcon = if (uiState.filtroSelecionado == filtro.codigo) {
                                                            {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                        } else null
                                                    )
                                                }
                                            }
                                        }
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
                                    bottom = 140.dp // ESPAÇO EXTRA para o botão flutuante
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.produtos) { produto ->
                                    ProdutoSeparacaoCompactCard(
                                        produto = produto,
                                        isSelected = uiState.produtosSelecionados.contains(produto.produto),
                                        onToggleSelection = {
                                            viewModel.toggleProdutoSelecionado(produto.produto)
                                        },
                                        onViewImage = {
                                            selectedProduct = produto
                                            showImageDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // BOTÃO FLUTUANTE - SEMPRE VISÍVEL NO FUNDO
        if (uiState.produtos.isNotEmpty() && uiState.produtosSelecionados.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 16.dp, // Elevação alta para ficar acima de tudo
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Informações de seleção
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${uiState.produtosSelecionados.size} produto(s) selecionado(s)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Botão principal
                    Button(
                        onClick = {
                            showLoadingModal = true
                            viewModel.realizarBaixaSeparacao(
                                onSuccess = { },
                                onError = { }
                            )
                        },
                        enabled = !uiState.isBaixaLoading && !showLoadingModal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isBaixaLoading || showLoadingModal) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Realizando...")
                        } else {
                            Icon(
                                Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Realizar Baixa/Separação",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog de visualização de imagem
    selectedProduct?.let { produto ->
        ProductImageDialogBase64(
            isVisible = showImageDialog,
            codigoProduto = produto.produto,
            productName = produto.descricao,
            onDismiss = {
                showImageDialog = false
                selectedProduct = null
            }
        )
    }

    // Modal de loading personalizado para separação
    SeparacaoLoadingModal(
        isVisible = showLoadingModal,
        onDismiss = { }
    )

    // Modal de sucesso
    TodosSeparadosDialog(
        isVisible = showSucessoModal,
        onDismiss = { showSucessoModal = false }
    )

    // Dialog de sucesso da baixa
    BaixaSucessoDialog(
        isVisible = uiState.showBaixaSucesso,
        quantidadeProdutos = uiState.quantidadeBaixaRealizada,
        onDismiss = {
            showLoadingModal = false
            viewModel.limparBaixaSucesso()
        }
    )

    // Mostrar erro de baixa se houver
    uiState.baixaError?.let { error ->
        AlertDialog(
            onDismissRequest = {
                showLoadingModal = false
                viewModel.limparBaixaError()
            },
            title = { Text("Erro na Baixa/Separação") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = {
                    showLoadingModal = false
                    viewModel.limparBaixaError()
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SeparacaoLoadingModal(
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
                SeparacaoLoadingContent()
            }
        }
    }
}

@Composable
private fun SeparacaoLoadingContent() {
    // Animações para os círculos pulsantes
    val infiniteTransition = rememberInfiniteTransition(label = "separacaoLoading")

    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale1"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale2"
    )

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha1"
    )

    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha2"
    )

    // Animação de rotação múltipla para os ícones
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation1"
    )

    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation2"
    )

    Card(
        modifier = Modifier
            .size(300.dp)
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
                    .size(200.dp)
                    .scale(scale1)
                    .background(
                        color = GreenPrimary.copy(alpha = alpha1 * 0.2f),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(scale2)
                    .background(
                        color = GreenPrimary.copy(alpha = alpha2 * 0.3f),
                        shape = CircleShape
                    )
            )

            // Conteúdo central
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Container com ícones rotativos
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Ícone de inventário (rotação lenta)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .rotate(rotation1)
                            .background(
                                color = GreenPrimary.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(35.dp),
                            tint = GreenPrimary
                        )
                    }

                    // Ícone de check (rotação rápida, menor)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .rotate(rotation2)
                            .offset(x = 25.dp, y = (-25).dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Textos
                Text(
                    text = "Realizando Baixa",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )

                Text(
                    text = "Aguarde enquanto processamos a separação dos materiais...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Barra de progresso indeterminada
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(140.dp)
                        .height(4.dp),
                    color = GreenPrimary,
                    trackColor = GreenPrimary.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
fun StatisticItem(
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
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
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
fun SeparacaoMaterialScreenPreview() {
    SeparadorPedidosTheme {
        SeparacaoMaterialScreen(
            numeroPedido = "PED-001",
            nomeCliente = "Cliente Exemplo Ltda",
            setoresSelecionados = setOf("Pré Montagem", "Laser")
        )
    }
}