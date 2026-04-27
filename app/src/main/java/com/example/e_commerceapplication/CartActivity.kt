package com.example.e_commerceapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.e_commerceapplication.adapter.CartAdapter
import com.example.e_commerceapplication.databinding.ActivityCartBinding
import com.example.e_commerceapplication.model.CartManager
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        updateUI()

        binding.btnCheckout.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            items = CartManager.cartItems,
            onQuantityChanged = { updateUI() },
            onRemoveItem = { position ->
                val item = CartManager.cartItems[position]
                CartManager.removeItem(item.productId)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, CartManager.cartItems.size)
                updateUI()
            }
        )
        binding.rvCartItems.adapter = adapter
    }

    private fun updateUI() {
        if (CartManager.cartItems.isEmpty()) {
            binding.emptyCartLayout.visibility = View.VISIBLE
            binding.rvCartItems.visibility = View.GONE
            binding.cvBottom.visibility = View.GONE
        } else {
            binding.emptyCartLayout.visibility = View.GONE
            binding.rvCartItems.visibility = View.VISIBLE
            binding.cvBottom.visibility = View.VISIBLE
            binding.tvTotalAmount.text = "₹${String.format(Locale.getDefault(), "%.2f", CartManager.getTotalAmount())}"
        }
    }
}