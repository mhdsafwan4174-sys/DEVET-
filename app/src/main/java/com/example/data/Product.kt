package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Shoes, Chappals, Sandals, Slippers, Accessories
    val brand: String,
    val supplier: String,
    val description: String,
    val barcode: String,
    val imagePaths: String = "", // Comma-separated list of image URIs or placeholder identifiers
    val sellingPrice: Double,
    val costPrice: Double = 0.0,
    val sizesString: String = "" // String format: "39:3,40:5,41:4"
) {
    // Helper to get helper map of Size -> Quantity
    fun getSizesMap(): Map<String, Int> {
        if (sizesString.isBlank()) return emptyMap()
        return try {
            sizesString.split(",")
                .filter { it.contains(":") }
                .associate {
                    val parts = it.split(":")
                    parts[0].trim() to (parts[1].trim().toIntOrNull() ?: 0)
                }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // Helper to calculate total stock quantity across all sizes
    fun getTotalStock(): Int {
        return getSizesMap().values.sum()
    }

    companion object {
        fun createSizesString(sizesMap: Map<String, Int>): String {
            return sizesMap.filter { it.key.isNotBlank() }
                .map { "${it.key.trim()}:${it.value}" }
                .joinToString(",")
        }
    }
}
