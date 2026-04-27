package com.example.e_commerceapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.e_commerceapplication.databinding.ItemOrderBinding
import com.example.e_commerceapplication.model.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(private val orders: List<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.tvOrderId.text = "Order #${order.orderId.takeLast(6).uppercase()}"
        holder.binding.tvOrderStatus.text = order.status
        
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        holder.binding.tvOrderDate.text = sdf.format(Date(order.timestamp))
        
        val itemsSummary = order.items.joinToString(", ") { it.name }
        holder.binding.tvOrderItems.text = itemsSummary
        
        holder.binding.tvOrderTotal.text = "₹${String.format(Locale.getDefault(), "%.2f", order.totalAmount)}"
    }

    override fun getItemCount(): Int = orders.size
}