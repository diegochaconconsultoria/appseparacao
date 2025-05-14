package com.example.separadorpedidos.data.model

import com.google.gson.annotations.SerializedName

data class EntregaRequest(
    val pedido: String,
    val setores: String
)

data class EntregaResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("Produtos")
    val produtos: List<ProdutoEntrega>? = null
)

data class ProdutoEntrega(
    @SerializedName("Produto")
    val produto: String,
    @SerializedName("Descricao")
    val descricao: String,
    @SerializedName("QtdOriginal")
    val qtdOriginal: Double,
    @SerializedName("QtdSeparada")
    val qtdSeparada: Double,
    @SerializedName("Saldoaentregar")
    val saldoAEntregar: Double,
    @SerializedName("Setor")
    val setor: String,
    @SerializedName("Origem")
    val origem: String
) {
    // Funções auxiliares para formatação
    fun getQtdOriginalFormatted(): String = formatQuantity(qtdOriginal)
    fun getQtdSeparadaFormatted(): String = formatQuantity(qtdSeparada)
    fun getSaldoAEntregarFormatted(): String = formatQuantity(saldoAEntregar)

    // Função para verificar se pode ser entregue (saldo > 0)
    fun podeEntregar(): Boolean = saldoAEntregar > 0.0

    // Função para verificar se já foi totalmente entregue
    fun jaEntregue(): Boolean = saldoAEntregar <= 0.0

    private fun formatQuantity(value: Double): String {
        return if (value == value.toInt().toDouble()) {
            value.toInt().toString()
        } else {
            String.format("%.2f", value).replace(",0{1,2}$".toRegex(), "")
        }
    }
}