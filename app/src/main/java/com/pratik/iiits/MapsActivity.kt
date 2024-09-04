package com.pratik.iiits

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pratik.iiits.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val mapTypeButton: ImageButton = findViewById(R.id.map_type)
        val popupMenu = PopupMenu(this, mapTypeButton)
        popupMenu.menuInflater.inflate(R.menu.map_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            changeMap(menuItem.itemId)
            true
        }
        mapTypeButton.setOnClickListener {
            popupMenu.show()
        }
    }

    private fun changeMap(itemId: Int) {
        when (itemId) {
            R.id.normal_map -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map -> mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.setellite_map -> mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add markers for each point of interest
        val pointsOfInterest = listOf(
            POI("Classroom Building", "Academic block", LatLng(13.555537351159236, 80.02665122939517), "Hours: 8 AM - 6 PM", "Contact: 123-456-7890"),
            POI("Cafeteria", "Cafeteria", LatLng(13.5562876468723, 80.02639170510895), "Hours: 7 AM - 8 PM", "Contact: 123-456-7892"),
            POI("Boys Hostel 1", "Boys Hostel 1", LatLng(13.557135049996454, 80.02517515846479), "Hours: 6 AM - 9 PM", "Contact: 123-456-7893"),
            POI("Boys Hostel 2", "Boys Hostel 2", LatLng(13.55682546801932, 80.02504320507725), "Hours: 6 AM - 9 PM", "Contact: 123-456-7893"),
            POI("Dining Hall 01", "Dining Hall 1", LatLng(13.5569065045269, 80.02514745657533), "Hours: 6 AM - 9 PM", "Contact: 123-456-7893"),
            POI("Boys Hostel 3", "Boys Hostel 3", LatLng(13.553578, 80.025984), "Hours: 6 AM - 9 PM", "Contact: 123-456-7893"),
            POI("Boys Hostel 4", "Boys Hostel 4", LatLng(13.552592, 80.026124), "Hours: 6 AM - 9 PM", "Contact: 123-456-7893"),
            POI("Dining Hall 02", "Dining Hall 2", LatLng(13.553072, 80.026059), "Hours: 6 AM - 9 PM", "Contact: 123-456-7893"),
            POI("Petal Park", "Petal Park", LatLng(13.55613, 80.027213), "Hours: 10 AM - 11 PM", "Contact: 123-456-7895")
        )

        for (poi in pointsOfInterest) {
            val markerOptions = MarkerOptions()
                .position(poi.latLng)
                .title(poi.shortName)
                .snippet(poi.info)

            val marker = mMap.addMarker(markerOptions)
            marker?.tag = poi

            // Log marker details for debugging
            println("Marker added: ${poi.shortName} at ${poi.latLng}")
        }

        mMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        mMap.setOnInfoWindowClickListener { marker ->
            val poi = marker.tag as? POI
            poi?.let {
                showPOIDialog(it)
            }
        }

        // Move the camera to the first POI
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pointsOfInterest.first().latLng, 15f))

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            MaterialAlertDialogBuilder(this)
                .setTitle("Location Permission")
                .setMessage("This app needs location access to show your current location on the map.")
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    enableMyLocation()
                } else {
                    // Permission denied
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Location Permission Denied")
                        .setMessage("The app needs location permission to function properly.")
                        .setPositiveButton("OK", null)
                        .show()
                }
                return
            }
        }
    }

    private fun showPOIDialog(poi: POI) {
        val dialogFragment = POIInfoDialogFragment.newInstance(poi.name, poi.info, poi.contact)
        dialogFragment.show(supportFragmentManager, "POIInfoDialog")
    }

    data class POI(val name: String, val shortName: String, val latLng: LatLng, val info: String, val contact: String)
}
