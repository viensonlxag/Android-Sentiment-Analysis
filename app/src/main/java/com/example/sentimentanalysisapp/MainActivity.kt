package com.example.sentimentanalysisapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var inputSentence: EditText
    private lateinit var submitButton: Button
    private lateinit var emojiView: ImageView

    private val client = OkHttpClient()
    private val API_URL = "https://a664-104-196-96-113.ngrok-free.app/predict" // Cập nhật URL Ngrok của bạn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputSentence = findViewById(R.id.inputSentence)
        submitButton = findViewById(R.id.submitButton)
        emojiView = findViewById(R.id.emojiView)

        // Ẩn emojiView khi mới vào app
        emojiView.visibility = View.GONE

        submitButton.setOnClickListener {
            val sentence = inputSentence.text.toString()
            if (sentence.isNotEmpty()) {
                analyzeSentiment(sentence)
            }
        }
    }

    private fun analyzeSentiment(sentence: String) {
        val jsonBody = JSONObject().apply {
            put("sentence", sentence)
        }

        val requestBody = jsonBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(API_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    emojiView.setImageResource(android.R.drawable.ic_dialog_alert)
                    emojiView.visibility = View.VISIBLE
                    println("API Error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody == null) {
                    runOnUiThread {
                        emojiView.setImageResource(android.R.drawable.ic_dialog_alert)
                        emojiView.visibility = View.VISIBLE
                        println("API Failure Response: ${response.code}")
                    }
                    return
                }

                println("API Response: $responseBody")

                try {
                    val jsonResponse = JSONObject(responseBody)
                    val sentimentResult = jsonResponse.getString("sentiment")

                    runOnUiThread {
                        emojiView.visibility = View.VISIBLE
                        when (sentimentResult.lowercase()) {
                            "positive" -> {
                                emojiView.setImageResource(R.drawable.smile_icon)
                                window.decorView.setBackgroundColor(
                                    ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_light)
                                )
                            }
                            "negative" -> {
                                emojiView.setImageResource(R.drawable.sad_icon)
                                window.decorView.setBackgroundColor(
                                    ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark)
                                )
                            }
                            "neutral" -> {
                                emojiView.setImageResource(R.drawable.neutral_icon)
                                window.decorView.setBackgroundColor(
                                    ContextCompat.getColor(this@MainActivity, android.R.color.darker_gray)
                                )
                            }
                            else -> {
                                emojiView.setImageResource(android.R.drawable.ic_dialog_alert)
                            }
                        }
                    }

                } catch (e: Exception) {
                    println("Error parsing API response: ${e.message}")
                    runOnUiThread {
                        emojiView.setImageResource(android.R.drawable.ic_dialog_alert)
                        emojiView.visibility = View.VISIBLE
                    }
                }
            }
        })
    }
}
