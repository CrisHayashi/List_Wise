package com.example.list_wise.ui.lists

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.example.list_wise.R

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textWelcome = view.findViewById<TextView>(R.id.textWelcome)

        // Busca o nome salvo
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", null)

        textWelcome.text = if (!userName.isNullOrBlank()) {
            getString(R.string.dashboard_welcome_named, userName)
        } else {
            getString(R.string.dashboard_welcome_generic)
        }

        val btnCreate = view.findViewById<Button>(R.id.btnCreateList)
        val btnWishlist = view.findViewById<Button>(R.id.btnWishlist)
        val btnPurchased = view.findViewById<Button>(R.id.btnPurchased)

        btnCreate.setOnClickListener {
            // Navega para o CreateListFragment
            findNavController().navigate(R.id.action_dashboard_to_createList)
        }

        btnWishlist.setOnClickListener {
            // Abre a lista desejada (lista ativa)
            findNavController().navigate(R.id.action_dashboard_to_listFragment)
        }

        btnPurchased.setOnClickListener {
            // Abre a tela de histórico (listas compradas)
            findNavController().navigate(R.id.action_dashboard_to_historicFragment)
        }
    }
}
