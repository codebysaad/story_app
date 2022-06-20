package com.saadfauzi.storyapp.injection

import android.content.Context
import com.saadfauzi.storyapp.data.database.room.StoryDatabase
import com.saadfauzi.storyapp.data.repository.StoryRepository
import com.saadfauzi.storyapp.data.rest.ApiConfiguration

object DependencyInjection {
    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getInstance(context)
        val apiServices = ApiConfiguration.getApiService()
        return StoryRepository(database, apiServices)
    }
}