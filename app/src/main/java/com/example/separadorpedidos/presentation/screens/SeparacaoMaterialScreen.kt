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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.separadorpedidos.data.model.FiltroLocal
import com.example.separadorpedidos.data.model.ProdutoSeparacao
import com.example.separadorpedidos.presentation.viewmodel.SeparacaoViewModel
import com.example.separadorpedidos.ui.components.*
import com.example.separadorpedidos.ui.theme.SeparadorPedidosTheme
// Adicione estas importações no topo do arquivo:
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType



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
    val context = LocalContext.current

    // Estado para controlar a animação de loading
    var showLoadingModal by remember { mutableStateOf(false) }

    // Estado para controlar a visibilidade do modal de sucesso
    var showSucessoModal by remember { mutableStateOf(false) }

    // Estado para controlar a visibilidade do diálogo de confirmação do email
    var showConfirmEmailDialog by remember { mutableStateOf(false) }

    // Estados para o dialog de imagem
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProdutoSeparacao?>(null) }

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

                                // Card com estatísticas e botão de comunicar falta
                                AnimatedCard {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Estatísticas existentes
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            // Item "Total" modificado para ser clicável
                                            val produtosSelecionaveis = uiState.produtos
                                                .filter { it.podeSelecionar() }
                                                .map { it.produto }
                                                .toSet()

                                            val todosSelecionados = uiState.produtosSelecionados.size == produtosSelecionaveis.size &&
                                                    uiState.produtosSelecionados.containsAll(produtosSelecionaveis)

                                            // StatisticItem clicável para Total
                                            ClickableStatisticItem(
                                                icon = Icons.Default.Inventory,
                                                label = "Total" + (if (todosSelecionados) " (Desmarcar)" else " (Marcar)"),
                                                value = uiState.produtosTodos.size.toString(),
                                                onClick = { viewModel.toggleSelecionarTodos() }
                                            )

                                            // Outros StatisticItems normais
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

                                        // Separador
                                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                                        // Botão para comunicar falta de materiais
                                        Button(
                                            onClick = {
                                                // Verificar se há produtos selecionados
                                                if (uiState.produtosSelecionados.isNotEmpty()) {
                                                    showConfirmEmailDialog = true
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !uiState.isEmailSending && uiState.produtosSelecionados.isNotEmpty(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError
                                            )
                                        ) {
                                            if (uiState.isEmailSending) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = MaterialTheme.colorScheme.onError,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Enviando...")
                                            } else {
                                                Icon(
                                                    Icons.Default.WarningAmber,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Comunicar Falta de Materiais")
                                            }
                                        }
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

                            // Botão de Selecionar Todos (novo componente)
                            /*if (uiState.produtos.isNotEmpty()) {
                                AnimatedCard {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Verificar se todos os produtos selecionáveis estão selecionados
                                        val produtosSelecionaveis = uiState.produtos
                                            .filter { it.podeSelecionar() }
                                            .map { it.produto }
                                            .toSet()

                                        val todosSelecionados = uiState.produtosSelecionados.size == produtosSelecionaveis.size &&
                                                uiState.produtosSelecionados.containsAll(produtosSelecionaveis)

                                        val numSelecionados = uiState.produtosSelecionados.size
                                        val numSelecionaveis = produtosSelecionaveis.size

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Checkbox(
                                                checked = todosSelecionados,
                                                onCheckedChange = { viewModel.toggleSelecionarTodos() },
                                                enabled = produtosSelecionaveis.isNotEmpty()
                                            )

                                            Text(
                                                text = if (todosSelecionados) "Desmarcar Todos" else "Marcar Todos",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        // Contador de seleção
                                        Text(
                                            text = "$numSelecionados de $numSelecionaveis selecionados",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }*/

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
                            // Mostrar diálogo de senha em vez de iniciar a baixa diretamente
                            viewModel.mostrarDialogSenha()
                        },
                        enabled = !uiState.isBaixaLoading && !showLoadingModal && uiState.produtosSelecionados.isNotEmpty(),
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

    EmailSendingDialog(isVisible = uiState.isEmailSending)

    // Lista de produtos selecionados para o diálogo de confirmação do email
    val produtosSelecionadosParaDialogo = uiState.produtosTodos.filter { produto ->
        uiState.produtosSelecionados.contains(produto.produto)
    }

    // Diálogo de confirmação de email
    ConfirmEmailDialog(
        isVisible = showConfirmEmailDialog,
        produtosSelecionados = produtosSelecionadosParaDialogo,
        onConfirm = {
            viewModel.enviarEmailFaltaMateriais(context, nomeCliente)
            showConfirmEmailDialog = false
        },
        onDismiss = {
            showConfirmEmailDialog = false
        }
    )

    // Diálogo de sucesso do email
    if (uiState.emailSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.limparEmailSuccess() },
            title = { Text("Comunicação Enviada") },
            text = { Text("A comunicação de falta de materiais foi enviada com sucesso!") },
            confirmButton = {
                Button(onClick = { viewModel.limparEmailSuccess() }) {
                    Text("OK")
                }
            }
        )
    }

    // Diálogo de erro do email
    if (uiState.emailError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.limparEmailError() },
            title = { Text("Erro") },
            text = { Text(uiState.emailError ?: "Ocorreu um erro desconhecido.") },
            confirmButton = {
                Button(onClick = { viewModel.limparEmailError() }) {
                    Text("OK")
                }
            }
        )
    }

    // Diálogo de validação de senha
    var senhaDigitada by remember { mutableStateOf("") }
    ValidacaoSenhaDialogSeparacao(
        isVisible = uiState.isPasswordDialogVisible,
        isLoading = uiState.isPasswordLoading,
        nomeUsuario = uiState.validatedUserName,
        erro = uiState.passwordError,
        onSenhaChange = { senhaDigitada = it },
        onConfirmar = {
            if (uiState.validatedUserName != null) {
                // Se já validou a senha, realizar a baixa
                viewModel.realizarBaixaSeparacao(
                    onSuccess = {},
                    onError = {}
                )
            } else {
                // Senão, validar a senha
                viewModel.validarSenha(senhaDigitada)
            }
        },
        onCancelar = {
            viewModel.ocultarDialogSenha()
            senhaDigitada = ""
        },
        onDismiss = {
            viewModel.ocultarDialogSenha()
            senhaDigitada = ""
        }
    )
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

