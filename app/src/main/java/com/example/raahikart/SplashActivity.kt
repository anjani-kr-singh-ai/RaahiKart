package com.example.raahikart

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var logoImageView: ImageView
    private lateinit var logoContainer: FrameLayout
    private lateinit var progressContainer: ViewGroup
    private lateinit var progressFill: View
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        logoImageView = findViewById(R.id.raahikart_logo)
        logoContainer = findViewById(R.id.logo_container)
        progressContainer = findViewById(R.id.progress_container)
        progressFill = findViewById(R.id.progress_fill)
        progressText = findViewById(R.id.progress_text)

        // Start the animation sequence after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            startAnimationSequence()
        }, 500)
    }

    private fun startAnimationSequence() {
        // Step 1: Initial logo pulse animation
        val pulseLogo = AnimationUtils.loadAnimation(this, R.anim.logo_pulse)
        pulseLogo.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Step 2: Reposition logo slightly (but keep it visible)
                repositionLogoAndShowProgressBar()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        logoImageView.startAnimation(pulseLogo)
    }

    private fun repositionLogoAndShowProgressBar() {
        val morphAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_morph_to_bar)
        morphAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Step 3: Show the progress bar with reveal animation
                progressContainer.visibility = View.VISIBLE
                val revealAnimation = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.progress_reveal)
                progressContainer.startAnimation(revealAnimation)

                // Step 4: Start filling the progress bar
                revealAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        animateProgressBar()
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        logoContainer.startAnimation(morphAnimation)
    }

    private fun animateProgressBar() {
        val progressBarWidth = (progressFill.parent as View).width
        val totalDuration = 3500L // 3.5 seconds for the fill animation

        // Create a progress sequence with different speeds to add visual interest
        val keyframes = listOf(
            Pair(0f, 0L),        // Start
            Pair(0.15f, 600L),   // Quick initial progress
            Pair(0.45f, 1200L),  // Slow down
            Pair(0.6f, 1800L),   // Even slower
            Pair(0.75f, 2200L),  // Speed up a bit
            Pair(0.9f, 2800L),   // Slow for anticipation
            Pair(1f, totalDuration)     // Final push
        )

        // Animate each segment separately for varying speeds
        for (i in 0 until keyframes.size - 1) {
            val startValue = keyframes[i].first
            val endValue = keyframes[i + 1].first
            val startTime = keyframes[i].second
            val duration = keyframes[i + 1].second - startTime

            val segmentAnimator = ValueAnimator.ofFloat(startValue, endValue)
            segmentAnimator.startDelay = startTime
            segmentAnimator.duration = duration

            // Use different interpolators for different segments for visual variety
            when (i) {
                0 -> segmentAnimator.interpolator = android.view.animation.AccelerateInterpolator(1.5f)
                1 -> segmentAnimator.interpolator = android.view.animation.DecelerateInterpolator(1.2f)
                2 -> segmentAnimator.interpolator = android.view.animation.LinearInterpolator()
                3 -> segmentAnimator.interpolator = android.view.animation.AccelerateInterpolator()
                else -> segmentAnimator.interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            }

            segmentAnimator.addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                val layoutParams = progressFill.layoutParams
                layoutParams.width = (progressBarWidth * value).toInt()
                progressFill.layoutParams = layoutParams

                // Update percentage text
                val percentComplete = (value * 100).toInt()
                progressText.text = "$percentComplete%"

                // Add visual feedback at certain milestones
                if (percentComplete % 25 == 0 && percentComplete > 0 && (layoutParams.width - (progressBarWidth * (percentComplete - 1) / 100)) < 10) {
                    addProgressMilestoneEffect(percentComplete)
                }
            }

            if (i == keyframes.size - 2) {
                segmentAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        // Create a ripple effect on completion
                        createCompletionEffect()

                        // When progress is complete, fade out and launch login
                        Handler(Looper.getMainLooper()).postDelayed({
                            finishSplashAndLaunchLogin()
                        }, 600)
                    }
                })
            }

            segmentAnimator.start()
        }
    }

    private fun addProgressMilestoneEffect(milestone: Int) {
        // Create a subtle flash/pulse effect at the current progress position
        val flash = View(this)
        flash.layoutParams = ViewGroup.LayoutParams(6,
            (progressFill.height * 1.5).toInt())
        flash.setBackgroundColor(android.graphics.Color.DKGRAY)
        flash.alpha = 0.7f

        // Add it to the progress bar container
        (progressFill.parent as ViewGroup).addView(flash)

        // Position at current progress
        flash.translationX = progressFill.width.toFloat() - 3
        flash.translationY = -progressFill.height * 0.25f

        // Animate it
        flash.animate()
            .alpha(0f)
            .scaleX(3f)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    (flash.parent as ViewGroup).removeView(flash)
                }
            })
            .start()

        // Also pulse the text
        val originalColor = progressText.currentTextColor
        progressText.animate()
            .scaleX(1.3f).scaleY(1.3f)
            .setDuration(200)
            .withEndAction {
                progressText.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    private fun createCompletionEffect() {
        // First, create a brief pause for effect
        progressFill.postDelayed({
            // Animate a ripple effect across the progress bar
            val ripple = ObjectAnimator.ofFloat(progressFill, "translationY", 0f, -2f, 0f, 2f, 0f)
            ripple.repeatCount = 1
            ripple.duration = 300
            ripple.start()

            // Add a color overlay and animate it
            val colorOverlay = View(this)
            colorOverlay.layoutParams = ViewGroup.LayoutParams(
                progressFill.width,
                progressFill.height)
            colorOverlay.setBackgroundColor(android.graphics.Color.DKGRAY)
            colorOverlay.alpha = 0f

            (progressFill.parent as ViewGroup).addView(colorOverlay)
            colorOverlay.translationY = progressFill.translationY

            // Animate the overlay
            colorOverlay.animate()
                .alpha(0.7f)
                .setDuration(200)
                .withEndAction {
                    colorOverlay.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            (colorOverlay.parent as ViewGroup).removeView(colorOverlay)
                        }
                        .start()
                }
                .start()

            // Also make the progress text celebrate completion
            progressText.setText("READY!")
            progressText.setTextColor(android.graphics.Color.BLACK)
            progressText.animate()
                .scaleX(1.5f).scaleY(1.5f)
                .setDuration(300)
                .withEndAction {
                    progressText.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }, 100)
    }

    private fun finishSplashAndLaunchLogin() {
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.full_screen_fade_out)
        val rootView = findViewById<View>(android.R.id.content)

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Navigate to the login activity with custom transition
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)

                // Apply custom transition
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)

                finish()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        rootView.startAnimation(fadeOut)
    }
}