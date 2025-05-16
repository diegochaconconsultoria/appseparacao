package com.example.separadorpedidos.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object LocalImageStorage {
    private const val TAG = "LocalImageStorage"
    private const val IMAGES_FOLDER = "product_images"

    /**
     * Salva uma imagem localmente
     * @param context contexto da aplicação
     * @param codigoProduto código do produto (usado como nome do arquivo)
     * @param bitmap imagem em formato Bitmap
     * @return caminho do arquivo salvo ou null em caso de erro
     */
    fun saveImage(context: Context, codigoProduto: String, bitmap: Bitmap): String? {
        return try {
            // Criar diretório se não existir
            val imageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGES_FOLDER)
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }

            // Criar arquivo com nome baseado no código do produto
            val filename = "${codigoProduto}.jpg"
            val file = File(imageDir, filename)

            // Salvar bitmap como JPEG com 85% de qualidade para economizar espaço
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()

            Log.d(TAG, "Imagem salva: ${file.absolutePath}")
            file.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao salvar imagem para $codigoProduto", e)
            null
        }
    }

    /**
     * Obtém o caminho da imagem local
     * @param context contexto da aplicação
     * @param codigoProduto código do produto
     * @return caminho do arquivo ou null se não existir
     */
    fun getImagePath(context: Context, codigoProduto: String): String? {
        val imageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGES_FOLDER)
        val filename = "${codigoProduto}.jpg"
        val file = File(imageDir, filename)

        return if (file.exists()) {
            Log.d(TAG, "Imagem encontrada: ${file.absolutePath}")
            file.absolutePath
        } else {
            Log.d(TAG, "Imagem não encontrada para: $codigoProduto")
            null
        }
    }

    /**
     * Verifica se existe uma imagem para o produto
     * @param context contexto da aplicação
     * @param codigoProduto código do produto
     * @return true se a imagem existe, false caso contrário
     */
    fun imageExists(context: Context, codigoProduto: String): Boolean {
        val imagePath = getImagePath(context, codigoProduto)
        return imagePath != null
    }

    /**
     * Deleta uma imagem local
     * @param context contexto da aplicação
     * @param codigoProduto código do produto
     * @return true se deletou com sucesso, false caso contrário
     */
    fun deleteImage(context: Context, codigoProduto: String): Boolean {
        val imagePath = getImagePath(context, codigoProduto)
        return if (imagePath != null) {
            val file = File(imagePath)
            val deleted = file.delete()
            Log.d(TAG, if (deleted) "Imagem deletada: $codigoProduto" else "Erro ao deletar: $codigoProduto")
            deleted
        } else {
            Log.d(TAG, "Imagem não encontrada para deletar: $codigoProduto")
            false
        }
    }

    /**
     * Converte um Bitmap para arquivo temporário e retorna URI
     * @param context contexto da aplicação
     * @param bitmap imagem em formato Bitmap
     * @return URI do arquivo temporário ou null em caso de erro
     */
    fun saveBitmapToTempFile(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val timestamp = System.currentTimeMillis()
            val file = File(context.cacheDir, "temp_image_$timestamp.jpg")

            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            Log.d(TAG, "Bitmap salvo em arquivo temporário: $uri")
            uri
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar bitmap temporário", e)
            null
        }
    }

    /**
     * Obtém todas as imagens salvas
     * @param context contexto da aplicação
     * @return lista de arquivos de imagem
     */
    fun getAllImages(context: Context): List<File> {
        val imageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGES_FOLDER)
        return if (imageDir.exists()) {
            imageDir.listFiles { file -> file.name.endsWith(".jpg") }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Obtém informações de uso de espaço
     * @param context contexto da aplicação
     * @return informações sobre o espaço usado
     */
    fun getStorageInfo(context: Context): StorageInfo {
        val imageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGES_FOLDER)
        var totalSize = 0L
        var fileCount = 0

        if (imageDir.exists()) {
            imageDir.listFiles { file -> file.name.endsWith(".jpg") }?.forEach { file ->
                totalSize += file.length()
                fileCount++
            }
        }

        return StorageInfo(
            fileCount = fileCount,
            totalSizeBytes = totalSize,
            totalSizeMB = totalSize / (1024.0 * 1024.0)
        )
    }
}

/**
 * Classe para informações de armazenamento
 */
data class StorageInfo(
    val fileCount: Int,
    val totalSizeBytes: Long,
    val totalSizeMB: Double
) {
    fun getFormattedSize(): String {
        return when {
            totalSizeMB < 1.0 -> "%.1f KB".format(totalSizeBytes / 1024.0)
            totalSizeMB < 1024.0 -> "%.1f MB".format(totalSizeMB)
            else -> "%.1f GB".format(totalSizeMB / 1024.0)
        }
    }
}