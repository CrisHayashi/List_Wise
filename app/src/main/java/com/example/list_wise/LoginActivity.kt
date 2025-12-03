package com.example.list_wise

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity() {

    // --- GOOGLE SIGN-IN ---
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_GOOGLE_SIGN_IN = 1001

    // Scroll da tela de login (pra esconder/mostrar quando abre o cadastro)
    private lateinit var loginScroll: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // garante que exista o usuário admin/admin no armazenamento
        UserStorage.ensureAdmin(this)

        // pega o ScrollView do login
        loginScroll = findViewById(R.id.loginScroll)

        // Ajuste para considerar barras de status/navigation
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- CONFIG GOOGLE SIGN-IN (apenas e-mail) ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()          // só precisamos do e-mail/ nome
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Se já estiver logado com Google, pode pular direto pro MainActivity (opcional)
        val lastAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (lastAccount != null) {
            val name = lastAccount.displayName ?: lastAccount.email ?: "Usuário Google"
            saveUserName(name)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
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
                    Toast.makeText(
                        this,
                        getString(R.string.login_empty_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // LOGIN ADMIN (continua funcionando igual)
                login == "admin" && password == "admin" -> {
                    Toast.makeText(this, "Login admin OK", Toast.LENGTH_SHORT).show()
                    saveUserName("Admin")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

                // LOGIN USUÁRIOS CADASTRADOS (SharedPreferences)
                UserStorage.validateLogin(this, login, password) -> {
                    Toast.makeText(
                        this,
                        getString(R.string.login_success_message),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Aqui posso usar o próprio e-mail como "nome" salvo
                    val userName = login
                    saveUserName(userName)

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

                else -> {
                    Toast.makeText(
                        this,
                        getString(R.string.login_error_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Clique em "Criar conta" -> abre CadastroFragment e esconde o login
        btnCreateAccount.setOnClickListener {
            loginScroll.visibility = View.GONE   // some a tela de login

            supportFragmentManager.commit {
                replace(R.id.fragment_container, CadastroFragment())
                addToBackStack("cadastro") // permite voltar para o login
            }
        }

        // Clique em esqueci senha
        btnForgotPassword.setOnClickListener {
            Toast.makeText(this, "Tela de recuperação ainda não implementada", Toast.LENGTH_SHORT)
                .show()
        }

        // --- Clique no login com Google ---
        btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)

                // 👇 Aqui pegamos o NOME do usuário
                val name = account.displayName
                    ?: account.givenName
                    ?: account.email
                    ?: "Usuário Google"

                // Salvamos para o Dashboard ("Olá, Fulano!")
                saveUserName(name)

                Toast.makeText(this, "Login com Google OK", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } catch (e: ApiException) {
                Toast.makeText(
                    this,
                    "Falha no login Google (código ${e.statusCode})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /** Chamado pelo CadastroFragment quando o usuário volta para o login */
    fun showLoginAgain() {
        loginScroll.visibility = View.VISIBLE
    }

    /** Salva o nome do usuário para ser usado no Dashboard ("Olá, Fulana!") */
    private fun saveUserName(name: String) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit {
            putString("user_name", name)
            // apply() é chamado automaticamente pela extensão
        }
    }
}
