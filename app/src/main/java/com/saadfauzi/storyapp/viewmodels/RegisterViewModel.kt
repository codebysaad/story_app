package com.saadfauzi.storyapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saadfauzi.storyapp.data.models.GeneralResponse
import com.saadfauzi.storyapp.data.rest.ApiConfiguration
import com.saadfauzi.storyapp.utils.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel: ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isMessage = MutableLiveData<Event<String?>>()
    val isMessage: LiveData<Event<String?>> = _isMessage

    private val _registerRes = MutableLiveData<GeneralResponse>()
    val registerRes: LiveData<GeneralResponse> = _registerRes

    fun registerNewUser(name: String, email: String, password: String){
        _isLoading.value = true
        val client = ApiConfiguration.getApiService().registerNewUser(name, email, password)
        client.enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful){
                    _registerRes.value = response.body()
                    _isMessage.value = Event(response.body()?.message.toString())
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                _isMessage.value = Event(t.message.toString())
            }
        })
    }
}