package com.example.list_wise.ui.lists

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.list_wise.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class StoreLocatorFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var map: GoogleMap? = null
    private val markers = mutableListOf<MarkerOptions>()
    private var firstZoomDone = false
    private var requestingLocationUpdates = false
    private var hintTextView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_store_locator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        // Título no toolbar (usa ? para não quebrar se não existir)
        val titleView = requireActivity().findViewById<TextView>(R.id.txtToolbarTitle)
        titleView?.text = getString(R.string.title_store_locator)

        // Subtítulo da tela (texto embaixo do título)
        hintTextView = view.findViewById(R.id.txtLocationHint)
        hintTextView?.text = getString(R.string.location_hint)

        // Cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        Log.d(TAG, "FusedLocationProviderClient inicializado")

        // Fragment do mapa dentro deste fragment
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment

        if (mapFragment == null) {
            Log.e(TAG, "mapFragment é null! Verifique o id R.id.mapFragment no XML.")
        } else {
            Log.d(TAG, "getMapAsync chamado")
            mapFragment.getMapAsync(this)
        }

        // Botão "Ativar localização"
        view.findViewById<View>(R.id.btnEnableLocation).setOnClickListener {
            Log.d(TAG, "Botão Ativar Localização clicado")
            enableMyLocation()
            setupLocationUpdates()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady - mapa pronto")
        map = googleMap
        enableMyLocation()
        setupMarkers()
        setupLocationUpdates()
    }

    /** Pede permissão e liga o "pontinho azul" do usuário */
    private fun enableMyLocation() {
        Log.d(TAG, "enableMyLocation chamado")

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Permissão de localização já concedida")
            map?.isMyLocationEnabled = true
        } else {
            Log.d(TAG, "Solicitando permissão de localização")
            // pede permissão na primeira vez
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    /** Supermercados fixos (exemplo, próximos à Faculdade Nova Roma) */
    private fun setupMarkers() {
        Log.d(TAG, "setupMarkers chamado")

        markers.clear()

        // Título: curto / profissional
        // Snippet: endereço / bairro mais detalhado
        data class StorePoint(
            val position: LatLng,
            val title: String,
            val snippet: String
        )

        // Coordenadas aproximadas da Faculdade Nova Roma / Shopping Recife / supermercados
        val stores = listOf(
            StorePoint(
                position = LatLng(-8.1227048, -34.9012738),
                title = "Recibom Boa Viagem",
                snippet = "Rua Prof. João Medeiros, 261 – Boa Viagem"
            ),
            StorePoint(
                position = LatLng(-8.11904, -34.90489),
                title = "Carrefour Shopping Recife",
                snippet = "Shopping Recife – Rua Padre Carapuceiro, 777 - Boa Viagem"
            ),
            StorePoint(
                position = LatLng(-8.11692, -34.90248),
                title = "Mix Mateus Boa Viagem",
                snippet = "Rua Padre Carapuceiro, 800 - Boa Viagem"
            )
        )


        // Mercados
        stores.forEach { store ->
            val marker = MarkerOptions()
                .position(store.position)
                .title(store.title)
                .snippet(store.snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
            markers.add(marker)
        }

        map?.let { googleMap ->
            Log.d(TAG, "Adicionando ${markers.size} markers no mapa")
            markers.forEach { googleMap.addMarker(it) }
        }
    }

    /** Atualiza a posição do usuário e ajusta o zoom */
    private fun setupLocationUpdates() {
        Log.d(TAG, "setupLocationUpdates chamado")

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "setupLocationUpdates abortado: sem permissão")
            return
        }

        if (requestingLocationUpdates) {
            Log.d(TAG, "Já está solicitando updates de localização, ignorando chamada extra")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L  // a cada 5 segundos
        )
            .setMinUpdateDistanceMeters(5f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: run {
                    Log.w(TAG, "onLocationResult: lastLocation é null")
                    return
                }
                val userLatLng = LatLng(location.latitude, location.longitude)
                Log.d(TAG, "onLocationResult: lat=${location.latitude}, lng=${location.longitude}")

                map?.let { googleMap ->
                    // Limpa só os marcadores do mapa e adiciona novamente
                    googleMap.clear()
                    markers.forEach { googleMap.addMarker(it) }

                    // Marcador do usuário
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(userLatLng)
                            .title("Você está aqui")
                            .icon(
                                BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_AZURE
                                )
                            )
                    )

                    // CÍRCULO de 1km em volta do USUÁRIO
                    googleMap.addCircle(
                        CircleOptions()
                            .center(userLatLng)
                            .radius(1000.0) // 1 km
                            .strokeWidth(2f)
                            .strokeColor(Color.BLUE)
                            .fillColor(0x220000FF) // azul translúcido
                    )

                    // Atualiza o texto explicando o raio
                    hintTextView?.text = getString(R.string.location_hint_radius, 1)
                    Log.d(TAG, "Texto atualizado: raio de 1 km ao redor do usuário")

                    // Zoom inicial que pega usuário + mercados
                    if (!firstZoomDone) {
                        Log.d(TAG, "Aplicando primeiro zoom no usuário com bounds")

                        val boundsBuilder = LatLngBounds.Builder()

                        // Inclui o usuário
                        boundsBuilder.include(userLatLng)

                        // Inclui todos os mercados
                        markers.forEach { boundsBuilder.include(it.position) }

                        val bounds = boundsBuilder.build()
                        val padding = 100 // px

                        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                        googleMap.animateCamera(cameraUpdate)

                        firstZoomDone = true
                    }

                }
            }
        }


        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        requestingLocationUpdates = true
        Log.d(TAG, "requestLocationUpdates registrado")
    }

    /** Retorno da permissão de localização */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Permissão de localização CONCEDIDA pelo usuário")
            enableMyLocation()
            setupLocationUpdates()
        } else {
            Log.w(TAG, "Permissão de localização NEGADA pelo usuário")
            Toast.makeText(
                requireContext(),
                "Permissão de localização é necessária para mostrar mercados próximos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        // IMPORTANTE: só chama removeLocationUpdates se o callback já foi inicializado
        if (this::locationCallback.isInitialized) {
            Log.d(TAG, "Removendo location updates")
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } else {
            Log.d(TAG, "locationCallback NÃO foi inicializado, nada para remover")
        }

        requestingLocationUpdates = false
        map = null
        super.onDestroyView()
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val TAG = "StoreLocatorFragment"
    }
}
