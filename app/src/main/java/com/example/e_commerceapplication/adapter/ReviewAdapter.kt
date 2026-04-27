package com.example.e_commerceapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.e_commerceapplication.databinding.ItemReviewBinding
import com.example.e_commerceapplication.model.Review

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    private var reviews = listOf<Review>()

    inner class ViewHolder(val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviews[position]

        holder.binding.tvUser.text = review.userName
        holder.binding.tvComment.text = review.comment
        holder.binding.ratingBar.rating = review.rating.toFloat()
    }

    override fun getItemCount() = reviews.size

    fun submitList(list: List<Review>) {
        reviews = list
        notifyDataSetChanged()
    }
}
