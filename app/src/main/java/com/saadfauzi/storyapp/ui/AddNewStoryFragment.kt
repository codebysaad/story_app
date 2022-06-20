package com.saadfauzi.storyapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.saadfauzi.storyapp.R
import com.saadfauzi.storyapp.databinding.FragmentAddNewStoryBinding
import com.saadfauzi.storyapp.utils.MySettingsPreference
import com.saadfauzi.storyapp.utils.reduceFileImage
import com.saadfauzi.storyapp.utils.uriToFile
import com.saadfauzi.storyapp.viewmodels.AddNewStoryViewModel
import com.saadfauzi.storyapp.viewmodels.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

private val Activity.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AddNewStoryFragment : Fragment() {

    private val binding by lazy {
        FragmentAddNewStoryBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: AddNewStoryViewModel
    private lateinit var currentPhotoPath: String
    private var getFile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lat: RequestBody
    private lateinit var lon: RequestBody

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        activity?.let { base ->
            ContextCompat.checkSelfPermission(
                base.baseContext,
                it
            )
        } == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val pref = MySettingsPreference.getInstance(requireActivity().dataStore)
        viewModel = ViewModelProvider(this, ViewModelFactory(requireContext(), pref))[
                AddNewStoryViewModel::class.java
        ]

        viewModel.apply {
            isLoading.observe(viewLifecycleOwner) {
                showLoading(it)
            }
            isMessage.observe(viewLifecycleOwner) {
                Toast.makeText(requireActivity(), it.getContentIfNotHandled(), Toast.LENGTH_SHORT)
                    .show()
            }
            location.observe(viewLifecycleOwner) {
                binding.tvLonLat.text = it
            }
        }

        setButtonUploadEnabled()

        binding.apply {
            myLocation.setOnClickListener {
                getMyLastLocation()
            }
            btnGallery.setOnClickListener {
                startGallery()
            }
            btnCamera.setOnClickListener {
                takePhoto()
            }
            btnUpload.setOnClickListener {
                addStory()
            }
            tvLonLat.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    setButtonUploadEnabled()
                }

                override fun afterTextChanged(p0: Editable?) {

                }

            })
            edtDesc.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        setButtonUploadEnabled()
                    }
                }

                override fun afterTextChanged(p0: Editable?) {

                }

            })
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = context?.let { uriToFile(selectedImg, it) }
            getFile = myFile
            binding.previewImageView.setImageURI(selectedImg)
        }
    }

    private val launcherIntentCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val myFile = File(currentPhotoPath)
                getFile = myFile
                val result = BitmapFactory.decodeFile(myFile.path)
                binding.previewImageView.setImageBitmap(result)
            }
        }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, resources.getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(requireActivity().packageManager)

        com.saadfauzi.storyapp.utils.createTempFile(requireActivity().application).also { file ->
            val photoURI: Uri = FileProvider.getUriForFile(
                requireActivity(),
                "com.saadfauzi.storyapp",
                file
            )
            currentPhotoPath = file.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun addStory() {
        binding.pbAddStory.visibility = View.VISIBLE
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)

            val description =
                binding.edtDesc.text.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            var token = ""

            viewModel.getAccessToken().observe(viewLifecycleOwner) {
                Log.d("AddNewFragment", it)
                token = it
            }

            viewModel.addStory(
                token,
                description,
                imageMultipart,
                lat,
                lon
            )
            viewModel.loginResult.observe(viewLifecycleOwner) { result ->
                if (result != null && result.error == false) {
                    findNavController().navigate(R.id.action_addNewStoryFragment_to_homeFragment)
                }
            }
        } else {
            binding.pbAddStory.visibility = View.GONE
            Toast.makeText(
                activity,
                resources.getString(R.string.error_no_image_selected),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                else -> {
                    Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.access_location_not_allowed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d("Location", "Lat: ${location.latitude} Lon: ${location.longitude}")
                    lat = location.latitude.toString().toRequestBody("text/plain".toMediaType())
                    lon = location.longitude.toString().toRequestBody("text/plain".toMediaType())
                    val currentLocation = "${location.latitude} ${location.longitude}"
                    viewModel.setLatLon(currentLocation)
                } else {
                    Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.no_location_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbAddStory.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnUpload.isEnabled = !isLoading
    }

    private fun setButtonUploadEnabled() {
        val desc = binding.edtDesc.text
        val loc = binding.tvLonLat.text
        binding.btnUpload.isEnabled =
            desc != null && getFile != null && loc != resources.getString(R.string.initial_null)
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}