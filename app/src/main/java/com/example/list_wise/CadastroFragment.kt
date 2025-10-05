package com.example.list_wise

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class CadastroFragment : Fragment(R.layout.fragment_cadastro) {

    companion object {
        // Lista de usuários em memória, já com admin
        var users = mutableListOf<User>().apply {
            add(User("admin", "admin")) // usuário admin mock
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referências dos campos de cadastro
        //val edtName = view.findViewById<EditText>(R.id.newUserPrompt)
        val edtEmail = view.findViewById<EditText>(R.id.txtLoginPrompt)
        val edtPassword = view.findViewById<EditText>(R.id.txtPasswordPrompt)
        val btnRegister = view.findViewById<Button>(R.id.btnLogin)
        val btnLoginAccount = view.findViewById<Button>(R.id.btnCreatAccount)

        // Clique no botão de cadastro
        btnRegister.setOnClickListener {
            //val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            // Verifica campos vazios
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.cadastro_error_message), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verifica se já existe usuário com o mesmo email
            val existingUser = users.find { it.login == email }
            if (existingUser != null) {
                Toast.makeText(requireContext(), getString(R.string.login_found_message), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Adiciona novo usuário à lista
            users.add(User(email, password))
            Toast.makeText(requireContext(), getString(R.string.cadastro_success_message), Toast.LENGTH_SHORT).show()

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
}