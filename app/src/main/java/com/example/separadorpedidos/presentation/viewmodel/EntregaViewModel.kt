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


    fun selecionarTodosProdutos() {
        // Obter todos os códigos de produtos que podem ser selecionados/entregues
        val produtosSelecionaveis = _uiState.value.produtos
            .filter { it.podeEntregar() }
            .map { it.produto }
            .toSet()

        _uiState.value = _uiState.value.copy(
            produtosSelecionados = produtosSelecionaveis
        )
    }

    fun desselecionarTodosProdutos() {
        _uiState.value = _uiState.value.copy(
            produtosSelecionados = emptySet()
        )
    }

    fun toggleSelecionarTodos() {
        // Se já temos todos selecionados, desseleciona todos
        val produtosSelecionaveis = _uiState.value.produtos
            .filter { it.podeEntregar() }
            .map { it.produto }
            .toSet()

        if (_uiState.value.produtosSelecionados.size == produtosSelecionaveis.size &&
            _uiState.value.produtosSelecionados.containsAll(produtosSelecionaveis)) {
            // Todos já estão selecionados, então desseleciona todos
            desselecionarTodosProdutos()
        } else {
            // Nem todos estão selecionados, então seleciona todos
            selecionarTodosProdutos()
        }
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

    fun confirmarEntrega(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _dialogState.value = _dialogState.value.copy(
                isLoading = true
            )

            try {
                val nomeUsuario = _dialogState.value.nomeUsuario ?: run {
                    _dialogState.value = _dialogState.value.copy(
                        isLoading = false,
                        erro = "Nome do usuário não encontrado"
                    )
                    onError("Nome do usuário não encontrado")
                    return@launch
                }

                // Buscar os produtos selecionados
                val produtosSelecionados = _uiState.value.produtos.filter { produto ->
                    _uiState.value.produtosSelecionados.contains(produto.produto)
                }

                // Criar lista de produtos para entrega
                val produtosEntrega = produtosSelecionados.map { produto ->
                    // Usar o setor diretamente como vem da API (já deve vir no formato correto)
                    val codigoSetor = produto.setor

                    ProdutoEntregaRequest(
                        codigo = produto.produto,
                        descricao = produto.descricao,
                        origem = produto.origem,
                        setor = codigoSetor,
                        entrega = produto.saldoAEntregar
                    )
                }

                // Criar request
                val request = RealizarEntregaRequest(
                    pedido = _uiState.value.pedido,
                    nome = nomeUsuario,
                    produtos = produtosEntrega
                )

                // Chamar API
                val response = apiService.realizarEntrega(request)

                if (response.isSuccessful) {
                    val entregaResponse = response.body()

                    if (entregaResponse?.success == true) {
                        _uiState.value = _uiState.value.copy(
                            produtosSelecionados = emptySet(),
                            showEntregaSucesso = true,
                            quantidadeEntregaRealizada = produtosSelecionados.size
                        )
                        _dialogState.value = DialogState()
                        onSuccess()
                        // Recarregar produtos para atualizar o status
                        buscarProdutosEntrega(_uiState.value.pedido, _uiState.value.setoresSelecionados)
                    } else {
                        _dialogState.value = _dialogState.value.copy(
                            isLoading = false,
                            erro = "Erro ao realizar entrega"
                        )
                        onError("Erro ao realizar entrega")
                    }
                } else {
                    _dialogState.value = _dialogState.value.copy(
                        isLoading = false,
                        erro = "Erro na comunicação com o servidor"
                    )
                    onError("Erro na comunicação com o servidor")
                }
            } catch (e: Exception) {
                _dialogState.value = _dialogState.value.copy(
                    isLoading = false,
                    erro = "Erro de conexão: ${e.message}"
                )
                onError("Erro de conexão: ${e.message}")
            }
        }
    }

    fun limparState() {
        _uiState.value = EntregaUiState()
        _dialogState.value = DialogState()
    }

    fun limparEntregaSucesso() {
        _uiState.value = _uiState.value.copy(showEntregaSucesso = false)
    }
}

data class EntregaUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val produtos: List<ProdutoEntrega> = emptyList(),
    val produtosSelecionados: Set<String> = emptySet(),
    val pedido: String = "",
    val setoresSelecionados: Set<String> = emptySet(),
    val showEntregaSucesso: Boolean = false,
    val quantidadeEntregaRealizada: Int = 0
)

data class DialogState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val nomeUsuario: String? = null,
    val erro: String? = null
)