package com.saadfauzi.storyapp.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.saadfauzi.storyapp.R
import com.saadfauzi.storyapp.adapters.AdapterStories
import com.saadfauzi.storyapp.adapters.LoadingStateAdapter
import com.saadfauzi.storyapp.databinding.FragmentHomeBinding
import com.saadfauzi.storyapp.utils.MySettingsPreference
import com.saadfauzi.storyapp.viewmodels.MainViewModel
import com.saadfauzi.storyapp.viewmodels.ViewModelFactory

private val Activity.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class HomeFragment : Fragment() {

    private val binding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = MySettingsPreference.getInstance(requireActivity().dataStore)
        viewModel = ViewModelProvider(this, ViewModelFactory(requireContext(), pref))[
                MainViewModel::class.java
        ]

        binding.rvListStory.layoutManager = LinearLayoutManager(requireActivity())

        viewModel.getAccessToken().observe(viewLifecycleOwner) {
            Log.d("HomeFragment", it)
            showRecyclerView(it)
        }

        binding.btnAddNewStory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addNewStoryFragment)
        }
    }

    private fun showRecyclerView(token: String) {
        val adapterStories = AdapterStories()
        binding.rvListStory.adapter = adapterStories.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapterStories.retry()
            }
        )
        viewModel.getStories(token).observe(viewLifecycleOwner) { data ->
            adapterStories.submitData(lifecycle, data)
        }
    }
}