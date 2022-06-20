package com.saadfauzi.storyapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.saadfauzi.storyapp.R
import com.saadfauzi.storyapp.data.database.entities.StoryEntity
import com.saadfauzi.storyapp.databinding.ActivityDetailBinding
import com.saadfauzi.storyapp.utils.dateFormatter

class DetailActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val data: StoryEntity = intent.getParcelableExtra<StoryEntity>(EXTRA_DATA) as StoryEntity

        binding.apply {
            tvName.text = data.name
            tvDescStory.text = data.description
            tvCreatedAt.text = dateFormatter(data.createdAt)
            Glide.with(this@DetailActivity)
                .load(data.photoUrl)
                .placeholder(R.drawable.ic_place_holder)
                .into(photoStory)
        }

        Log.d("DetailActivity", "Name: ${data.name} Created at: ${data.createdAt}")
    }

    companion object {
        const val EXTRA_DATA = "detail_story"
    }
}