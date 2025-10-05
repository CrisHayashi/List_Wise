package com.example.list_wise.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
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

        val btnCreate = view.findViewById<Button>(R.id.btnCreateList)
        val btnWishlist = view.findViewById<Button>(R.id.btnWishlist)
        val btnPurchased = view.findViewById<Button>(R.id.btnPurchased)

        btnCreate.setOnClickListener {
            Toast.makeText(requireContext(), "Criar nova lista...", Toast.LENGTH_SHORT).show()
        }

        btnWishlist.setOnClickListener {
            Toast.makeText(requireContext(), "Abrindo listas desejadas...", Toast.LENGTH_SHORT).show()
        }

        btnPurchased.setOnClickListener {
            Toast.makeText(requireContext(), "Abrindo listas compradas...", Toast.LENGTH_SHORT).show()
        }
    }
}