package com.simats.unimind

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startDotBlinkAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }, 2500)
    }

    private fun startDotBlinkAnimation() {
        val dot1 = findViewById<View>(R.id.splash_dot1)
        val dot2 = findViewById<View>(R.id.splash_dot2)
        val dot3 = findViewById<View>(R.id.splash_dot3)

        val duration = 400L
        val blink = { view: View ->
            ObjectAnimator.ofFloat(view, View.ALPHA, 0.3f, 1f).apply {
                this.duration = duration
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
            }
        }

        blink(dot1).start()
        dot2.postDelayed({ blink(dot2).start() }, 150)
        dot3.postDelayed({ blink(dot3).start() }, 300)
    }
}
