package com.example.separadorpedidos.data.model

data class Pedido(
    val id: String,
    val numero: String,
    val items: List<ItemPedido>,
    val status: StatusPedido
)

data class ItemPedido(
    val id: String,
    val produto: Produto,
    val quantidade: Int,
    val quantidadeSeparada: Int = 0,
    val localizacao: Localizacao? = null,
    val setor: Setor
)

data class Produto(
    val id: String,
    val nome: String,
    val codigo: String,
    val descricao: String? = null
)

data class Localizacao(
    val id: String,
    val nome: String,
    val coordenadas: Coordenadas? = null
)

data class Coordenadas(
    val x: Float,
    val y: Float
)

data class Setor(
    val id: String,
    val nome: String,
    val cor: String? = null
)

enum class StatusPedido {
    PENDENTE,
    SEPARANDO,
    SEPARADO,
    EM_ENTREGA,
    ENTREGUE
}