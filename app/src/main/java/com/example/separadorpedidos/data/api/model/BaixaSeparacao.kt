package com.example.separadorpedidos.data.model

import com.google.gson.annotations.SerializedName

// Request para realizar baixa/separação
data class BaixaSeparacaoRequest(
    @SerializedName("Pedido")
    val pedido: String,
    @SerializedName("Produtos")
    val produtos: List<ProdutoBaixa>,
    @SerializedName("Usuario")  // Novo campo
    val usuario: String = ""    // Campo para o nome do usuário
)

data class ProdutoBaixa(
    @SerializedName("Codigo")
    val codigo: String,
    @SerializedName("Setor")
    val setor: String,
    @SerializedName("Um")
    val um: String
)

// Response da API de baixa/separação
data class BaixaSeparacaoResponse(
    @SerializedName("sucess")
    val success: Boolean
)