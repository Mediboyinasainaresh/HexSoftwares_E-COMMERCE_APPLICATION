package com.example.e_commerceapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_commerceapplication.adapter.CategoryAdapter
import com.example.e_commerceapplication.adapter.ProductAdapter
import com.example.e_commerceapplication.databinding.ActivityMainBinding
import com.example.e_commerceapplication.model.Product
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private val productList = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadCategories()
        fetchProducts()

        binding.etSearch.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty().lowercase()
                filterProducts(query)
                return true
            }
        })

        binding.fabSell.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.btnViewCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        updateCartPreview()
    }

    private fun loadCategories() {
        val list = mutableListOf<String>()
        list.add("All")

        db.collection("categories").get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                // If the collection exists but is empty, use defaults
                val defaults = listOf("All", "Electronics", "Fashion", "Home", "Accessories")
                setupCategoryUI(defaults)
            } else {
                for (doc in result) {
                    val name = doc.getString("name")
                    if (name != null) list.add(name)
                }
                setupCategoryUI(list)
            }
        }.addOnFailureListener {
            // If the collection fetch fails, use defaults
            val defaults = listOf("All", "Electronics", "Fashion", "Home", "Accessories")
            setupCategoryUI(defaults)
        }
    }

    private fun setupCategoryUI(categories: List<String>) {
        categoryAdapter = CategoryAdapter(categories) { selected ->
            filterByCategory(selected)
        }
        binding.rvCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = categoryAdapter
    }

    private fun filterByCategory(category: String) {
        android.util.Log.d("CATEGORY_FILTER", "Filtering by: $category")
        val filteredList = if (category.equals("All", ignoreCase = true)) {
            productList
        } else {
            productList.filter { it.category.equals(category, ignoreCase = true) }
        }
        android.util.Log.d("CATEGORY_FILTER", "Found ${filteredList.size} products")
        adapter.updateList(sortProducts(filteredList))
    }

    private fun sortProducts(list: List<Product>): List<Product> {
        return list.sortedWith(compareBy({ it.name.orEmpty().lowercase() }, { it.description.orEmpty().lowercase() }))
    }

    override fun onResume() {
        super.onResume()
        updateCartPreview()
    }

    private fun updateCartPreview() {
        val count = com.example.e_commerceapplication.model.CartManager.cartItems.sumOf { it.quantity }
        if (count > 0) {
            binding.cvCartPreview.visibility = View.VISIBLE
            binding.tvCartSummary.text = "$count item${if (count > 1) "s" else ""} in cart"
            binding.tvCartBadge.visibility = View.VISIBLE
            binding.tvCartBadge.text = count.toString()
        } else {
            binding.cvCartPreview.visibility = View.GONE
            binding.tvCartBadge.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(productList, { updateCartPreview() }) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_ID", product.id)
            startActivity(intent)
        }
        binding.rvProducts.layoutManager = GridLayoutManager(this, 2)
        binding.rvProducts.adapter = adapter
    }

    private fun fetchProducts() {
        binding.shimmerView.startShimmer()
        binding.shimmerView.visibility = View.VISIBLE
        binding.rvProducts.visibility = View.GONE

        db.collection("products").addSnapshotListener { value, error ->
            if (isFinishing) return@addSnapshotListener

            binding.shimmerView.stopShimmer()
            binding.shimmerView.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE

            if (error != null) {
                android.util.Log.e("FIREBASE", "Error fetching: ", error)
                Toast.makeText(this, "Error fetching products", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            productList.clear()
            if (value != null) {
                android.util.Log.d("FIREBASE", "Documents found: ${value.size()}")
                for (doc in value) {
                    val product = doc.toObject(Product::class.java)
                    productList.add(product.copy(id = doc.id))
                }
            }
            adapter.updateList(sortProducts(productList))
        }
    }

    private fun filterProducts(query: String) {
        val filteredList = if (query.isEmpty()) {
            productList.toList()
        } else {
            productList.filter {
                (it.name?.lowercase()?.contains(query) == true) ||
                        (it.description?.lowercase()?.contains(query) == true)
            }
        }
        adapter.updateList(sortProducts(filteredList))
    }
}