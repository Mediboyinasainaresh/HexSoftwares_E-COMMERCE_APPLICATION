package com.example.e_commerceapplication.model

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<CartItem> = listOf(),
    val totalAmount: Double = 0.0,
    val address: Address? = null,
    val paymentId: String? = "",
    val paymentMethod: String = "",
    val status: String = "",
    val timestamp: Long = System.currentTimeMillis()
)