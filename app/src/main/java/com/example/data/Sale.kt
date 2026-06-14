package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productBarcode: String,
    val productName: String,
    val category: String,
    val size: String,
    val quantity: Int,
    val sellingPrice: Double,
    val costPrice: Double,
    val totalPrice: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getProfit(): Double {
        return totalPrice - (costPrice * quantity)
    }
}
