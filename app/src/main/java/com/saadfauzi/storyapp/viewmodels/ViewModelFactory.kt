package com.saadfauzi.storyapp.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.saadfauzi.storyapp.injection.DependencyInjection
import com.saadfauzi.storyapp.utils.MySettingsPreference

class ViewModelFactory (private val context: Context, private val pref: MySettingsPreference): ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(DependencyInjection.provideRepository(context), pref) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel() as T
            }
            modelClass.isAssignableFrom(AddNewStoryViewModel::class.java) -> {
                AddNewStoryViewModel(pref) as T
            }
            modelClass.isAssignableFrom(MapsVersionViewModel::class.java) -> {
                MapsVersionViewModel(pref) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel Class " + modelClass.name)
        }
    }
}