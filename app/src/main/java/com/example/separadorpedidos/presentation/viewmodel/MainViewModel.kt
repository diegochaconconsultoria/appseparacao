package com.example.separadorpedidos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.separadorpedidos.data.api.NetworkModule  // Adicionar este import
import com.example.separadorpedidos.data.model.PedidoRequest
import com.example.separadorpedidos.data.model.PedidoResponse
import com.example.separadorpedidos.data.model.SetorDisponivel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val apiService = NetworkModule.apiService

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun buscarPedido(numeroPedido: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val response = apiService.buscarPedido(PedidoRequest(numeroPedido))

                if (response.isSuccessful) {
                    val pedidoResponse = response.body()

                    if (pedidoResponse?.success == true) {
                        val setoresDisponiveis = criarListaSetores(pedidoResponse)

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            pedidoEncontrado = true,
                            nomeCliente = pedidoResponse.cliente ?: "Cliente não informado",
                            setoresDisponiveis = setoresDisponiveis,
                            numeroPedido = numeroPedido
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = pedidoResponse?.descricao ?: "Pedido não encontrado"
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

    private fun criarListaSetores(response: PedidoResponse): List<SetorDisponivel> {
        return listOf(
            SetorDisponivel("Aramado", "aramado", response.aramado == "Sim"),
            SetorDisponivel("Tubo", "tubo", response.tubo == "Sim"),
            SetorDisponivel("Chapa", "chapa", response.chapa == "Sim"),
            SetorDisponivel("Solda", "solda", response.solda == "Sim"),
            SetorDisponivel("Marcenaria", "marcenaria", response.marcenaria == "Sim"),
            SetorDisponivel("Pintura", "pintura", response.pintura == "Sim"),
            SetorDisponivel("Comunicação Visual", "comunicacaoVisual", response.comunicacaoVisual == "Sim"),
            SetorDisponivel("Embalagem", "embalagem", response.embalagem == "Sim"),
            SetorDisponivel("Pré Montagem", "preMontagem", response.preMontagem == "Sim"),
            SetorDisponivel("Laser", "laser", response.laser == "Sim"),
            SetorDisponivel("Vaccumm Forming", "vaccum", response.vaccum == "Sim"),
            SetorDisponivel("Usinagem MDF", "usinagem", response.usinagem == "Sim"),
            SetorDisponivel("Laser Tubo", "laserTubo", response.laserTubo == "Sim"),
            SetorDisponivel("Sem Cadastro", "semCadastro", response.semCadastro == "Sim")
        )
    }

    fun limparState() {
        _uiState.value = MainUiState()
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pedidoEncontrado: Boolean = false,
    val nomeCliente: String = "",
    val setoresDisponiveis: List<SetorDisponivel> = emptyList(),
    val numeroPedido: String = ""
)