// Adicione esta função ao arquivo para criar um item de estatística clicável
@Composable
fun ClickableStatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)  // Padding para aumentar a área clicável
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun AjusteQuantidadesDialog(
    isVisible: Boolean,
    produtos: List<ProdutoSeparacao>,
    quantidadesFaltantes: Map<String, Double>,
    onQuantidadeChanged: (String, Double) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Cabeçalho
                    TopAppBar(
                        title = {
                            Text(
                                "Ajustar Quantidades Faltantes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    // Texto explicativo
                    Text(
                        text = "Ajuste a quantidade faltante para cada produto antes de enviar a comunicação.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Lista de produtos com controles de ajuste de quantidade
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(produtos) { produto ->
                            ProdutoQuantidadeAjusteItem(
                                produto = produto,
                                quantidadeAtual = quantidadesFaltantes[produto.produto] ?: produto.qtdaSeparar,
                                onQuantidadeChanged = { novaQuantidade ->
                                    onQuantidadeChanged(produto.produto, novaQuantidade)
                                }
                            )
                        }
                    }

                    // Botões de ação
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar Comunicação")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProdutoQuantidadeAjusteItem(
    produto: ProdutoSeparacao,
    quantidadeAtual: Double,
    onQuantidadeChanged: (Double) -> Unit
) {
    var quantidadeText by remember { mutableStateOf(formatQuantity(quantidadeAtual)) }
    var isError by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Informações do produto
            Text(
                text = produto.descricao,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Código: ${produto.produto}",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Quantidade Original:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = produto.getQtdOriginalFormatted(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Controle de quantidade
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quantidade Faltante:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botão de decremento
                    IconButton(
                        onClick = {
                            val valor = try {
                                val atual = quantidadeText.replace(",", ".").toDoubleOrNull() ?: quantidadeAtual
                                if (atual > 1.0) atual - 1.0 else 1.0
                            } catch (e: Exception) {
                                1.0
                            }
                            quantidadeText = formatQuantity(valor)
                            onQuantidadeChanged(valor)
                            isError = false
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                    }

                    // Campo de entrada
                    OutlinedTextField(
                        value = quantidadeText,
                        onValueChange = { newValue ->
                            quantidadeText = newValue
                            try {
                                val valor = newValue.replace(",", ".").toDoubleOrNull()
                                if (valor != null && valor > 0) {
                                    onQuantidadeChanged(valor)
                                    isError = false
                                } else {
                                    isError = true
                                }
                            } catch (e: Exception) {
                                isError = true
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = isError,
                        modifier = Modifier.width(100.dp),
                        textStyle = TextStyle(textAlign = TextAlign.Center)
                    )

                    // Botão de incremento
                    IconButton(
                        onClick = {
                            val valor = try {
                                val atual = quantidadeText.replace(",", ".").toDoubleOrNull() ?: quantidadeAtual
                                atual + 1.0
                            } catch (e: Exception) {
                                quantidadeAtual + 1.0
                            }
                            quantidadeText = formatQuantity(valor)
                            onQuantidadeChanged(valor)
                            isError = false
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar")
                    }
                }
            }
        }
    }
}

// Função auxiliar para formatar números
private fun formatQuantity(value: Double): String {
    return if (value == value.toInt().toDouble()) {
        value.toInt().toString()
    } else {
        String.format("%.2f", value).replace(",0{1,2}$".toRegex(), "")
    }
}

@Composable
fun ConfirmEmailDialog(
    isVisible: Boolean,
    produtosSelecionados: List<ProdutoSeparacao>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirmar Comunicação de Falta") },
            text = {
                Column {
                    Text(
                        "Você está prestes a enviar um e-mail informando a falta dos seguintes materiais:"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista de produtos selecionados
                    Column(
                        modifier = Modifier
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        produtosSelecionados.forEach { produto ->
                            Text(
                                "• ${produto.descricao} (${produto.produto})",
                                style = MaterialTheme.typography.bodyMedium,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Um e-mail será enviado para os responsáveis. Deseja continuar?",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EmailSendingDialog(isVisible: Boolean) {
    if (isVisible) {
        Dialog(
            onDismissRequest = { /* Não faz nada, não pode ser dispensado */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Animação do ícone de e-mail
                    val infiniteTransition = rememberInfiniteTransition(label = "emailAnimation")

                    val iconRotation by infiniteTransition.animateFloat(
                        initialValue = -10f,
                        targetValue = 10f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "rotation"
                    )

                    val iconScale by infiniteTransition.animateFloat(
                        initialValue = 0.9f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    // Animação das linhas de "enviando"
                    val dotsTransition = rememberInfiniteTransition(label = "dotsAnimation")
                    val dotsState by dotsTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "dots"
                    )

                    val dots = when {
                        dotsState < 0.33f -> "."
                        dotsState < 0.66f -> ".."
                        else -> "..."
                    }

                    // Ícone animado
                    Box(contentAlignment = Alignment.Center) {
                        // Círculo de fundo
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .scale(iconScale)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                        )

                        // Ícone de e-mail
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .rotate(iconRotation)
                                .scale(iconScale),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Texto com animação de pontos
                    Text(
                        text = "Enviando e-mail$dots",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Por favor, aguarde enquanto enviamos sua comunicação",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Indicador de progresso
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ValidacaoSenhaDialogSeparacao(
    isVisible: Boolean,
    isLoading: Boolean = false,
    nomeUsuario: String? = null,
    erro: String? = null,
    onSenhaChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    onDismiss: () -> Unit
) {
    var senha by remember { mutableStateOf("") }

    // Efeito para limpar a senha quando ocorre um erro
    LaunchedEffect(erro) {
        if (erro != null && erro.contains("sem permissão")) {
            senha = ""
            onSenhaChange("")
        }
    }

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = !isLoading,
                dismissOnClickOutside = !isLoading
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ícone e título
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = GreenPrimary  // Use a cor verde para diferenciar da tela de entrega
                    )

                    Text(
                        text = "Validação de Separação",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Campo de senha
                    OutlinedTextField(
                        value = senha,
                        onValueChange = {
                            senha = it
                            onSenhaChange(it)
                        },
                        label = { Text("Digite sua senha") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !isLoading,
                        isError = erro != null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Erro (se houver)
                    erro?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Nome do usuário e confirmação (se validado)
                    nomeUsuario?.let { nome ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = GreenContainer  // Use a cor verde para diferenciar
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = GreenPrimary  // Cor verde
                                )
                                // MENSAGEM PERSONALIZADA
                                Text(
                                    text = "Olá $nome, confirma a separação dos produtos?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Botões
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelar,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = onConfirmar,
                            enabled = !isLoading && senha.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary  // Cor verde para diferenciar
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (nomeUsuario != null) "Confirmar" else "Validar")
                        }
                    }
                }
            }
        }
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