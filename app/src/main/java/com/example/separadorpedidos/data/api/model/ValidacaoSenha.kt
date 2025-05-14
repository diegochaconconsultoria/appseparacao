package com.example.separadorpedidos.data.model

import com.google.gson.annotations.SerializedName

data class ValidacaoSenhaRequest(
    val senha: String // Formato exato: {"senha":"Senha Digitada"}
)

data class ValidacaoSenhaResponse(
    @SerializedName("sucess") // Note que é "sucess" não "success" (igual no retorno da API)
    val success: Boolean,
    @SerializedName("nome")
    val nome: String? = null
)