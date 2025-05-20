// EmailSender.kt
package com.example.separadorpedidos.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.separadorpedidos.data.model.ProdutoSeparacao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender {
    companion object {
        private const val SMTP_HOST = "email-ssl.com.br"
        private const val SMTP_PORT = "465"
        private const val EMAIL_FROM = "cadeideajuda@mvk.com.br"
        private const val EMAIL_PASSWORD = "@Vendas2366"
        private val EMAIL_TO = arrayOf(
            "regina.cesarin@mvk.com.br",
            "marcos.lalla@mvk.com.br",
            "andresa.marques@mvk.com.br"
        )

        /*private val EMAIL_TO = arrayOf(
            "daniela.cippola@mvk.com.br",
            "diego.chacon@mvk.com.br"
        )*/

        suspend fun sendEmailAboutMissingProducts(
            context: Context,
            numeroPedido: String,
            nomeCliente: String,
            produtosSelecionados: List<ProdutoSeparacao>
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    // Configurar as propriedades
                    val properties = Properties()
                    properties["mail.smtp.host"] = SMTP_HOST
                    properties["mail.smtp.socketFactory.port"] = SMTP_PORT
                    properties["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                    properties["mail.smtp.auth"] = "true"
                    properties["mail.smtp.port"] = SMTP_PORT

                    // Criar a sessão de e-mail com autenticação
                    val session = Session.getDefaultInstance(properties,
                        object : Authenticator() {
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD)
                            }
                        })

                    // Criar mensagem
                    val message = MimeMessage(session)
                    message.setFrom(InternetAddress(EMAIL_FROM))

                    // Adicionar destinatários
                    for (recipient in EMAIL_TO) {
                        message.addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
                    }

                    // Assunto do e-mail
                    message.subject = "Comunicação de Falta de Materiais - Pedido $numeroPedido"

                    // Conteúdo do e-mail em HTML
                    val emailBody = buildEmailContent(numeroPedido, nomeCliente, produtosSelecionados)
                    message.setContent(emailBody, "text/html; charset=utf-8")

                    // Enviar mensagem
                    Transport.send(message)

                    // Exibir mensagem de sucesso na thread principal
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            "E-mail enviado com sucesso!",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    true
                } catch (e: Exception) {
                    e.printStackTrace()

                    // Exibir mensagem de erro na thread principal
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            "Erro ao enviar e-mail: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    false
                }
            }
        }

        private fun buildEmailContent(
            numeroPedido: String,
            nomeCliente: String,
            produtos: List<ProdutoSeparacao>
        ): String {
            val sb = StringBuilder()

            // Cabeçalho do e-mail
            sb.append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; }
                        .container { max-width: 800px; margin: 0 auto; padding: 20px; }
                        h1 { color: #9F2340; border-bottom: 2px solid #9F2340; padding-bottom: 10px; }
                        .info { background-color: #f8f8f8; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
                        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                        th { background-color: #9F2340; color: white; text-align: left; padding: 10px; }
                        td { padding: 10px; border-bottom: 1px solid #ddd; }
                        tr:nth-child(even) { background-color: #f2f2f2; }
                        .footer { margin-top: 30px; font-size: 12px; color: #777; text-align: center; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Comunicação de Falta de Materiais</h1>
                        
                        <div class="info">
                            <p><strong>Pedido:</strong> ${numeroPedido}</p>
                            <p><strong>Cliente:</strong> ${nomeCliente}</p>
                            <p><strong>Data/Hora:</strong> ${Date()}</p>
                        </div>
                        
                        <p>Segue abaixo a lista de produtos com falta de materiais que foram informados pelo separador:</p>
                        
                        <table>
                            <tr>
                                <th>Código</th>
                                <th>Descrição</th>
                                <th>Setor</th>
                                <th>Quantidade</th>
                            </tr>
            """.trimIndent())

            // Adicionar cada produto na tabela
            for (produto in produtos) {
                sb.append("""
                    <tr>
                        <td>${produto.produto}</td>
                        <td>${produto.descricao}</td>
                        <td>${produto.setor}</td>
                        <td>${produto.getQtdaSepararFormatted()}</td>
                    </tr>
                """.trimIndent())
            }

            // Rodapé do e-mail
            sb.append("""
                        </table>
                        
                        <p>Por favor, verifique a disponibilidade destes itens e tome as providências necessárias.</p>
                        
                        <div class="footer">
                            <p>Esta é uma mensagem automática. Por favor, não responda diretamente a este e-mail.</p>
                            <p>MVK Separação de Pedidos - Sistema Automático</p>
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent())

            return sb.toString()
        }
    }
}