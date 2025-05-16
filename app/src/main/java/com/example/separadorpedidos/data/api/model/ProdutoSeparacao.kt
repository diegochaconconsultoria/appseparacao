package com.example.separadorpedidos.data.model

import com.google.gson.annotations.SerializedName

data class SeparacaoRequest(
    val pedido: String,
    val setores: String
)

data class SeparacaoResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("Produtos")
    val produtos: List<ProdutoSeparacao>? = null
)

data class ProdutoSeparacao(
    @SerializedName("Produto")
    val produto: String,
    @SerializedName("Descricao")
    val descricao: String,
    @SerializedName("QtdOriginal")
    val qtdOriginal: Double,
    @SerializedName("Saldo")
    val saldo: Double,
    @SerializedName("QtdaSeparar")
    val qtdaSeparar: Double,
    @SerializedName("Um")
    val um: String,
    @SerializedName("QtdSegum")
    val qtdSegum: Double,
    @SerializedName("SegunUm")
    val segunUm: String,
    @SerializedName("Setor")
    val setor: String,
    @SerializedName("Local")
    val local: String,
    @SerializedName("DescricaoLocal")
    val descricaoLocal: String,
    @SerializedName("Corredor")
    val corredor: String,
    @SerializedName("Estante")
    val estante: String,
    @SerializedName("Prateleira")
    val prateleira: String
) {
    // Funções auxiliares para formatação
    fun getQtdOriginalFormatted(): String = formatQuantity(qtdOriginal)
    fun getSaldoFormatted(): String = formatQuantity(saldo)
    fun getQtdaSepararFormatted(): String = formatQuantity(qtdaSeparar)
    fun getQtdSegumFormatted(): String = formatQuantity(qtdSegum)

    // ALTERADO: Função para verificar se já foi separado (saldo = 0)
    fun jaSeparado(): Boolean = saldo <= 0.0

    // NOVO: Função para verificar se o produto pode ser selecionado
    fun podeSelecionar(): Boolean = !jaSeparado()

    // Função para obter o endereço completo do produto
    fun getEnderecoCompleto(): String {
        return if (local.isBlank()) {
            "Sem localização"
        } else {
            buildString {
                if (descricaoLocal.isNotBlank()) append(descricaoLocal)
                if (corredor.isNotBlank() || estante.isNotBlank() || prateleira.isNotBlank()) {
                    if (isNotEmpty()) append(" - ")
                    if (corredor.isNotBlank()) append("Corredor $corredor")
                    if (estante.isNotBlank()) append(" Estante $estante")
                    if (prateleira.isNotBlank()) append(" Prateleira $prateleira")
                }
                if (isEmpty()) append("Local: $local")
            }
        }
    }

    // Função para obter o texto do filtro de local
    fun getLocalParaFiltro(): String {
        return if (local.isBlank()) {
            "Sem local cadastrado"
        } else {
            descricaoLocal.ifBlank { "Local: $local" }
        }
    }

    private fun formatQuantity(value: Double): String {
        return if (value == value.toInt().toDouble()) {
            value.toInt().toString()
        } else {
            String.format("%.2f", value).replace(",0{1,2}$".toRegex(), "")
        }
    }
}

// Classe para representar um filtro de local
data class FiltroLocal(
    val codigo: String,
    val descricao: String,
    val quantidadeProdutos: Int
) {
    companion object {
        const val FILTRO_TODOS = "TODOS"
        const val FILTRO_SEM_LOCAL = "SEM_LOCAL"

        fun criarTodos(quantidade: Int) = FiltroLocal(
            codigo = FILTRO_TODOS,
            descricao = "Todos",
            quantidadeProdutos = quantidade
        )

        fun criarSemLocal(quantidade: Int) = FiltroLocal(
            codigo = FILTRO_SEM_LOCAL,
            descricao = "Sem local cadastrado",
            quantidadeProdutos = quantidade
        )
    }
}

// Mapeamento de códigos dos setores (sem alteração)
object CodigoSetores {
    private val mapeamento = mapOf(
        "Aramado" to "0001",
        "Tubo" to "0002",
        "Chapa" to "0003",
        "Solda" to "0004",
        "Marcenaria" to "0005",
        "Pintura" to "0006",
        "Comunicação Visual" to "0007",
        "Embalagem" to "0008",
        "Pré Montagem" to "0009",
        "Laser" to "0010",
        "Vaccumm Forming" to "0011",
        "Usinagem MDF" to "0012",
        "Laser Tubo" to "0013",
        "Sem Cadastro" to "9999"
    )

    fun obterCodigo(nomeSetor: String): String {
        return mapeamento[nomeSetor] ?: run {
            // Se não encontrar pelo nome, verificar se já é um código
            if (nomeSetor.matches(Regex("\\d{4}"))) {
                nomeSetor
            } else {
                "9999" // Padrão para setores não encontrados
            }
        }
    }

    // NOVA FUNÇÃO: Obter nome do setor pelo código
    fun obterNomePorCodigo(codigo: String): String {
        return mapeamento.entries.find { it.value == codigo }?.key ?: codigo
    }

    fun formatarSetores(setoresSelecionados: Set<String>): String {
        val codigos = setoresSelecionados.map { "'${obterCodigo(it)}'" }
        return codigos.joinToString(",")
    }
}