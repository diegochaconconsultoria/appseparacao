package com.example.separadorpedidos.data.api

import com.example.separadorpedidos.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("VKSEPALMCS")
    suspend fun buscarPedido(@Body request: PedidoRequest): Response<PedidoResponse>

    @POST("VKSEPALMEMP")
    suspend fun buscarProdutosSeparacao(@Body request: SeparacaoRequest): Response<SeparacaoResponse>

    @POST("VKSEPALMENT")
    suspend fun buscarProdutosEntrega(@Body request: EntregaRequest): Response<EntregaResponse>

    @POST("VKSEPALMCONF")
    suspend fun validarSenha(@Body request: ValidacaoSenhaRequest): Response<ValidacaoSenhaResponse>

    @POST("VKSEPALMBX")
    suspend fun realizarBaixaSeparacao(@Body request: BaixaSeparacaoRequest): Response<BaixaSeparacaoResponse>

    @POST("VKSEPALMREENT")
    suspend fun realizarEntrega(@Body request: RealizarEntregaRequest): Response<RealizarEntregaResponse>

    // Endpoint atualizado para usar ImageResponse
    @GET("VKSEPALMIMG/codigoProduto")
    suspend fun getProductImage(@Path("codigoProduto") codigoProduto: String): Response<ImageResponse>

    // CORRIGIDO: uploadProductImage usando ImageUploadRequest em vez de Map
    @POST("VKSEPALMIMG/codigoProduto")
    suspend fun uploadProductImage(
        @Path("codigoProduto") codigoProduto: String,
        @Body request: ImageUploadRequest
    ): Response<ImageResponse>
}