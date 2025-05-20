// HistoricoScreen.kt
package com.example.separadorpedidos.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.separadorpedidos.data.model.RegistroEntrega
import com.example.separadorpedidos.presentation.viewmodel.HistoricoViewModel
import com.example.separadorpedidos.ui.components.AnimatedScreen
import com.example.separadorpedidos.ui.theme.SeparadorPedidosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(
    numeroPedido: String,
    nomeCliente: String,
    onVoltarClick: () -> Unit = {},
    viewModel: HistoricoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Buscar histórico quando a tela for carregada
    LaunchedEffect(numeroPedido) {
        viewModel.buscarHistorico(numeroPedido)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Histórico do Pedido",
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
                            text = "Carregando histórico...",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else if (uiState.error != null) {
                // Error State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )

                        Text(
                            text = uiState.error ?: "Erro desconhecido",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { viewModel.buscarHistorico(numeroPedido) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tentar Novamente")
                        }
                    }
                }
            } else {
                // Content State
                val historico = uiState.historicoResponse

                if (historico == null) {
                    // Sem dados
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma informação disponível",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else if (!uiState.entregaIniciada) {
                    // Entrega não iniciada
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "Entrega Não Iniciada",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Este pedido ainda não teve nenhuma operação de entrega registrada.",
                                    textAlign = TextAlign.Center
                                )

                                // Detalhes do pedido em cards de datas
                                DatasInfoCard(historico)
                            }
                        }
                    }
                } else {
                    // Histórico completo
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card com informações do cliente
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Cliente",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = nomeCliente,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                if (historico.notaFiscal?.isNotBlank() == true &&
                                    historico.notaFiscal != "000000000") {
                                    Divider()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )

                                        Text(
                                            text = "Nota Fiscal: ${historico.notaFiscal}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Card com datas importantes
                        DatasInfoCard(historico)

                        // Seção de entregas
                        if (!historico.listaEntrega.isNullOrEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Título
                                Text(
                                    text = "Histórico de Entregas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                // Lista de entregas
                                historico.listaEntrega.forEach { entrega ->
                                    EntregaItemCard(entrega)
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
fun DatasInfoCard(historico: com.example.separadorpedidos.data.model.HistoricoResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Datas do Pedido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Grid de datas
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Linha 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DataItem(
                        label = "Data de Venda",
                        data = historico.formatarData(historico.dataVenda),
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f)
                    )

                    DataItem(
                        label = "Data de Inclusão",
                        data = historico.formatarData(historico.dataInclusao),
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Linha 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DataItem(
                        label = "Liberação PCP",
                        data = historico.formatarData(historico.liberacaoParaPcp),
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )

                    DataItem(
                        label = "Geração OP",
                        data = historico.formatarData(historico.dataGeracaoOp),
                        icon = Icons.Default.Description,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Linha 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DataItem(
                        label = "Início Embalagem",
                        data = historico.formatarData(historico.dataInicioEmbalagem),
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DataItem(
    label: String,
    data: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = data,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EntregaItemCard(
    entrega: RegistroEntrega
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // OP e data
                Column {
                    Text(
                        text = "OP: ${entrega.ordemProducao}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = entrega.formatarDataRetirada(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quantidade com círculo destacado
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entrega.formatarQuantidade(), // Usando o novo método formatarQuantidade()
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Divider()

            // Produto
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Produto: ${entrega.codigoProduto}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = entrega.descProduto,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Setor e colaborador
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Setor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = entrega.setor,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Colaborador
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = entrega.colaborador,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/*@Preview(showBackground = true)
@Composable
fun HistoricoScreenPreview() {
    SeparadorPedidosTheme {
        // Criar um HistoricoResponse simulado para preview
        val historico = com.example.separadorpedidos.data.model.HistoricoResponse(
            dataVenda = "20250101",
            dataInclusao = "20250102",
            liberacaoParaPcp = "20250103",
            dataGeracaoOp = "20250104",
            dataInicioEmbalagem = "20250105",
            notaFiscal = "123456789",
            listaEntrega = listOf(
                com.example.separadorpedidos.data.model.RegistroEntrega(
                    ordemProducao = "OP123456",
                    codigoProduto = "500100001",
                    descProduto = "MESA DE ESCRITÓRIO 1.20M",
                    quantidade = 10,
                    dataRetirada = "20250120",
                    colaborador = "CARLOS",
                    setor = "PREMONTAGEM"
                ),
                com.example.separadorpedidos.data.model.RegistroEntrega(
                    ordemProducao = "OP123457",
                    codigoProduto = "500100002",
                    descProduto = "CADEIRA GIRATÓRIA",
                    quantidade = 5,
                    dataRetirada = "20250121",
                    colaborador = "MARIA",
                    setor = "ESTOFARIA"
                )
            )
        )

        // Tela de preview
        Box(modifier = Modifier.padding(top = 56.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DatasInfoCard(historico)
                Text(
                    text = "Histórico de Entregas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                historico.listaEntrega?.forEach { entrega ->
                    EntregaItemCard(entrega)
                }
            }
        }
    }
}*/