package com.saadfauzi.storyapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.saadfauzi.storyapp.data.models.GeneralResponse
import com.saadfauzi.storyapp.data.rest.ApiConfiguration
import com.saadfauzi.storyapp.utils.Event
import com.saadfauzi.storyapp.utils.MySettingsPreference
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddNewStoryViewModel(private val pref: MySettingsPreference): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isMessage = MutableLiveData<Event<String?>>()
    val isMessage: LiveData<Event<String?>> = _isMessage

    private val _addNewStoryResult = MutableLiveData<GeneralResponse?>()
    val loginResult: LiveData<GeneralResponse?> = _addNewStoryResult

    private val _location = MutableLiveData<String>()
    val location: LiveData<String> = _location

    fun setLatLon(location: String){
        _location.value = location
    }

    fun getAccessToken(): LiveData<String> {
        return pref.getAccessToken().asLiveData()
    }

    fun addStory(token: String, description: RequestBody, imageMultipart: MultipartBody.Part, lat: RequestBody, lon: RequestBody){
        _isLoading.value = true
        val client = ApiConfiguration.getApiService().addNewStory(
            "Bearer $token",
            description,
            lat,
            lon,
            imageMultipart
        )
        client.enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                if (response.isSuccessful) {
                    _isLoading.value = false
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.error == false) {
                        _addNewStoryResult.value = responseBody
                        _isMessage.value = Event(responseBody.message)
                    } else if (responseBody != null && responseBody.error == true) {
                        _isMessage.value = Event(responseBody.message)
                        _addNewStoryResult.value = responseBody
                    }
                } else {
                    _isLoading.value = false
                    _isMessage.value = Event(response.message())
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                _isMessage.value = Event(t.message)
            }

        })
    }

}