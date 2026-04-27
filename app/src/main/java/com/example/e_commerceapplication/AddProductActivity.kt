package com.example.e_commerceapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_commerceapplication.databinding.ActivityAddProductBinding
import com.example.e_commerceapplication.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupCategorySpinner()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSubmitProduct.setOnClickListener {
            val name = binding.etProductName.text.toString().trim()
            val price = binding.etProductPrice.text.toString().trim()
            val description = binding.etProductDescription.text.toString().trim()
            val imageUrl = binding.etImageUrl.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem.toString()

            if (name.isNotEmpty() && price.isNotEmpty() && description.isNotEmpty() && imageUrl.isNotEmpty()) {
                val sellerId = auth.currentUser?.uid ?: ""
                
                val product = Product("", name, price, description, imageUrl, emptyList(), sellerId, category)

                db.collection("products").add(product)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Product Posted Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCategorySpinner() {
        val categories = mutableListOf<String>()
        
        db.collection("categories").get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                categories.addAll(listOf("Electronics", "Fashion", "Home", "Accessories", "Other"))
            } else {
                for (doc in result) {
                    val name = doc.getString("name")
                    if (name != null) categories.add(name)
                }
            }
            val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }.addOnFailureListener {
            categories.addAll(listOf("Electronics", "Fashion", "Home", "Accessories", "Other"))
            val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}