package com.example.list_wise.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.list_wise.R

class PlaceholderFragment : Fragment() {

    companion object {
        private const val ARG_MESSAGE = "message"

        fun newInstance(message: String) = PlaceholderFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MESSAGE, message)
            }
        }
    }

    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        message = arguments?.getString(ARG_MESSAGE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Exibe Toast ao criar view
        Toast.makeText(requireContext(), message ?: "Tela em desenvolvimento", Toast.LENGTH_SHORT).show()

        // Retorna uma View simples, pode ser uma layout com só um TextView, por exemplo
        return inflater.inflate(R.layout.fragment_placeholder, container, false)
    }
}