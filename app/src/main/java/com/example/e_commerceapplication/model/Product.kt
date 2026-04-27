package com.example.e_commerceapplication.model

data class Product(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val description: String = "",
    val imageUrl: String = "", // Keeping for backward compatibility or as main image
    val imageUrls: List<String> = emptyList(),
    val sellerId: String = "",
    val category: String = "All"
)