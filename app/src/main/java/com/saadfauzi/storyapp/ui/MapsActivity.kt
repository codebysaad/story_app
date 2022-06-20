package com.saadfauzi.storyapp.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.saadfauzi.storyapp.R
import com.saadfauzi.storyapp.databinding.ActivityMapsBinding
import com.saadfauzi.storyapp.utils.MySettingsPreference
import com.saadfauzi.storyapp.utils.dateFormatter
import com.saadfauzi.storyapp.viewmodels.MapsVersionViewModel
import com.saadfauzi.storyapp.viewmodels.ViewModelFactory

private val Activity.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModel: MapsVersionViewModel
    private var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = MySettingsPreference.getInstance(this.dataStore)
        viewModel = ViewModelProvider(this, ViewModelFactory(this, pref))[
                MapsVersionViewModel::class.java
        ]

        viewModel.getAccessToken().observe(this) {
            token = it
        }

        viewModel.isLoading.observe(this) {
            showLoading(it)
        }

        viewModel.isMessage.observe(this) {
            Toast.makeText(this@MapsActivity, it.getContentIfNotHandled(), Toast.LENGTH_SHORT)
                .show()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        Log.d("Token", token)
        viewModel.getAllStories(token)
        viewModel.listStories.observe(this) { result ->
            if (result != null) {
                Log.d("MapsVersion", result.listStory.size.toString())
                for (story in result.listStory) {
                    val position = story.lat?.let { lat ->
                        story.lon?.let { lon ->
                            LatLng(lat, lon)
                        }
                    }
                    position?.let { latLng ->
                        MarkerOptions()
                            .position(latLng)
                            .title(story.name)
                            .snippet(dateFormatter(story.createdAt))
                    }?.let { marker ->
                        mMap.addMarker(
                            marker
                        )
                    }
                    position?.let { latLng ->
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    }?.let { cam ->
                        mMap.animateCamera(cam)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.map_style, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbMap.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}