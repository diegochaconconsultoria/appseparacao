// Em um novo arquivo ou no mesmo arquivo de outros modelos de dados
package com.example.separadorpedidos.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

data class HistoricoRequest(
    @SerializedName("Pedido")
    val pedido: String
)

data class HistoricoResponse(
    @SerializedName("DataVenda")
    val dataVenda: String? = null,
    @SerializedName("DataInclusao")
    val dataInclusao: String? = null,
    @SerializedName("Liberacaoparapcp")
    val liberacaoParaPcp: String? = null,
    @SerializedName("DataGeracaoOp")
    val dataGeracaoOp: String? = null,
    @SerializedName("DataInicioEmabalagem")
    val dataInicioEmbalagem: String? = null,
    @SerializedName("Notafiscal")
    val notaFiscal: String? = null,
    @SerializedName("ListaEntrega")
    val listaEntrega: List<RegistroEntrega>? = null,
    @SerializedName("Entrega")
    val entregaStatus: String? = null
) {
    fun isEntregaNaoIniciada(): Boolean {
        return entregaStatus == "Entrega nao Iniciada"
    }

    fun formatarData(dataString: String?): String {
        if (dataString.isNullOrBlank() || dataString == "00000000") return "Não definida"

        return try {
            val inputFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dataString)
            date?.let { outputFormat.format(it) } ?: "Formato inválido"
        } catch (e: Exception) {
            "Formato inválido"
        }
    }
}

data class RegistroEntrega(
    @SerializedName("OrdemProducao")
    val ordemProducao: String,
    @SerializedName("CodigoProduto")
    val codigoProduto: String,
    @SerializedName("DescProduto")
    val descProduto: String,
    @SerializedName("Qtd")
    val quantidade: Double, // Alterado de Int para Double
    @SerializedName("DataRetirada")
    val dataRetirada: String,
    @SerializedName("Colaborador")
    val colaborador: String,
    @SerializedName("Setor")
    val setor: String
) {
    fun formatarDataRetirada(): String {
        if (dataRetirada.isNullOrBlank() || dataRetirada == "00000000") return "Não definida"

        return try {
            val inputFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dataRetirada)
            date?.let { outputFormat.format(it) } ?: "Formato inválido"
        } catch (e: Exception) {
            "Formato inválido"
        }
    }

    // Formatar a quantidade para exibição
    fun formatarQuantidade(): String {
        // Se for um número inteiro, remove as casas decimais
        return if (quantidade == quantidade.toInt().toDouble()) {
            quantidade.toInt().toString()
        } else {
            // Caso contrário, formata com até 2 casas decimais
            String.format("%.2f", quantidade)
        }
    }
}