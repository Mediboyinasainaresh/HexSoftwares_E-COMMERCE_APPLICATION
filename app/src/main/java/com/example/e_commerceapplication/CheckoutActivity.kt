package com.example.e_commerceapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_commerceapplication.databinding.ActivityCheckoutBinding
import com.example.e_commerceapplication.model.AddressManager
import com.example.e_commerceapplication.model.CartManager
import com.example.e_commerceapplication.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.util.Locale

class CheckoutActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivityCheckoutBinding
    private var totalAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        totalAmount = CartManager.getTotalAmount()
        updateAddressUI()
        updateOrderSummary()

        binding.cvAddress.setOnClickListener {
            startActivity(Intent(this, AddressActivity::class.java))
        }

        binding.btnPlaceOrder.setOnClickListener {
            if (AddressManager.selectedAddress == null) {
                Toast.makeText(this, "Add address first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (binding.rbCod.isChecked) {
                saveOrder(null, "COD", "PLACED")
            } else if (binding.rbOnline.isChecked) {
                startPayment()
            }
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        Checkout.preload(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        updateAddressUI()
    }

    private fun updateAddressUI() {
        val address = AddressManager.selectedAddress
        if (address != null) {
            binding.tvAddressName.text = address.name
            binding.tvAddressDetails.text = "${address.addressLine}, ${address.city} - ${address.pincode}\nPhone: ${address.phone}"
        }
    }

    private fun updateOrderSummary() {
        binding.tvCheckoutProductPrice.text = "Total: ₹${String.format(Locale.getDefault(), "%.2f", totalAmount)}"
        val productNames = CartManager.cartItems.joinToString(", ") { it.name }
        binding.tvCheckoutProductName.text = productNames
    }

    private fun startPayment() {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_SgxHxOInNXy0Tk")

        try {
            val options = JSONObject()
            options.put("name", "TradeNest")
            options.put("description", "Order Payment")
            options.put("theme.color", "#6200EE")
            options.put("currency", "INR")
            options.put("amount", (totalAmount * 100).toInt())

            val prefill = JSONObject()
            prefill.put("email", FirebaseAuth.getInstance().currentUser?.email ?: "user@test.com")
            prefill.put("contact", AddressManager.selectedAddress?.phone)
            options.put("prefill", prefill)

            checkout.open(this, options)

        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        Toast.makeText(this, "Payment Successful ✅", Toast.LENGTH_SHORT).show()
        saveOrder(paymentId, "ONLINE", "PAID")
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed ❌", Toast.LENGTH_SHORT).show()
    }

    private fun saveOrder(paymentId: String?, method: String, status: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "demoUser"
        val db = FirebaseFirestore.getInstance()
        val orderRef = db.collection("orders").document()
        val orderId = orderRef.id

        val order = Order(
            orderId = orderId,
            userId = userId,
            items = CartManager.cartItems.toList(),
            totalAmount = totalAmount,
            address = AddressManager.selectedAddress,
            paymentId = paymentId,
            paymentMethod = method,
            status = status,
            timestamp = System.currentTimeMillis()
        )

        orderRef.set(order)
            .addOnSuccessListener {
                CartManager.cartItems.clear()
                val intent = Intent(this, OrderSuccessActivity::class.java)
                intent.putExtra("ORDER_ID", orderId)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to place order: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}