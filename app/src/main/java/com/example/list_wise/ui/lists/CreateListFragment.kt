package com.example.list_wise.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.list_wise.databinding.FragmentCreateListBinding
import com.example.list_wise.ui.lists.CreateListFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.list_wise.R

class CreateListFragment : Fragment() {

    private var _binding: FragmentCreateListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveList.setOnClickListener {
            val listName = binding.editListName.text.toString().trim()
            if (listName.isEmpty()) {
                Toast.makeText(requireContext(), "Digite o nome da lista", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(requireContext(), "Lista \"$listName\" criada!", Toast.LENGTH_SHORT).show()

            // Navega para ListFragment com Safe Args
            val action = CreateListFragmentDirections.actionCreateListToList(listName)
            findNavController().navigate(action)

            // Atualiza o BottomNavigation para marcar o primeiro botão (ListFragment)
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.listFragment // <-- id do primeiro botão do bottom nav
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
