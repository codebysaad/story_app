package com.saadfauzi.storyapp.data.rest

import com.saadfauzi.storyapp.data.models.GeneralResponse
import com.saadfauzi.storyapp.data.models.GetAllStoriesResponse
import com.saadfauzi.storyapp.data.models.LoginResponse
import com.saadfauzi.storyapp.utils.Helpers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiServices {
    @GET(Helpers.ADD_AND_GET_STORIES)
    suspend fun getAllStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): GetAllStoriesResponse

    @FormUrlEncoded
    @POST(Helpers.REGISTER)
    fun registerNewUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<GeneralResponse>

    @FormUrlEncoded
    @POST(Helpers.LOGIN)
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @Multipart
    @POST(Helpers.ADD_AND_GET_STORIES)
    fun addNewStory(
        @Header("Authorization") token: String,
        @Part("description") desc: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody,
        @Part photo: MultipartBody.Part,
    ): Call<GeneralResponse>

    @GET(Helpers.ADD_AND_GET_STORIES)
    fun getStoriesInMap(
        @Header("Authorization") token: String,
        @Query("location") location: String,
    ): Call<GetAllStoriesResponse>
}