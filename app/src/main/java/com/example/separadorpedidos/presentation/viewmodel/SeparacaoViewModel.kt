package com.example.separadorpedidos.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.separadorpedidos.data.api.NetworkModule
import com.example.separadorpedidos.data.model.*
import com.example.separadorpedidos.utils.EmailSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SeparacaoViewModel : ViewModel() {

    private val apiService = NetworkModule.apiService

    private val _uiState = MutableStateFlow(SeparacaoUiState())
    val uiState: StateFlow<SeparacaoUiState> = _uiState.asStateFlow()

    fun buscarProdutosSeparacao(pedido: String, setoresSelecionados: Set<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val setoresFormatados = CodigoSetores.formatarSetores(setoresSelecionados)
                val request = SeparacaoRequest(pedido, setoresFormatados)
                val response = apiService.buscarProdutosSeparacao(request)

                if (response.isSuccessful) {
                    val separacaoResponse = response.body()

                    if (separacaoResponse?.success == true) {
                        val produtos = separacaoResponse.produtos ?: emptyList()
                        val filtrosLocais = criarFiltrosLocais(produtos)

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            produtosTodos = produtos,
                            produtos = produtos,
                            produtosSelecionados = emptySet(),
                            pedido = pedido,
                            setoresSelecionados = setoresSelecionados,
                            filtrosLocais = filtrosLocais,
                            filtroSelecionado = FiltroLocal.FILTRO_TODOS
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Nenhum produto encontrado para os setores selecionados"
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

    fun realizarBaixaSeparacao(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBaixaLoading = true,
                baixaError = null
            )

            try {
                // Validar que temos um nome de usuário
                val nomeUsuario = _uiState.value.validatedUserName
                if (nomeUsuario.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isBaixaLoading = false,
                        baixaError = "Usuário não autenticado"
                    )
                    onError("Usuário não autenticado")
                    return@launch
                }

                // Buscar os produtos selecionados
                val produtosSelecionados = _uiState.value.produtosTodos.filter { produto ->
                    _uiState.value.produtosSelecionados.contains(produto.produto)
                }

                // Salvar quantidade para mostrar no dialog
                val quantidadeProdutos = produtosSelecionados.size

                // Criar lista de produtos para baixa
                val produtosBaixa = produtosSelecionados.map { produto ->
                    ProdutoBaixa(
                        codigo = produto.produto,
                        setor = produto.setor,
                        um = produto.um
                    )
                }

                // Criar request com o novo campo usuario
                val request = BaixaSeparacaoRequest(
                    pedido = _uiState.value.pedido,
                    produtos = produtosBaixa,
                    usuario = nomeUsuario  // Adicionar o nome do usuário ao request
                )

                // Chamar API
                val response = apiService.realizarBaixaSeparacao(request)

                if (response.isSuccessful) {
                    val baixaResponse = response.body()

                    if (baixaResponse?.success == true) {
                        _uiState.value = _uiState.value.copy(
                            isBaixaLoading = false,
                            isPasswordDialogVisible = false,  // Fechar o diálogo de senha
                            produtosSelecionados = emptySet(),
                            showBaixaSucesso = true,
                            quantidadeBaixaRealizada = quantidadeProdutos
                        )
                        onSuccess()
                        // Recarregar produtos para atualizar o status
                        buscarProdutosSeparacao(_uiState.value.pedido, _uiState.value.setoresSelecionados)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isBaixaLoading = false,
                            baixaError = "Erro ao realizar baixa/separação"
                        )
                        onError("Erro ao realizar baixa/separação")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isBaixaLoading = false,
                        baixaError = "Erro na comunicação com o servidor"
                    )
                    onError("Erro na comunicação com o servidor")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isBaixaLoading = false,
                    baixaError = "Erro de conexão: ${e.message}"
                )
                onError("Erro de conexão: ${e.message}")
            }
        }
    }

    fun limparPasswordError() {
        _uiState.value = _uiState.value.copy(
            passwordError = null
        )
    }

    fun enviarEmailFaltaMateriais(context: Context, nomeCliente: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isEmailSending = true
            )

            try {
                // Obter produtos selecionados
                val produtosSelecionados = _uiState.value.produtosTodos.filter { produto ->
                    _uiState.value.produtosSelecionados.contains(produto.produto)
                }

                if (produtosSelecionados.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isEmailSending = false,
                        emailError = "Selecione pelo menos um produto para comunicar a falta"
                    )
                    return@launch
                }

                // Enviar e-mail
                val success = EmailSender.sendEmailAboutMissingProducts(
                    context,
                    _uiState.value.pedido,
                    nomeCliente,
                    produtosSelecionados
                )

                _uiState.value = _uiState.value.copy(
                    isEmailSending = false,
                    emailSuccess = success,
                    emailError = if (!success) "Falha ao enviar e-mail" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isEmailSending = false,
                    emailError = "Erro: ${e.message}"
                )
            }
        }
    }

    private fun criarFiltrosLocais(produtos: List<ProdutoSeparacao>): List<FiltroLocal> {
        val filtros = mutableListOf<FiltroLocal>()

        // Adicionar filtro "Todos"
        filtros.add(FiltroLocal.criarTodos(produtos.size))

        // Contar produtos por local
        val produtosPorLocal = produtos.groupBy { produto ->
            if (produto.local.isBlank()) {
                FiltroLocal.FILTRO_SEM_LOCAL
            } else {
                produto.local
            }
        }

        // Criar filtros para cada local
        produtosPorLocal.forEach { (codigo, produtosDoLocal) ->
            val filtro = if (codigo == FiltroLocal.FILTRO_SEM_LOCAL) {
                FiltroLocal.criarSemLocal(produtosDoLocal.size)
            } else {
                val descricao = produtosDoLocal.first().descricaoLocal.ifBlank { "Local: $codigo" }
                FiltroLocal(
                    codigo = codigo,
                    descricao = descricao,
                    quantidadeProdutos = produtosDoLocal.size
                )
            }
            filtros.add(filtro)
        }

        return filtros.sortedBy {
            when (it.codigo) {
                FiltroLocal.FILTRO_TODOS -> "0"
                FiltroLocal.FILTRO_SEM_LOCAL -> "1"
                else -> "2${it.descricao}"
            }
        }
    }

    fun aplicarFiltroLocal(codigoFiltro: String) {
        val produtosFiltrados = when (codigoFiltro) {
            FiltroLocal.FILTRO_TODOS -> _uiState.value.produtosTodos
            FiltroLocal.FILTRO_SEM_LOCAL -> _uiState.value.produtosTodos.filter { it.local.isBlank() }
            else -> _uiState.value.produtosTodos.filter { it.local == codigoFiltro }
        }

        _uiState.value = _uiState.value.copy(
            produtos = produtosFiltrados,
            filtroSelecionado = codigoFiltro,
            // Limpar seleções que não estão mais visíveis OU que já foram separados
            produtosSelecionados = _uiState.value.produtosSelecionados.intersect(
                produtosFiltrados.filter { it.podeSelecionar() }.map { it.produto }.toSet()
            )
        )
    }

    fun toggleProdutoSelecionado(codigoProduto: String) {
        // ALTERADO: Verificar se o produto pode ser selecionado
        val produto = _uiState.value.produtos.find { it.produto == codigoProduto }
        if (produto?.podeSelecionar() != true) return

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

    fun limparState() {
        _uiState.value = SeparacaoUiState()
    }

    fun limparBaixaError() {
        _uiState.value = _uiState.value.copy(baixaError = null)
    }

    fun limparBaixaSucesso() {
        _uiState.value = _uiState.value.copy(showBaixaSucesso = false)
    }

    fun limparEmailSuccess() {
        _uiState.value = _uiState.value.copy(
            emailSuccess = false
        )
    }

    fun limparEmailError() {
        _uiState.value = _uiState.value.copy(
            emailError = null
        )
    }

    fun mostrarDialogSenha() {
        _uiState.value = _uiState.value.copy(
            isPasswordDialogVisible = true,
            passwordError = null,
            validatedUserName = null
        )
    }

    fun ocultarDialogSenha() {
        _uiState.value = _uiState.value.copy(
            isPasswordDialogVisible = false,
            passwordError = null
        )
    }

    fun validarSenha(senha: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isPasswordLoading = true,
                passwordError = null
            )

            try {
                val request = ValidacaoSenhaRequest(senha)
                val response = apiService.validarSenha(request)

                if (response.isSuccessful) {
                    val validacaoResponse = response.body()

                    if (validacaoResponse?.success == true) {
                        _uiState.value = _uiState.value.copy(
                            isPasswordLoading = false,
                            validatedUserName = validacaoResponse.nome
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isPasswordLoading = false,
                            passwordError = "Senha inválida"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isPasswordLoading = false,
                        passwordError = "Erro na validação da senha"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPasswordLoading = false,
                    passwordError = "Erro de conexão: ${e.message}"
                )
            }
        }
    }

}



data class SeparacaoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val produtosTodos: List<ProdutoSeparacao> = emptyList(),
    val produtos: List<ProdutoSeparacao> = emptyList(),
    val produtosSelecionados: Set<String> = emptySet(),
    val pedido: String = "",
    val setoresSelecionados: Set<String> = emptySet(),
    val filtrosLocais: List<FiltroLocal> = emptyList(),
    val filtroSelecionado: String = FiltroLocal.FILTRO_TODOS,
    // Adicionado para controlar estado da baixa/separação
    val isBaixaLoading: Boolean = false,
    val baixaError: String? = null,
    val showBaixaSucesso: Boolean = false,
    val quantidadeBaixaRealizada: Int = 0,
    // Novos campos para controle de e-mail
    val isEmailSending: Boolean = false,
    val emailSuccess: Boolean = false,
    val emailError: String? = null
    val isPasswordDialogVisible: Boolean = false,
    val isPasswordLoading: Boolean = false,
    val validatedUserName: String? = null,
    val passwordError: String? = null
)