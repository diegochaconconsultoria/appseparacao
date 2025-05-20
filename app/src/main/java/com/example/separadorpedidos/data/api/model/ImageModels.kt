package com.example.separadorpedidos.data.model

import com.google.gson.annotations.SerializedName

// Request para upload de imagem
data class ImageUploadRequest(
    @SerializedName("imageBase64")
    val imageBase64: String,
    @SerializedName("extensao")
    val extensao: String = "jpg"
)

// Response do upload de imagem
data class ImageUploadResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("codigo")
    val codigo: String? = null,
    @SerializedName("arquivo")
    val arquivo: String? = null,
    @SerializedName("tamanho")
    val tamanho: Int? = null,
    @SerializedName("message")
    val message: String? = null
)

// Response quando imagem não existe
data class ImageNotFoundResponse(
    @SerializedName("exists")
    val exists: Boolean,
    @SerializedName("codigo")
    val codigo: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("canUpload")
    val canUpload: Boolean
)

data class UploadImageResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String
    // Campo path removido conforme a nova especificação
)