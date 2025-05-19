package com.example.separadorpedidos.data.model

import com.google.gson.annotations.SerializedName

// Modelo para resposta de imagem com Base64
data class ImageResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("codigoProduto")
    val codigoProduto: String? = null,

    @SerializedName("imageBase64")
    val imageBase64: String? = null,

    @SerializedName("contentType")
    val contentType: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("canUpload")
    val canUpload: Boolean = false,

    // Campos adicionais que podem estar vindo da API
    @SerializedName("codigo")
    val codigo: String? = null,

    @SerializedName("arquivo")
    val arquivo: String? = null,

    @SerializedName("tamanho")
    val tamanho: Int? = null
)