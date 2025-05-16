package com.example.separadorpedidos.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.separadorpedidos.presentation.viewmodel.EntregaViewModel
import com.example.separadorpedidos.ui.components.*
import com.example.separadorpedidos.ui.theme.SeparadorPedidosTheme

@OptIn(ExperimentalMaterial3Api::class)
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

    // Estado para controlar a visibilidade do modal de sucesso
    var showSucessoModal by remember { mutableStateOf(false) }

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
            // App Bar
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
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    titleContentColor = MaterialTheme.colorScheme.onTertiary
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
                                // Card do cliente
                                AnimatedCard(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = nomeCliente,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
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
                                    bottom = 140.dp // ESPAÇO EXTRA para o botão flutuante
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.produtos) { produto ->
                                    ProdutoCompactCard(
                                        produto = produto,
                                        isSelected = uiState.produtosSelecionados.contains(produto.produto),
                                        onToggleSelection = {
                                            viewModel.toggleProdutoSelecionado(produto.produto)
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
                shape = MaterialTheme.shapes.large,
                shadowElevation = 16.dp,
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
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${uiState.produtosSelecionados.size} produto(s) selecionado(s) para entrega",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    // Botão principal
                    Button(
                        onClick = {
                            viewModel.mostrarDialogValidacao()
                        },
                        enabled = !dialogState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        if (dialogState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onTertiary,
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
                        onRealizarEntregaClick(uiState.produtosSelecionados)
                    },
                    onError = { error ->
                        // Erro será tratado pelo dialog
                    }
                )
            } else {
                viewModel.validarSenha(senhaDigitada)
            }
        },
        onCancelar = {
            viewModel.ocultarDialogValidacao()
            senhaDigitada = ""
        },
        onDismiss = {
            viewModel.ocultarDialogValidacao()
            senhaDigitada = ""
        }
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
            tint = MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
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