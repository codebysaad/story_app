package com.saadfauzi.storyapp.viewmodels

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.saadfauzi.storyapp.data.database.entities.StoryEntity
import com.saadfauzi.storyapp.data.repository.StoryRepository
import com.saadfauzi.storyapp.utils.MySettingsPreference
import kotlinx.coroutines.launch

class MainViewModel(private val storyRepository: StoryRepository, private val pref: MySettingsPreference): ViewModel() {

    fun getAccessToken(): LiveData<String> {
        return pref.getAccessToken().asLiveData()
    }

    fun saveAccessToken(isLogged: Boolean, token: String) {
        viewModelScope.launch {
            pref.saveAccessToken(isLogged, token)
        }
    }

    fun getStories(token: String): LiveData<PagingData<StoryEntity>> = storyRepository.getAllStories(token).cachedIn(viewModelScope)
}