package com.saadfauzi.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.saadfauzi.storyapp.data.database.entities.StoryEntity
import com.saadfauzi.storyapp.data.database.room.StoryDatabase
import com.saadfauzi.storyapp.data.paging.StoryRemoteMediator
import com.saadfauzi.storyapp.data.rest.ApiServices

class StoryRepository(
    private val database: StoryDatabase,
    private val apiServices: ApiServices,
) {
    fun getAllStories(token: String): LiveData<PagingData<StoryEntity>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 10
            ),
            remoteMediator = StoryRemoteMediator(token, database, apiServices),
            pagingSourceFactory = {
                database.storyDao().getAllStories()
            }
        ).liveData
    }
}