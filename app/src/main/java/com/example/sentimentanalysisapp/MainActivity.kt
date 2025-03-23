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
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var inputSentence: EditText
    private lateinit var submitButton: Button
    private lateinit var emojiView: ImageView

    private val client = OkHttpClient()
    private val API_KEY = "AIzaSyAT80JH2kmk3oFy6kmLSSEwpjUTmhFQcMQ"
    private val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-pro-exp-02-05:generateContent?key=$API_KEY"

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
        val prompt = "Analyze the sentiment of this text: \"$sentence\".\n" +
                "Reply ONLY with one word: Positive, Negative, or Neutral. No explanation."

        val jsonBody = JSONObject().apply {
            val contents = JSONArray()
            val parts = JSONObject().put("text", prompt)
            val partsArray = JSONArray().put(parts)
            val content = JSONObject().put("parts", partsArray)
            contents.put(content)
            put("contents", contents)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            jsonBody.toString()
        )

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
                if (responseBody != null) {
                    println("API Response: $responseBody")
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val sentimentResult = jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")
                            .trim()

                        runOnUiThread {
                            emojiView.visibility = View.VISIBLE // Hiện icon khi có kết quả
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
            }
        })
    }
}
