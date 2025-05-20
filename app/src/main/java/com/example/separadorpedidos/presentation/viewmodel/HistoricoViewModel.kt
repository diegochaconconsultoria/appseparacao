// HistoricoViewModel.kt
package com.example.separadorpedidos.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.separadorpedidos.data.api.NetworkModule
import com.example.separadorpedidos.data.model.HistoricoRequest
import com.example.separadorpedidos.data.model.HistoricoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoricoViewModel : ViewModel() {

    private val apiService = NetworkModule.apiService

    private val _uiState = MutableStateFlow(HistoricoUiState())
    val uiState: StateFlow<HistoricoUiState> = _uiState.asStateFlow()

    fun buscarHistorico(numeroPedido: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                Log.d("HistoricoViewModel", "Buscando histórico para o pedido: $numeroPedido")
                val request = HistoricoRequest(pedido = numeroPedido)
                val response = apiService.obterHistoricoPedido(request)

                if (response.isSuccessful) {
                    val historicoResponse = response.body()
                    Log.d("HistoricoViewModel", "Resposta obtida: $historicoResponse")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        historicoResponse = historicoResponse,
                        entregaIniciada = historicoResponse?.isEntregaNaoIniciada() != true
                    )
                } else {
                    val errorMsg = "Erro ${response.code()}: ${response.message()}"
                    Log.e("HistoricoViewModel", errorMsg)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                }
            } catch (e: Exception) {
                Log.e("HistoricoViewModel", "Erro ao buscar histórico", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erro de conexão: ${e.message}"
                )
            }
        }
    }

    fun limparState() {
        _uiState.value = HistoricoUiState()
    }
}

data class HistoricoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val historicoResponse: HistoricoResponse? = null,
    val entregaIniciada: Boolean = false
)