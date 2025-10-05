package com.example.list_wise

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import android.widget.TextView
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Intent


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Ajuste para considerar barras de status/navigation
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referências do layout de login
        val txtLoginPrompt = findViewById<EditText>(R.id.txtLoginPrompt)
        val txtPasswordPrompt = findViewById<EditText>(R.id.txtPasswordPrompt)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnCreateAccount = findViewById<TextView>(R.id.btnCreatAccount)
        val btnForgotPassword = findViewById<TextView>(R.id.txtForgotPassword)
        val btnGoogleLogin = findViewById<LinearLayout>(R.id.btnGoogleLogin)

        // Clique no botão de login
        btnLogin.setOnClickListener {
            val login = txtLoginPrompt.text.toString().trim()
            val password = txtPasswordPrompt.text.toString().trim()

            when {
                login.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.login_empty_message), Toast.LENGTH_SHORT).show()
                }

                // LOGIN MOCK ADMIN
                login == "admin" && password == "admin" -> {
                    Toast.makeText(this, "Login admin OK", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                // LOGIN USUÁRIOS CADASTRADOS
                CadastroFragment.users.any { it.login == login && it.password == password } -> {
                    Toast.makeText(this, getString(R.string.login_success_message), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> {
                    Toast.makeText(this, getString(R.string.login_error_message), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Clique em criar conta → abre CadastroFragment
        btnCreateAccount.setOnClickListener {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, CadastroFragment())
                addToBackStack(null) // permite voltar para o login
            }
        }

        // Clique em esqueci senha
        btnForgotPassword.setOnClickListener {
            // TODO: startActivity(Intent(this, RecoveryActivity::class.java))
            Toast.makeText(this, "Tela de recuperação ainda não implementada", Toast.LENGTH_SHORT).show()
        }

        // Clique no login com Google
        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Login com Google ainda não implementado", Toast.LENGTH_SHORT).show()
        }
    }
}
