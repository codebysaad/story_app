package com.saadfauzi.storyapp.adapters

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saadfauzi.storyapp.R
import com.saadfauzi.storyapp.data.database.entities.StoryEntity
import com.saadfauzi.storyapp.databinding.ItemListStoryBinding
import com.saadfauzi.storyapp.ui.DetailActivity
import com.saadfauzi.storyapp.utils.dateFormatter

class AdapterStories: PagingDataAdapter<StoryEntity, AdapterStories.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null){
            holder.bind(data)
            Log.d("AdapterStories", data.name)
        }
    }

    class ViewHolder(var binding: ItemListStoryBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: StoryEntity){
            binding.tvName.text = data.name
            binding.tvCreatedAt.text = dateFormatter(data.createdAt)
            binding.tvDescStory.text = data.description
            Glide.with(itemView.context)
                .load(data.photoUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_place_holder)
                .into(binding.photoStory)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_DATA, data)
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.photoStory, "photo"),
                        Pair(binding.tvName, "name"),
                        Pair(binding.tvDescStory, "description"),
                        Pair(binding.tvCreatedAt, "date")
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}