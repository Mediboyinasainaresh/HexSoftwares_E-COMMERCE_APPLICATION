package com.example.e_commerceapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.e_commerceapplication.databinding.ItemProductBinding
import android.widget.Toast
import android.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.example.e_commerceapplication.R
import com.example.e_commerceapplication.model.Product
import com.example.e_commerceapplication.model.CartItem
import com.example.e_commerceapplication.model.CartManager

class ProductAdapter(
    private var products: List<Product>,
    private val onCartUpdated: () -> Unit,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.binding.tvProductName.text = product.name
        holder.binding.tvProductPrice.text = "₹${product.price}"

        // Fetch and display real rating from Firebase
        FirebaseFirestore.getInstance()
            .collection("products")
            .document(product.id)
            .collection("reviews")
            .addSnapshotListener { value, _ ->
                val reviews = value?.documents?.mapNotNull { it.toObject(com.example.e_commerceapplication.model.Review::class.java) }
                if (!reviews.isNullOrEmpty()) {
                    val avg = reviews.map { it.rating }.average()
                    holder.binding.tvRating.text = "%.1f".format(avg)
                } else {
                    holder.binding.tvRating.text = "0.0"
                }
            }
        
        if (product.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .into(holder.binding.ivProductImage)
        }

        holder.binding.btnAddToCart.setOnClickListener {
            val item = CartItem(product.id, product.name, product.price, 1, product.imageUrl)
            CartManager.addItem(item)
            Toast.makeText(holder.itemView.context, "${product.name} added to cart! 🛒", Toast.LENGTH_SHORT).show()
            onCartUpdated()
        }

        holder.binding.ivWishlist.setOnClickListener {
            val isFavorite = it.tag as? Boolean ?: false
            val newFavorite = !isFavorite
            it.tag = newFavorite
            (it as android.widget.ImageView).setImageResource(
                if (newFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
            Toast.makeText(holder.itemView.context, if (newFavorite) "Added to Favorites ❤️" else "Removed from Favorites", Toast.LENGTH_SHORT).show()
        }

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(it.context)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete ${product.name}?")
                .setPositiveButton("Delete") { _, _ ->
                    FirebaseFirestore.getInstance().collection("products").document(product.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Product Deleted", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }

        holder.itemView.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateList(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }
}