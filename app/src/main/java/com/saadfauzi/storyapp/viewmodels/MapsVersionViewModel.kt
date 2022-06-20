package com.saadfauzi.storyapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.saadfauzi.storyapp.data.models.GetAllStoriesResponse
import com.saadfauzi.storyapp.data.rest.ApiConfiguration
import com.saadfauzi.storyapp.utils.Event
import com.saadfauzi.storyapp.utils.MySettingsPreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsVersionViewModel(private val pref: MySettingsPreference): ViewModel() {

    private val TAG = "MapVersionViewModel"

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isMessage = MutableLiveData<Event<String?>>()
    val isMessage: LiveData<Event<String?>> = _isMessage

    private val _listStories = MutableLiveData<GetAllStoriesResponse>()
    val listStories: LiveData<GetAllStoriesResponse> = _listStories

    fun getAccessToken(): LiveData<String> {
        return pref.getAccessToken().asLiveData()
    }

    fun getAllStories(token: String){
        _isLoading.value = true
        val client = ApiConfiguration.getApiService().getStoriesInMap("Bearer $token", "1")
        client.enqueue(object : Callback<GetAllStoriesResponse> {
            override fun onResponse(
                call: Call<GetAllStoriesResponse>,
                response: Response<GetAllStoriesResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful){
                    if (response.body()?.error == false){
                        _listStories.value = response.body()
                        _isMessage.value = Event(response.body()?.message.toString())
                        Log.d(TAG, response.body()?.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call<GetAllStoriesResponse>, t: Throwable) {
                _isLoading.value = false
                _isMessage.value = Event(t.message.toString())
                Log.e(TAG, t.message.toString())
            }

        })
    }
}