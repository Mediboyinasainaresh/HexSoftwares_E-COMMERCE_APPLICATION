package com.example.e_commerceapplication.model

object CartManager {

    val cartItems = mutableListOf<CartItem>()

    fun addItem(newItem: CartItem) {
        val existingItem = cartItems.find { it.productId == newItem.productId }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(newItem)
        }
    }

    fun removeItem(productId: String) {
        cartItems.removeAll { it.productId == productId }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        val item = cartItems.find { it.productId == productId }
        item?.quantity = quantity
    }

    fun getTotalAmount(): Double {
        return cartItems.sumOf {
            val p = it.price.replace("₹", "").replace(",", "").trim()
            (p.toDoubleOrNull() ?: 0.0) * it.quantity
        }
    }

    fun clearCart() {
        cartItems.clear()
    }
}