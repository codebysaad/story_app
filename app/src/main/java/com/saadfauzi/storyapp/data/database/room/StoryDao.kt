package com.saadfauzi.storyapp.data.database.room

import androidx.paging.PagingSource
import androidx.room.*
import com.saadfauzi.storyapp.data.database.entities.StoryEntity

@Dao
interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStory(storyEntity: ArrayList<StoryEntity>)

    @Query("SELECT * FROM stories")
    fun getAllStories(): PagingSource<Int, StoryEntity>

    @Query("DELETE FROM stories")
    suspend fun deleteAll()
}