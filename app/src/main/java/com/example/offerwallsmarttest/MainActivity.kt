package com.example.offerwallsmarttest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var ids: List<IdsData> = listOf()
    private var currentItem: Int = 0
    private val baseUrl = "https://demo3005513.mockable.io/api/v1/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onStart() {
        super.onStart()

        val web = findViewById<WebView>(R.id.web)
        val webSettings: WebSettings = web.settings
        webSettings.javaScriptEnabled = true

        findViewById<Button>(R.id.next).setOnClickListener {
            if (ids.isNotEmpty())
                currentItem++
                if (ids.size > currentItem){
                    getItem(ids[currentItem].id)
                } else {
                    currentItem = 0
                    getItem(ids[currentItem].id)
                }
        }

        Thread {
            val url = baseUrl + "entities/getAllIds"
            try {
                val idsResp = Gson().fromJson(httpRequest(url) ?: "", Ids::class.java)
                ids = idsResp!!.data
                getItem(ids[0].id)
            } catch (e: Exception) {
                println(e)
            }
        }.start()
    }

    private fun httpRequest(url: String): String? {
        val client = OkHttpClient()
        val request: Request  = Request.Builder()
            .url(url)
            .build()
        try {
            val response: Response = client.newCall(request).execute()
            return response.body?.string() ?: ""
        } catch (e: Exception) {
            println(e)
        } catch (e: IOException) {
            println(e)
        }
        return null
    }

    private fun getItem(i: Int){
        Thread {
            val url = baseUrl + "object/$i"
            try {
                val itemResp = Gson().fromJson(httpRequest(url), Item::class.java)
                val text = findViewById<TextView>(R.id.text)
                val web = findViewById<WebView>(R.id.web)
                this.runOnUiThread {

                    when (itemResp?.type) {
                        "text" -> {
                            text.visibility = View.VISIBLE
                            web.visibility = View.GONE
                            text.text = itemResp.message
                        }
                        "webview" -> {
                            text.visibility = View.GONE
                            web.visibility = View.VISIBLE
                            itemResp.url?.let { web.loadUrl(it) }
                        }
                        "image" -> {
                            text.visibility = View.GONE
                            web.visibility = View.VISIBLE
                            itemResp.url?.let { web.loadUrl(it) }
                        }
                        "game" -> {
                            text.visibility = View.GONE
                            web.visibility = View.GONE
                        }
                        else -> {
                            text.visibility = View.GONE
                            web.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception){
                println(e)
            }
        }.start()
    }
}

data class Ids(
    val data: List<IdsData>
)
data class IdsData(
    val id: Int
)
data class Item(
    val id: Int?,
    val type: String?,
    val message: String?,
    val url: String?,
)