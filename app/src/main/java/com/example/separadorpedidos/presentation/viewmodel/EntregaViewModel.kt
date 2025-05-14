package com.example.separadorpedidos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.separadorpedidos.data.api.NetworkModule
import com.example.separadorpedidos.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EntregaViewModel : ViewModel() {

    private val apiService = NetworkModule.apiService

    private val _uiState = MutableStateFlow(EntregaUiState())
    val uiState: StateFlow<EntregaUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow(DialogState())
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()

    fun buscarProdutosEntrega(pedido: String, setoresSelecionados: Set<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val setoresFormatados = CodigoSetores.formatarSetores(setoresSelecionados)
                val request = EntregaRequest(pedido, setoresFormatados)
                val response = apiService.buscarProdutosEntrega(request)

                if (response.isSuccessful) {
                    val entregaResponse = response.body()

                    if (entregaResponse?.success == true) {
                        val produtos = entregaResponse.produtos ?: emptyList()

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            produtos = produtos,
                            produtosSelecionados = emptySet(),
                            pedido = pedido,
                            setoresSelecionados = setoresSelecionados
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Nenhum produto encontrado para entrega nos setores selecionados"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erro na comunicação com o servidor"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erro de conexão: ${e.message}"
                )
            }
        }
    }

    fun toggleProdutoSelecionado(codigoProduto: String) {
        val produto = _uiState.value.produtos.find { it.produto == codigoProduto }
        if (produto?.podeEntregar() != true) return

        val produtosSelecionados = _uiState.value.produtosSelecionados.toMutableSet()

        if (produtosSelecionados.contains(codigoProduto)) {
            produtosSelecionados.remove(codigoProduto)
        } else {
            produtosSelecionados.add(codigoProduto)
        }

        _uiState.value = _uiState.value.copy(
            produtosSelecionados = produtosSelecionados
        )
    }

    fun mostrarDialogValidacao() {
        _dialogState.value = _dialogState.value.copy(
            isVisible = true,
            isLoading = false,
            nomeUsuario = null,
            erro = null
        )
    }

    fun ocultarDialogValidacao() {
        _dialogState.value = DialogState()
    }

    fun validarSenha(senha: String) {
        viewModelScope.launch {
            _dialogState.value = _dialogState.value.copy(
                isLoading = true,
                erro = null
            )

            try {
                val request = ValidacaoSenhaRequest(senha)
                val response = apiService.validarSenha(request)

                if (response.isSuccessful) {
                    val validacaoResponse = response.body()

                    if (validacaoResponse?.success == true) {
                        _dialogState.value = _dialogState.value.copy(
                            isLoading = false,
                            nomeUsuario = validacaoResponse.nome,
                            erro = null
                        )
                    } else {
                        _dialogState.value = _dialogState.value.copy(
                            isLoading = false,
                            erro = "Senha inválida"
                        )
                    }
                } else {
                    _dialogState.value = _dialogState.value.copy(
                        isLoading = false,
                        erro = "Erro na validação da senha"
                    )
                }
            } catch (e: Exception) {
                _dialogState.value = _dialogState.value.copy(
                    isLoading = false,
                    erro = "Erro de conexão: ${e.message}"
                )
            }
        }
    }

    fun confirmarEntrega(onSuccess: () -> Unit) {
        // TODO: Implementar API de entrega final
        viewModelScope.launch {
            ocultarDialogValidacao()
            onSuccess()
        }
    }

    fun limparState() {
        _uiState.value = EntregaUiState()
        _dialogState.value = DialogState()
    }
}

data class EntregaUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val produtos: List<ProdutoEntrega> = emptyList(),
    val produtosSelecionados: Set<String> = emptySet(),
    val pedido: String = "",
    val setoresSelecionados: Set<String> = emptySet()
)

data class DialogState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val nomeUsuario: String? = null,
    val erro: String? = null
)