package com.saadfauzi.storyapp.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.saadfauzi.storyapp.data.models.LoginResponse
import com.saadfauzi.storyapp.data.rest.ApiConfiguration
import com.saadfauzi.storyapp.utils.Event
import com.saadfauzi.storyapp.utils.MySettingsPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val pref: MySettingsPreference) : ViewModel() {

    private val TAG = "LoginViewModel"

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isMessage = MutableLiveData<Event<String?>>()
    val isMessage: LiveData<Event<String?>> = _isMessage

    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: LiveData<LoginResponse?> = _loginResult

    fun getStateLogin(): LiveData<Boolean> {
        return pref.getLoginState().asLiveData()
    }

    fun saveAccessToken(isLogged: Boolean, token: String) {
        viewModelScope.launch {
            pref.saveAccessToken(isLogged, token)
        }
    }

    fun postLogin(email: String, password: String) {
        _isLoading.value = true
        val client = ApiConfiguration.getApiService().login(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        _loginResult.value = responseBody
                        _isMessage.value = Event(responseBody.message)
                        Log.d(TAG, responseBody.message)
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                _isMessage.value = Event(t.message)
                Log.e(TAG, t.message.toString())
            }

        })
    }
}