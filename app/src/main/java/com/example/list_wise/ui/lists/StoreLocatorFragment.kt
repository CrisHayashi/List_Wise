package com.example.list_wise.ui.lists

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class StoreLocatorFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var map: GoogleMap? = null
    private val markers = mutableListOf<MarkerOptions>()
    private var firstZoomDone = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_store_locator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Título no toolbar
        val titleView = requireActivity().findViewById<TextView>(R.id.txtToolbarTitle)
        titleView.text = getString(R.string.title_store_locator)

        // Cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Fragment do mapa dentro deste fragment
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Botão "Ativar localização"
        view.findViewById<View>(R.id.btnEnableLocation).setOnClickListener {
            enableMyLocation()
            setupLocationUpdates()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        setupMarkers()
        setupLocationUpdates()
    }

    /** Pede permissão e liga o "pontinho azul" do usuário */
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map?.isMyLocationEnabled = true
        } else {
            // pede permissão na primeira vez
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    /** Supermercados fixos (exemplo, Recife) */
    private fun setupMarkers() {
        // Usei nomes parecidos com o seu protótipo
        val stores = listOf(
            LatLng(-8.1222, -34.9167) to "Extra - Boa Viagem",
            LatLng(-8.0539, -34.9180) to "Carrefour",
            LatLng(-8.0470, -34.8970) to "Recibom",
            LatLng(-8.0715, -34.9500) to "Mix Mateus"
        )

        stores.forEach { (coords, name) ->
            markers.add(
                MarkerOptions()
                    .position(coords)
                    .title(name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }

        map?.let { googleMap ->
            markers.forEach { googleMap.addMarker(it) }
        }
    }

    /** Atualiza a posição do usuário e ajusta o zoom */
    private fun setupLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L  // a cada 5 segundos
        )
            .setMinUpdateDistanceMeters(5f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                val userLatLng = LatLng(location.latitude, location.longitude)

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

                    // Zoom inicial que pega usuário + mercados
                    if (!firstZoomDone) {
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                            )
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
            enableMyLocation()
            setupLocationUpdates()
        } else {
            Toast.makeText(
                requireContext(),
                "Permissão de localização é necessária para mostrar mercados próximos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}