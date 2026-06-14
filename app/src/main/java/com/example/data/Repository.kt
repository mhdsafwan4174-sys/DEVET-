package com.example.data

import kotlinx.coroutines.flow.Flow

class Repository(private val appDao: AppDao) {

    val allProducts: Flow<List<Product>> = appDao.getAllProducts()
    val allSales: Flow<List<Sale>> = appDao.getAllSales()

    suspend fun getProductById(id: Int): Product? {
        return appDao.getProductById(id)
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        return appDao.getProductByBarcode(barcode)
    }

    suspend fun insertProduct(product: Product): Long {
        return appDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        appDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        appDao.deleteProduct(product)
    }

    suspend fun deleteProductById(id: Int) {
        appDao.deleteProductById(id)
    }

    suspend fun insertSale(sale: Sale): Long {
        return appDao.insertSale(sale)
    }

    suspend fun deleteSale(sale: Sale) {
        appDao.deleteSale(sale)
    }

    suspend fun clearAllSales() {
        appDao.clearAllSales()
    }
}
