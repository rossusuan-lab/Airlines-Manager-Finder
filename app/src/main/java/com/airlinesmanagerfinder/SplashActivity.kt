package com.airlinesmanagerfinder

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.Gravity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Layout sederhana langsung di Kotlin
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(android.graphics.Color.parseColor("#36719D"))

            val logo = ImageView(context).apply {
                setImageResource(R.drawable.logo) // logo.png taruh di res/drawable/
                adjustViewBounds = true
                layoutParams = LinearLayout.LayoutParams(640, 640)
            }
            addView(logo)
        }

        setContentView(layout)

        // Delay 3 detik lalu pindah ke MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 5000)
    }
}