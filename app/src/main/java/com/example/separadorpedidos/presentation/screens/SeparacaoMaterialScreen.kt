package com.example.separadorpedidos.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.separadorpedidos.data.model.FiltroLocal
import com.example.separadorpedidos.presentation.viewmodel.SeparacaoViewModel
import com.example.separadorpedidos.ui.components.*
import com.example.separadorpedidos.ui.theme.SeparadorPedidosTheme

@OptIn(ExperimentalMaterial3Api::class)
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

    // Buscar produtos quando a tela for carregada
    LaunchedEffect(numeroPedido, setoresSelecionados) {
        if (numeroPedido.isNotBlank() && setoresSelecionados.isNotEmpty()) {
            viewModel.buscarProdutosSeparacao(numeroPedido, setoresSelecionados)
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
                    // Success State
                    // VERIFICAR SE TODOS OS PRODUTOS FORAM SEPARADOS
                    val produtosParaSeparar = uiState.produtos.filter { it.podeSelecionar() }

                    if (produtosParaSeparar.isEmpty() && uiState.produtos.isNotEmpty()) {
                        // MOSTRAR ANIMAÇÃO - TODOS SEPARADOS
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            TodosSeparadosAnimation()

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

                            // Lista de produtos COMPACTA
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.produtos) { produto ->
                                    ProdutoSeparacaoCompactCard(
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
                                            text = "${uiState.produtosSelecionados.size} produtos selecionados",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    ModernButton(
                                        onClick = { onRealizarBaixaClick(uiState.produtosSelecionados) },
                                        enabled = uiState.produtosSelecionados.isNotEmpty()
                                    ) {
                                        Icon(
                                            Icons.Default.Done,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Realizar Baixa/Separação")
                                    }
                                }
                            }
                        }
                    }
                }
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