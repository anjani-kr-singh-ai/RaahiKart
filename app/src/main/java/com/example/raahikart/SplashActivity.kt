package com.example.raahikart

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var logoImageView: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        logoImageView = findViewById(R.id.raahikart_logo)
        progressBar = findViewById(R.id.progressBar)

        // Start the animation sequence after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            startAnimationSequence()
        }, 500)
    }

    private fun startAnimationSequence() {
        // First animation: Scale down the logo
        val scaleDownAnim = AnimationUtils.loadAnimation(this, R.anim.logo_scale_down)
        scaleDownAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Show and animate the loader
                progressBar.visibility = View.VISIBLE
                val fadeInAnim = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.loader_fade_in)
                progressBar.startAnimation(fadeInAnim)

                // Simulate loading process
                simulateLoading()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        logoImageView.startAnimation(scaleDownAnim)
    }

    private fun simulateLoading() {
        // Simulate a loading process that takes 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            // When loading is complete, animate both logo and loader fading out
            val fadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.fade_out)
            fadeOutAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    // Transition to LoginActivity with a smooth fade transition
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)

                    // Add a fade transition
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

                    // Finish this activity
                    finish()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })

            logoImageView.startAnimation(fadeOutAnim)
            progressBar.startAnimation(fadeOutAnim)
        }, 3000) // 3 seconds loading simulation
    }
}