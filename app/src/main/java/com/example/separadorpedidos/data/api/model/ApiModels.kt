package com.example.separadorpedidos.data.model

import com.google.gson.annotations.SerializedName

data class PedidoRequest(
    val pedido: String
)

data class PedidoResponse(
    @SerializedName("sucess")
    val success: Boolean,
    @SerializedName("Descricao")
    val descricao: String,
    @SerializedName("Cliente")
    val cliente: String? = null,
    @SerializedName("Aramado")
    val aramado: String? = null,
    @SerializedName("Chapa")
    val chapa: String? = null,
    @SerializedName("Tubo")
    val tubo: String? = null,
    @SerializedName("Solda")
    val solda: String? = null,
    @SerializedName("Marcenaria")
    val marcenaria: String? = null,
    @SerializedName("Pintura")
    val pintura: String? = null,
    @SerializedName("ComunicacaoVisual")
    val comunicacaoVisual: String? = null,
    @SerializedName("Embalagem")
    val embalagem: String? = null,
    @SerializedName("PreMontagem")
    val preMontagem: String? = null,
    @SerializedName("Laser")
    val laser: String? = null,
    @SerializedName("Vaccum")
    val vaccum: String? = null,
    @SerializedName("Usinagem")
    val usinagem: String? = null,
    @SerializedName("LaserTubo")
    val laserTubo: String? = null,
    @SerializedName("SemCadastro")
    val semCadastro: String? = null
)

// No arquivo ApiModels.kt, mantenha SetorDisponivel assim:
data class SetorDisponivel(
    val nome: String,
    val chaveApi: String,
    val habilitado: Boolean
)