package com.example.separadorpedidos.data.api

import com.example.separadorpedidos.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("VKSEPALMCS")
    suspend fun buscarPedido(@Body request: PedidoRequest): Response<PedidoResponse>

    @POST("VKSEPALMEMP")
    suspend fun buscarProdutosSeparacao(@Body request: SeparacaoRequest): Response<SeparacaoResponse>

    @POST("VKSEPALMENT")
    suspend fun buscarProdutosEntrega(@Body request: EntregaRequest): Response<EntregaResponse>

    @POST("VKSEPALMCONF") // API real para validação de senha
    suspend fun validarSenha(@Body request: ValidacaoSenhaRequest): Response<ValidacaoSenhaResponse>
}