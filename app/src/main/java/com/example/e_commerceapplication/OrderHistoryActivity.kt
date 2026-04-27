package com.example.e_commerceapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_commerceapplication.adapter.OrderAdapter
import com.example.e_commerceapplication.databinding.ActivityOrderHistoryBinding
import com.example.e_commerceapplication.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val orderList = mutableListOf<Order>()
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        fetchOrders()

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter(orderList)
        binding.rvOrderHistory.adapter = adapter
    }

    private fun fetchOrders() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error fetching orders", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                orderList.clear()
                if (value != null) {
                    for (doc in value) {
                        val order = doc.toObject(Order::class.java)
                        if (order != null) {
                            orderList.add(order.copy(orderId = doc.id))
                        }
                    }
                }

                if (orderList.isEmpty()) {
                    binding.emptyOrdersLayout.visibility = View.VISIBLE
                    binding.rvOrderHistory.visibility = View.GONE
                } else {
                    binding.emptyOrdersLayout.visibility = View.GONE
                    binding.rvOrderHistory.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
    }
}