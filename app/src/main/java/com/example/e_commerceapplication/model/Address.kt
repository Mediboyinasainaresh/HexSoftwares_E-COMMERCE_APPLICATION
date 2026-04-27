package com.example.e_commerceapplication.model

data class Address(
    val name: String = "",
    val phone: String = "",
    val addressLine: String = "",
    val city: String = "",
    val pincode: String = ""
)

object AddressManager {
    var selectedAddress: Address? = null
}