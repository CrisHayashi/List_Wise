package com.example.list_wise.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.list_wise.databinding.FragmentCreateListBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.list_wise.R
import com.example.list_wise.data.database.DatabaseHelper
import com.example.list_wise.data.model.Lista
import com.example.list_wise.data.repository.ListRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateListFragment : Fragment() {

    private var _binding: FragmentCreateListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ListRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = ListRepository(DatabaseHelper(requireContext()))

        // Se já existir uma lista desejada, mostra o nome para permitir renomear
        val listaAtual = repository.getListaDesejadaAtiva()
        if (listaAtual != null) {
            binding.editListName.setText(listaAtual.nome)
        }

        binding.btnSaveList.setOnClickListener {
            val listName = binding.editListName.text.toString().trim()
            if (listName.isEmpty()) {
                Toast.makeText(requireContext(), "Digite o nome da lista", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val listaDesejadaExistente = repository.getListaDesejadaAtiva()
            val listaId: Int = if (listaDesejadaExistente == null) {
                // Criar nova lista desejada
                val dataCriacao = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date())

                val novaLista = Lista(
                    nome = listName,
                    dataCriacao = dataCriacao,
                    finalizada = false
                )

                val idGerado = repository.inserirLista(novaLista)
                if (idGerado == -1L) {
                    Toast.makeText(
                        requireContext(),
                        "Já existe uma lista desejada ativa.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                idGerado.toInt()
            } else {
                // Renomear a lista desejada existente
                repository.atualizarNomeLista(listaDesejadaExistente.id, listName)
                listaDesejadaExistente.id
            }

            Toast.makeText(requireContext(), "Lista \"$listName\" pronta para usar!", Toast.LENGTH_SHORT).show()

            // Navega para ListFragment com Safe Args (sem parâmetro, pois a action não define args)
            val action = CreateListFragmentDirections.actionCreateListToList()
            findNavController().navigate(action)

            // Atualiza o BottomNavigation para marcar o primeiro botão (ListFragment)
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
