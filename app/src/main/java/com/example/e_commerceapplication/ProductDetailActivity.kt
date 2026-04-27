package com.example.e_commerceapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.e_commerceapplication.adapter.ImageAdapter
import com.example.e_commerceapplication.adapter.ReviewAdapter
import com.example.e_commerceapplication.databinding.ActivityProductDetailBinding
import com.example.e_commerceapplication.model.CartItem
import com.example.e_commerceapplication.model.CartManager
import com.example.e_commerceapplication.model.Product
import com.example.e_commerceapplication.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var reviewAdapter: ReviewAdapter
    private var currentProduct: Product? = null
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        val productId = intent.getStringExtra("PRODUCT_ID") ?: ""

        if (productId.isNotEmpty()) {
            fetchProductDetails(productId)
            setupReviewSystem(productId)
        }

        binding.btnBackDetail.setOnClickListener {
            finish()
        }

        binding.btnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            if (isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.btnAddToCart.setOnClickListener {
            currentProduct?.let { product ->
                val cartItem = CartItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    imageUrl = product.imageUrl,
                    quantity = 1
                )
                CartManager.addItem(cartItem)
                updateCartBadge()
                Toast.makeText(this, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBuyNow.setOnClickListener {
            currentProduct?.let {
                val intent = Intent(this, CheckoutActivity::class.java)
                intent.putExtra("PRODUCT_NAME", it.name)
                intent.putExtra("PRODUCT_PRICE", it.price)
                startActivity(intent)
            }
        }

        updateCartBadge()
    }

    private fun updateCartBadge() {
        val count = CartManager.cartItems.sumOf { it.quantity }
        if (count > 0) {
            binding.tvCartBadge.visibility = android.view.View.VISIBLE
            binding.tvCartBadge.text = count.toString()
        } else {
            binding.tvCartBadge.visibility = android.view.View.GONE
        }
    }

    private fun fetchProductDetails(productId: String) {
        db.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val product = document.toObject(Product::class.java)
                    currentProduct = product
                    product?.let {
                        binding.tvProductName.text = it.name
                        binding.tvProductPrice.text = "₹${it.price}"
                        binding.tvProductDescription.text = it.description
                        
                        // Setup Image Gallery
                        val images = mutableListOf<String>()
                        if (it.imageUrl.isNotEmpty()) images.add(it.imageUrl)
                        images.addAll(it.imageUrls)

                        if (images.isNotEmpty()) {
                            // Set first image
                            Glide.with(this).load(images[0]).into(binding.ivProductImage)

                            // Setup thumbnails
                            binding.rvImages.layoutManager =
                                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                            binding.rvImages.adapter = ImageAdapter(images) { selectedImage ->
                                Glide.with(this)
                                    .load(selectedImage)
                                    .placeholder(android.R.drawable.ic_menu_report_image)
                                    .into(binding.ivProductImage)
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading product details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupReviewSystem(productId: String) {
        reviewAdapter = ReviewAdapter()
        binding.rvReviews.layoutManager = LinearLayoutManager(this)
        binding.rvReviews.adapter = reviewAdapter

        loadReviews(productId)

        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.ratingBar.rating.toInt()
            val comment = binding.etReview.text.toString().trim()

            if (comment.isEmpty()) {
                Toast.makeText(this, "Write review first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rating == 0) {
                Toast.makeText(this, "Please give a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            val review = Review(
                userId = user?.uid ?: "guest",
                userName = user?.displayName ?: user?.email?.substringBefore("@") ?: "Anonymous",
                rating = rating,
                comment = comment
            )

            db.collection("products")
                .document(productId)
                .collection("reviews")
                .add(review)
                .addOnSuccessListener {
                    Toast.makeText(this, "Review added ⭐", Toast.LENGTH_SHORT).show()
                    binding.etReview.text.clear()
                    binding.ratingBar.rating = 0f
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add review", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadReviews(productId: String) {
        db.collection("products")
            .document(productId)
            .collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val list = mutableListOf<Review>()
                value?.documents?.forEach {
                    val review = it.toObject(Review::class.java)
                    if (review != null) list.add(review)
                }

                reviewAdapter.submitList(list)

                if (list.isNotEmpty()) {
                    val avg = list.map { it.rating }.average()
                    binding.tvRating.text = "⭐ %.1f".format(avg)
                    binding.tvReviewCount.text = "(${list.size} reviews)"
                } else {
                    binding.tvRating.text = "⭐ 0.0"
                    binding.tvReviewCount.text = "(0 reviews)"
                }
            }
    }
}