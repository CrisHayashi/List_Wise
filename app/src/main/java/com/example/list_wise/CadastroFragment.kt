package com.example.list_wise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.edit

class CadastroFragment : Fragment(R.layout.fragment_cadastro) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val edtName = view.findViewById<EditText>(R.id.editName)
        val edtEmail = view.findViewById<EditText>(R.id.txtLoginPrompt)
        val edtPassword = view.findViewById<EditText>(R.id.txtPasswordPrompt)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val btnLoginAccount = view.findViewById<TextView>(R.id.btnGoToLogin)

        // Clique no botão de cadastro
        btnRegister.setOnClickListener {
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.cadastro_error_message),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Verifica se já existe usuário com o mesmo email (no SharedPreferences)
            if (UserStorage.userExists(requireContext(), email)) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.login_found_message),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Salva novo usuário
            UserStorage.addUser(requireContext(), User(email, password))

            // 👉 salva o nome que vai aparecer no "Olá, Fulana!"
            val nomeParaSaudar =
                if (name.isNotBlank()) name else email.substringBefore("@")
            saveUserName(nomeParaSaudar)

            Toast.makeText(
                requireContext(),
                getString(R.string.cadastro_success_message),
                Toast.LENGTH_SHORT
            ).show()

            // Vai direto para MainActivity após cadastro
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // Clique em "já tenho conta" → volta para login
        btnLoginAccount.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun saveUserName(name: String) {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("user_name", name)
        }
    }
}
