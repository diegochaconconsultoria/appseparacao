/*package com.example.separadorpedidos.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sua_empresa.separador_pedidos.data.model.*

@Composable
fun ItemsPedidoScreen(
    pedido: Pedido,
    onMarcarComoSeparado: (ItemPedido) -> Unit = {},
    onVerificarLocalizacao: (ItemPedido) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header do pedido
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Pedido ${pedido.numero}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Status: ${pedido.status.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when(pedido.status) {
                        StatusPedido.PENDENTE -> MaterialTheme.colorScheme.error
                        StatusPedido.SEPARANDO -> MaterialTheme.colorScheme.tertiary
                        StatusPedido.SEPARADO -> MaterialTheme.colorScheme.primary
                        StatusPedido.EM_ENTREGA -> MaterialTheme.colorScheme.secondary
                        StatusPedido.ENTREGUE -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de itens
        Text(
            text = "Itens do Pedido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pedido.items) { item ->
                ItemCard(
                    item = item,
                    onMarcarComoSeparado = { onMarcarComoSeparado(item) },
                    onVerificarLocalizacao = { onVerificarLocalizacao(item) }
                )
            }
        }
    }
}

@Composable
fun ItemCard(
    item: ItemPedido,
    onMarcarComoSeparado: () -> Unit,
    onVerificarLocalizacao: () -> Unit
) {
    val isSeparado = item.quantidadeSeparada >= item.quantidade

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSeparado)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.produto.nome,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Código: ${item.produto.codigo}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Setor: ${item.setor.nome}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (isSeparado) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Separado",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quantidade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quantidade: ${item.quantidade}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Separado: ${item.quantidadeSeparada}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSeparado)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            // Localização
            item.localizacao?.let { localizacao ->
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = localizacao.nome,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Botões
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.localizacao != null) {
                    OutlinedButton(
                        onClick = onVerificarLocalizacao,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocationOn, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver no Mapa")
                    }
                }

                if (!isSeparado) {
                    Button(
                        onClick = onMarcarComoSeparado,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Marcar Separado")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemsPedidoScreenPreview() {
    val pedidoExemplo = Pedido(
        id = "1",
        numero = "PED-001",
        items = listOf(
            ItemPedido(
                id = "1",
                produto = Produto(
                    id = "P1",
                    nome = "Parafuso M8",
                    codigo = "PAR001"
                ),
                quantidade = 50,
                quantidadeSeparada = 0,
                localizacao = Localizacao(
                    id = "L1",
                    nome = "Prateleira A-1"
                ),
                setor = Setor(
                    id = "S1",
                    nome = "Almoxarifado"
                )
            ),
            ItemPedido(
                id = "2",
                produto = Produto(
                    id = "P2",
                    nome = "Porca M8",
                    codigo = "POR001"
                ),
                quantidade = 50,
                quantidadeSeparada = 50,
                localizacao = Localizacao(
                    id = "L2",
                    nome = "Prateleira A-2"
                ),
                setor = Setor(
                    id = "S1",
                    nome = "Almoxarifado"
                )
            )
        ),
        status = StatusPedido.SEPARANDO
    )

    MaterialTheme {
        ItemsPedidoScreen(pedido = pedidoExemplo)
    }
}*/