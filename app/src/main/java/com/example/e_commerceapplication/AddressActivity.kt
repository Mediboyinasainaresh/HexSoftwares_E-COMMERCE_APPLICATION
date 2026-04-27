package com.example.e_commerceapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_commerceapplication.databinding.ActivityAddressBinding
import com.example.e_commerceapplication.model.Address
import com.example.e_commerceapplication.model.AddressManager

class AddressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveAddress.setOnClickListener {
            val name = binding.etFullName.text.toString()
            val phone = binding.etPhone.text.toString()
            val addressLine = binding.etAddress.text.toString()
            val city = binding.etCity.text.toString()
            val pincode = binding.etPincode.text.toString()

            if (name.isEmpty() || phone.isEmpty() || addressLine.isEmpty() || city.isEmpty() || pincode.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                val address = Address(name, phone, addressLine, city, pincode)
                saveAddressToFirebase(address)
            }
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun saveAddressToFirebase(address: Address) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .collection("addresses").add(address)
            .addOnSuccessListener {
                AddressManager.selectedAddress = address
                Toast.makeText(this, "Address Saved Successfully! ✅", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}