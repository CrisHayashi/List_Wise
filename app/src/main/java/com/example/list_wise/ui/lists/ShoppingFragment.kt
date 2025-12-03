package com.example.list_wise.ui.lists

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.list_wise.R
import com.example.list_wise.data.database.DatabaseHelper
import com.example.list_wise.data.model.ItemEntity
import com.example.list_wise.data.model.Lista
import com.example.list_wise.data.model.ListaItemRelation
import com.example.list_wise.data.repository.ListRepository
import com.example.list_wise.databinding.FragmentShoppingBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShoppingFragment : Fragment() {

    private var _binding: FragmentShoppingBinding? = null
    private val binding get() = _binding!!

    // DB / Repositório
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var repository: ListRepository

    // Lista ativa
    private var currentLista: Lista? = null

    // Adapter de categorias/itens
    private lateinit var categoryAdapter: CategoryAdapter
    private val displayCategories = mutableListOf<Category>()

    // Localização
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Pedido de permissão de localização
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineGranted || coarseGranted) {
                obterLocalizacaoAtual()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissão de localização negada.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar: título “Modo compra”
        val titleView = requireActivity().findViewById<TextView>(R.id.txtToolbarTitle)
        titleView.text = getString(R.string.title_shopping) // string: "Modo compra"

        // Inicializa DB e repositório
        dbHelper = DatabaseHelper(requireContext())
        repository = ListRepository(dbHelper)

        // Inicializa cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setupRecyclerView()
        carregarListaParaCompra()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Se voltar da tela de edição, recarrega a lista
        carregarListaParaCompra()
    }

    private fun setupRecyclerView() {
        // No modo compra, o checkbox significa "COMPRADO"
        categoryAdapter = CategoryAdapter(
            onAddItem = { _ ->
                Toast.makeText(
                    requireContext(),
                    "Adicionar itens só é permitido no modo lista.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onEditItem = { _ ->
                Toast.makeText(
                    requireContext(),
                    "Editar itens só é permitido no modo lista.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDeleteItem = { _ ->
                Toast.makeText(
                    requireContext(),
                    "Excluir itens só é permitido no modo lista.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onCheckItem = { item, isChecked ->
                // Aqui o checkbox representa "comprado"
                if (item.relationId > 0) {
                    repository.marcarItemComoComprado(
                        item.relationId,
                        isChecked,
                        if (isChecked) item.preco else null
                    )
                    carregarListaParaCompra()
                }
            }
        )

        binding.recyclerViewCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            isVerticalScrollBarEnabled = true
        }
    }

    private fun carregarListaParaCompra() {
        val lista = repository.getListaDesejadaAtiva()
        currentLista = lista

        if (lista != null) {
            // Usa o nome real da lista
            binding.textListName.text = lista.nome

            // Carregamos apenas os itens QUE PERTENCEM à lista
            val itensDoBanco = repository.obterItensDaLista(lista.id)
            atualizarListaVisual(itensDoBanco)
        } else {
            // Nenhuma lista ativa: mostra texto padrão
            binding.textListName.text = getString(R.string.shopping_list_title_default)
            Toast.makeText(
                requireContext(),
                "Nenhuma lista desejada ativa. Crie ou selecione uma lista.",
                Toast.LENGTH_SHORT
            ).show()

            // limpa a tela
            displayCategories.clear()
            categoryAdapter.submitList(displayCategories.toList())
        }
    }

    // Converte os dados do DB para a estrutura de categorias/itens da UI
    private fun atualizarListaVisual(
        itensDb: List<Triple<ItemEntity, ListaItemRelation, Boolean>>
    ) {
        displayCategories.clear()

        // Agrupa por categoria
        val agrupado = itensDb.groupBy { triple ->
            val entity = triple.first
            entity.categoria ?: "Sem Categoria"
        }

        agrupado.forEach { (nomeCategoria, listaTriples) ->
            val uiItems = listaTriples.map { triple ->
                val entity = triple.first
                val relation = triple.second
                val comprado = triple.third

                Item(
                    id = entity.id,
                    relationId = relation.id,
                    nome = entity.nome,
                    marca = entity.marca ?: "",
                    quantidade = relation.quantidadeDesejada,
                    preco = relation.precoEstimado,
                    isSelected = comprado, // aqui significa "comprado"
                    categoria = entity.categoria
                )
            }.toMutableList()

            displayCategories.add(
                Category(
                    name = nomeCategoria,
                    items = uiItems,
                    expanded = true
                )
            )
        }

        categoryAdapter.submitList(displayCategories.toList())
    }

    private fun setupListeners() {
        // Botão "Finalizar compra"
        binding.btnFinishPurchase.setOnClickListener {
            finalizarCompra()
        }
        // Botão "Editar lista" → volta para a tela anterior (ListFragment)
        binding.btnEditList.setOnClickListener {
            // Volta um nível na pilha de navegação
            findNavController().popBackStack()
            // ou: findNavController().navigateUp()
        }

        // Botão "Usar minha localização"
        binding.btnUseLocation.setOnClickListener {
            solicitarOuObterLocalizacao()
        }
    }

    /** Verifica permissão e, se tiver OK, busca a localização atual */
    private fun solicitarOuObterLocalizacao() {
        val context = requireContext()

        val finePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (finePermission == PackageManager.PERMISSION_GRANTED ||
            coarsePermission == PackageManager.PERMISSION_GRANTED
        ) {
            obterLocalizacaoAtual()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /** Usa FusedLocation + Geocoder para preencher o endereço */
    private fun obterLocalizacaoAtual() {
        // Confere de novo a permissão – isso deixa o Lint feliz e evita crash se algo mudar
        val context = requireContext()
        val finePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (finePermission != PackageManager.PERMISSION_GRANTED &&
            coarsePermission != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                context,
                "Permissão de localização não concedida.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        try {
                            val geocoder = Geocoder(requireContext(), Locale.getDefault())
                            val results = geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            )

                            if (!results.isNullOrEmpty()) {
                                val address = results[0]
                                val linhaEndereco = address.getAddressLine(0) ?: ""

                                // Preenche endereço
                                binding.editStoreAddress.setText(linhaEndereco)

                                // Se nome do local estiver vazio, sugere algo
                                if (binding.editStoreName.text.isNullOrBlank()) {
                                    val nomeLocal =
                                        address.subLocality
                                            ?: address.thoroughfare
                                            ?: address.locality
                                            ?: "Supermercado"
                                    binding.editStoreName.setText(nomeLocal)
                                }

                                Toast.makeText(
                                    requireContext(),
                                    "Endereço preenchido pela localização atual.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Não foi possível obter o endereço.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                requireContext(),
                                "Erro ao converter localização em endereço.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Localização indisponível. Tente novamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao obter localização.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (se: SecurityException) {
            // Caso extremo: se por algum motivo a permissão for revogada no meio do caminho
            Toast.makeText(
                requireContext(),
                "Sem permissão para acessar localização.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun finalizarCompra() {
        val lista = currentLista ?: run {
            Toast.makeText(requireContext(), "Nenhuma lista ativa.", Toast.LENGTH_SHORT).show()
            return
        }

        val dataHoje = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        // Usa precoPago (quando houver), senão precoEstimado
        val total = repository.calcularTotalGastoDaLista(lista.id, isHistorico = true)

        // Lê os campos de local e endereço
        val local = binding.editStoreName.text?.toString()?.trim().orEmpty()
        val endereco = binding.editStoreAddress.text?.toString()?.trim().orEmpty()

        val localOuNull = local.ifBlank { null }
        val enderecoOuNull = endereco.ifBlank { null }

        val sucesso = repository.finalizarListaEMigrar(
            listaId = lista.id,
            dataFinalizacao = dataHoje,
            local = localOuNull,
            endereco = enderecoOuNull,
            totalGasto = total
        )

        if (sucesso) {
            Toast.makeText(
                requireContext(),
                "Compra finalizada! Lista movida para o histórico.",
                Toast.LENGTH_LONG
            ).show()
            // Vai para o histórico depois de finalizar
            findNavController().navigate(R.id.historicFragment)
        } else {
            Toast.makeText(requireContext(), "Erro ao finalizar compra.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        dbHelper.close()
        super.onDestroyView()
        _binding = null
    }
}
