package com.example.separadorpedidos.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.separadorpedidos.data.model.SetorDisponivel
import com.example.separadorpedidos.presentation.screens.*

sealed class Screen {
    object Main : Screen()
    data class SetorSelection(
        val numeroPedido: String,
        val nomeCliente: String,
        val setoresDisponiveis: List<SetorDisponivel>
    ) : Screen()
    data class StatusSeparacao(val numeroPedido: String, val nomeCliente: String) : Screen()
    data class SeparacaoMaterial(
        val numeroPedido: String,
        val nomeCliente: String,
        val setoresSelecionados: Set<String>
    ) : Screen()
    data class RegistrarEntrega(
        val numeroPedido: String,
        val nomeCliente: String,
        val setoresSelecionados: Set<String>
    ) : Screen()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {
    // Estados preservados na rotação (apenas tipos básicos)
    var screenType by rememberSaveable { mutableStateOf("Main") }
    var numeroPedido by rememberSaveable { mutableStateOf("") }
    var nomeCliente by rememberSaveable { mutableStateOf("") }
    var setoresSelecionados by rememberSaveable { mutableStateOf(emptySet<String>()) }

    // Este estado não persiste, mas será recriado via API se necessário
    var setoresDisponiveis by remember { mutableStateOf<List<SetorDisponivel>>(emptyList()) }

    // Recriar a tela atual baseado nos dados salvos
    val currentScreen = when (screenType) {
        "Main" -> Screen.Main
        "SetorSelection" -> Screen.SetorSelection(numeroPedido, nomeCliente, setoresDisponiveis)
        "StatusSeparacao" -> Screen.StatusSeparacao(numeroPedido, nomeCliente)
        "SeparacaoMaterial" -> Screen.SeparacaoMaterial(numeroPedido, nomeCliente, setoresSelecionados)
        "RegistrarEntrega" -> Screen.RegistrarEntrega(numeroPedido, nomeCliente, setoresSelecionados)
        else -> Screen.Main
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) with slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            )
        }
    ) { screen ->
        when (screen) {
            Screen.Main -> {
                MainScreen(
                    onPedidoEncontrado = { pedido, cliente, setores ->
                        screenType = "SetorSelection"
                        numeroPedido = pedido
                        nomeCliente = cliente
                        setoresDisponiveis = setores
                    }
                )
            }

            is Screen.SetorSelection -> {
                SetorSelectionScreen(
                    numeroPedido = screen.numeroPedido,
                    nomeCliente = screen.nomeCliente,
                    setoresDisponiveis = screen.setoresDisponiveis,
                    onVoltarClick = {
                        screenType = "Main"
                        numeroPedido = ""
                        nomeCliente = ""
                        setoresDisponiveis = emptyList()
                        setoresSelecionados = emptySet()
                    },
                    onContinuarClick = { setores ->
                        screenType = "StatusSeparacao"
                        setoresSelecionados = setores
                    }
                )
            }

            is Screen.StatusSeparacao -> {
                StatusSeparacaoScreen(
                    numeroPedido = screen.numeroPedido,
                    nomeCliente = screen.nomeCliente,
                    onVoltarClick = {
                        // Se há setores disponíveis, volta para seleção, senão vai para Main
                        if (setoresDisponiveis.isNotEmpty()) {
                            screenType = "SetorSelection"
                        } else {
                            screenType = "Main"
                            numeroPedido = ""
                            nomeCliente = ""
                            setoresSelecionados = emptySet()
                        }
                    },
                    onSepararMaterialClick = {
                        screenType = "SeparacaoMaterial"
                    },
                    onRegistrarEntregaClick = {
                        screenType = "RegistrarEntrega"
                    },
                    onVisualizarHistoricoClick = {
                        // TODO: Implementar tela de histórico
                    }
                )
            }

            is Screen.SeparacaoMaterial -> {
                SeparacaoMaterialScreen(
                    numeroPedido = screen.numeroPedido,
                    nomeCliente = screen.nomeCliente,
                    setoresSelecionados = screen.setoresSelecionados,
                    onVoltarClick = {
                        screenType = "StatusSeparacao"
                    },
                    onRealizarBaixaClick = { produtosSelecionados ->
                        // TODO: Implementar API de baixa/separação
                        println("Produtos selecionados para baixa: $produtosSelecionados")
                    }
                )
            }

            is Screen.RegistrarEntrega -> {
                EntregaScreen(
                    numeroPedido = screen.numeroPedido,
                    nomeCliente = screen.nomeCliente,
                    setoresSelecionados = screen.setoresSelecionados,
                    onVoltarClick = {
                        screenType = "StatusSeparacao"
                    },
                    onRealizarEntregaClick = { produtosSelecionados ->
                        // TODO: Implementar API de entrega
                        println("Produtos selecionados para entrega: $produtosSelecionados")
                    }
                )
            }
        }
    }
}