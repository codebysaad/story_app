package com.saadfauzi.storyapp.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MySettingsPreference private constructor(private val datastore: DataStore<Preferences>) {

    private val STATE_LOGGED = booleanPreferencesKey("is_logged")
    private val ACCESS_TOKEN = stringPreferencesKey("token")

    fun getAccessToken(): Flow<String> {
        return datastore.data.map { preferences ->
            preferences[ACCESS_TOKEN] ?: "No auth"
        }
    }

    fun getLoginState(): Flow<Boolean> {
        return datastore.data.map { preferences ->
            preferences[STATE_LOGGED] ?: false
        }
    }

    suspend fun saveAccessToken(isLogged: Boolean, token: String){
        datastore.edit { preferences ->
            preferences[STATE_LOGGED] = isLogged
            preferences[ACCESS_TOKEN] = token
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MySettingsPreference? = null

        fun getInstance(datastore: DataStore<Preferences>): MySettingsPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = MySettingsPreference(datastore)
                INSTANCE = instance
                return instance
            }
        }
    }
}