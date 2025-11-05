package com.example.list_wise

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Pega a referência do logo
        val logo: ImageView = findViewById(R.id.logolistwise)
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale)

        // Aplica a animação no logo
        logo.startAnimation(animation)

        // Listener para detectar quando a animação terminar
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(anim: Animation?) {
                // nada aqui
            }

            override fun onAnimationRepeat(anim: Animation?) {
                // Nada a fazer aqui
            }

            override fun onAnimationEnd(anim: Animation?) {
                // espera 1s depois da animação (opcional)
                Handler(Looper.getMainLooper()).postDelayed({
                    goToLogin()
                }, 2000)
            }
        })
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}