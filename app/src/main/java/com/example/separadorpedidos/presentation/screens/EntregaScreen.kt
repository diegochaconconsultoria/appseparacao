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

    // Buscar produtos quando a tela for carregada
    LaunchedEffect(numeroPedido, setoresSelecionados) {
        if (numeroPedido.isNotBlank() && setoresSelecionados.isNotEmpty()) {
            viewModel.buscarProdutosEntrega(numeroPedido, setoresSelecionados)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
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
                    // Success State
                    // VERIFICAR SE TODOS OS PRODUTOS FORAM ENTREGUES
                    val produtosParaEntregar = uiState.produtos.filter { it.podeEntregar() }

                    if (produtosParaEntregar.isEmpty() && uiState.produtos.isNotEmpty()) {
                        // MOSTRAR ANIMAÇÃO - TODOS ENTREGUES
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            TodosEntreguesAnimation()

                            // Botão para voltar
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 8.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    ModernButton(
                                        onClick = onVoltarClick,
                                        isPrimary = true
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowBack,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Voltar")
                                    }
                                }
                            }
                        }
                    } else {
                        // MOSTRAR LISTA NORMAL DE PRODUTOS
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

                            // Lista de produtos COMPACTA
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 24.dp),
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

                                // Espaço extra no final
                                item {
                                    Spacer(modifier = Modifier.height(100.dp))
                                }
                            }
                        }

                        // Bottom Bar com botão de ação
                        if (uiState.produtos.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 8.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (uiState.produtosSelecionados.isNotEmpty()) {
                                        Text(
                                            text = "${uiState.produtosSelecionados.size} produtos selecionados para entrega",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    ModernButton(
                                        onClick = {
                                            viewModel.mostrarDialogValidacao()
                                        },
                                        enabled = uiState.produtosSelecionados.isNotEmpty()
                                    ) {
                                        Icon(
                                            Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Realizar Entrega")
                                    }
                                }
                            }
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
                viewModel.confirmarEntrega {
                    onRealizarEntregaClick(uiState.produtosSelecionados)
                }
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