package com.saadfauzi.storyapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.saadfauzi.storyapp.R
import com.saadfauzi.storyapp.databinding.ActivityMainBinding
import com.saadfauzi.storyapp.utils.MySettingsPreference
import com.saadfauzi.storyapp.viewmodels.MainViewModel
import com.saadfauzi.storyapp.viewmodels.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val pref = MySettingsPreference.getInstance(dataStore)

        viewModel = ViewModelProvider(this, ViewModelFactory(this, pref))[
                MainViewModel::class.java
        ]

        if (!allPermissionsGranted()){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.map -> {
                startActivity(Intent(this, MapsActivity::class.java))
            }
            R.id.language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
            R.id.logout -> {
                logoutDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun logoutDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.app_name))
        builder.setMessage(resources.getString(R.string.dialog_logout))
        builder.setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
            viewModel.saveAccessToken(false, "No auth")
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ -> dialogInterface.cancel() }
        builder.show()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}