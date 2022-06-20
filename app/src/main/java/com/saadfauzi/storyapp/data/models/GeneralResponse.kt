package com.saadfauzi.storyapp.data.models

import com.google.gson.annotations.SerializedName

data class GeneralResponse(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)
