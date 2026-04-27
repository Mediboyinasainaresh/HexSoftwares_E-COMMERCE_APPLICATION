package com.example.e_commerceapplication.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(
    private val images: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    inner class ViewHolder(val imageView: ImageView) :
        RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val iv = ImageView(parent.context)
        val size = (80 * parent.context.resources.displayMetrics.density).toInt()
        val margin = (8 * parent.context.resources.displayMetrics.density).toInt()
        val layoutParams = ViewGroup.MarginLayoutParams(size, size)
        layoutParams.setMargins(0, 0, margin, 0)
        iv.layoutParams = layoutParams
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        return ViewHolder(iv)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = images[position]

        Glide.with(holder.itemView.context)
            .load(url)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            onClick(url)
        }
    }

    override fun getItemCount() = images.size
}