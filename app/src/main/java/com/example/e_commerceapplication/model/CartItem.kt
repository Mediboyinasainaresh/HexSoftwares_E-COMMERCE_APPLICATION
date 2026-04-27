package com.example.e_commerceapplication.model

data class CartItem(
    val productId: String = "",
    val name: String = "",
    val price: String = "",
    var quantity: Int = 1,
    val imageUrl: String = ""
)