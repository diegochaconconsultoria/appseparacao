// Crie um novo arquivo em utils/VoiceRecognitionHelper.kt
package com.example.separadorpedidos.utils

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.*

/**
 * Classe auxiliar para gerenciar o reconhecimento de voz
 */
class VoiceRecognitionHelper {
    companion object {
        /**
         * Cria um launcher para o reconhecimento de voz com o Intent padrão
         * @param onResult Callback chamado quando o reconhecimento for bem-sucedido
         * @param onError Callback chamado em caso de erro
         */
        @Composable
        fun createVoiceRecognitionLauncher(
            onResult: (String) -> Unit,
            onError: (String) -> Unit = {}
        ): VoiceRecognitionLauncher {
            // Registrar o launcher para o resultado da Activity
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Obter os resultados do reconhecimento
                    val data: Intent? = result.data
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                    if (!results.isNullOrEmpty()) {
                        // Extrair apenas os dígitos do primeiro resultado
                        val digits = results[0].filter { it.isDigit() }

                        if (digits.isNotEmpty()) {
                            onResult(digits)
                        } else {
                            onError("Nenhum número identificado na fala")
                        }
                    } else {
                        onError("Não foi possível reconhecer o que você disse")
                    }
                } else if (result.resultCode == Activity.RESULT_CANCELED) {
                    onError("Reconhecimento de voz cancelado")
                }
            }

            return remember { VoiceRecognitionLauncher(launcher) }
        }
    }

    /**
     * Classe que encapsula o launcher do reconhecimento de voz
     */
    class VoiceRecognitionLauncher(
        private val launcher: androidx.activity.result.ActivityResultLauncher<Intent>
    ) {
        /**
         * Inicia o reconhecimento de voz
         */
        fun launch() {
            // Criar o intent para o reconhecimento de voz
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("pt", "BR"))
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga o número do pedido")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            // Iniciar a Activity de reconhecimento
            launcher.launch(intent)
        }
    }
}