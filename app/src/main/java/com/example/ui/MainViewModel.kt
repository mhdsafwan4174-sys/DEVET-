package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Product
import com.example.data.Repository
import com.example.data.Sale
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    Dashboard,
    Inventory,
    ProductForm,
    POS,
    Reports,
    Settings
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: Repository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = Repository(database.appDao())
        
        // Seed database if empty
        viewModelScope.launch {
            repository.allProducts.first().let { items ->
                if (items.isEmpty()) {
                    seedData()
                }
            }
        }
    }

    // --- Core States ---
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(AppScreen.Dashboard)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _isAdmin = MutableStateFlow(true)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    // --- Search & Filter States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedStockStatus = MutableStateFlow("All") // All, In Stock, Low Stock, Out of Stock
    val selectedStockStatus: StateFlow<String> = _selectedStockStatus.asStateFlow()

    private val _selectedSizeFilter = MutableStateFlow("All")
    val selectedSizeFilter: StateFlow<String> = _selectedSizeFilter.asStateFlow()

    // --- Form UI States ---
    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    // --- POS Cart State ---
    // Maps: Product Barcode -> Size -> Quantity
    private val _cart = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())
    val cart: StateFlow<Map<String, Map<String, Int>>> = _cart.asStateFlow()

    // --- Active Scanned Barcode (Simulation) ---
    private val _scannedBarcode = MutableStateFlow<String?>(null)
    val scannedBarcode: StateFlow<String?> = _scannedBarcode.asStateFlow()

    // --- Notification state ---
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // --- Stock Limits ---
    val LOW_STOCK_THRESHOLD = 5

    // --- Actions ---
    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun toggleRole() {
        _isAdmin.value = !_isAdmin.value
        showStatus("Role changed to ${if (_isAdmin.value) "Admin" else "Staff"}")
    }

    fun setAdmin(admin: Boolean) {
        _isAdmin.value = admin
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun updateStockStatusFilter(status: String) {
        _selectedStockStatus.value = status
    }

    fun updateSizeFilter(size: String) {
        _selectedSizeFilter.value = size
    }

    fun startAddProduct() {
        _editingProduct.value = null
        _currentScreen.value = AppScreen.ProductForm
    }

    fun startEditProduct(product: Product) {
        _editingProduct.value = product
        _currentScreen.value = AppScreen.ProductForm
    }

    fun duplicateProduct(product: Product) {
        viewModelScope.launch {
            val duplicate = Product(
                name = "${product.name} (Copy)",
                category = product.category,
                brand = product.brand,
                supplier = product.supplier,
                description = product.description,
                barcode = generateUniqueBarcode(),
                imagePaths = product.imagePaths,
                sellingPrice = product.sellingPrice,
                costPrice = product.costPrice,
                sizesString = product.sizesString
            )
            repository.insertProduct(duplicate)
            showStatus("Duplicated Product: ${product.name}")
        }
    }

    fun saveProduct(
        name: String,
        category: String,
        brand: String,
        supplier: String,
        description: String,
        barcode: String,
        imagePaths: String,
        sellingPrice: Double,
        costPrice: Double,
        sizesMap: Map<String, Int>
    ) {
        viewModelScope.launch {
            val sizesStr = Product.createSizesString(sizesMap)
            val current = _editingProduct.value
            if (current == null) {
                // New product
                // Check unique barcode helper
                val check = repository.getProductByBarcode(barcode)
                val finalBarcode = if (check != null && barcode.isNotBlank()) {
                    generateUniqueBarcode()
                } else if (barcode.isBlank()) {
                    generateUniqueBarcode()
                } else {
                    barcode
                }

                val product = Product(
                    name = name,
                    category = category,
                    brand = brand,
                    supplier = supplier,
                    description = description,
                    barcode = finalBarcode,
                    imagePaths = imagePaths,
                    sellingPrice = sellingPrice,
                    costPrice = costPrice,
                    sizesString = sizesStr
                )
                repository.insertProduct(product)
                showStatus("Added: $name")
            } else {
                // Edit existing
                val product = current.copy(
                    name = name,
                    category = category,
                    brand = brand,
                    supplier = supplier,
                    description = description,
                    barcode = barcode.ifBlank { current.barcode },
                    imagePaths = imagePaths,
                    sellingPrice = sellingPrice,
                    costPrice = costPrice,
                    sizesString = sizesStr
                )
                repository.updateProduct(product)
                showStatus("Updated: $name")
            }
            _currentScreen.value = AppScreen.Inventory
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showStatus("Deleted: ${product.name}")
        }
    }

    fun adjustStockForDamagedOrReturned(product: Product, size: String, changeAmount: Int, isDamaged: Boolean) {
        viewModelScope.launch {
            val sizesMap = product.getSizesMap().toMutableMap()
            val currentQty = sizesMap[size] ?: 0
            val newQty = (currentQty + changeAmount).coerceAtLeast(0)
            sizesMap[size] = newQty
            val updated = product.copy(sizesString = Product.createSizesString(sizesMap))
            repository.updateProduct(updated)
            
            val typeStr = if (isDamaged) "Damage Adjustment" else "Return Adjustment"
            showStatus("$typeStr: ${product.name} (Size: $size) set to $newQty")
        }
    }

    // --- POS Cart Operations ---
    fun addToCart(product: Product, size: String, qty: Int = 1) {
        val currentCart = _cart.value.toMutableMap()
        val productBarcode = product.barcode
        val sizeMap = currentCart[productBarcode]?.toMutableMap() ?: mutableMapOf()
        
        val stockAvailable = product.getSizesMap()[size] ?: 0
        val currentInCart = sizeMap[size] ?: 0
        
        if (currentInCart + qty <= stockAvailable) {
            sizeMap[size] = currentInCart + qty
            currentCart[productBarcode] = sizeMap
            _cart.value = currentCart
            showStatus("Added ${product.name} (Size $size) to Cart")
        } else {
            showStatus("Error: Insufficient stock for ${product.name} size $size!")
        }
    }

    fun updateCartQty(productBarcode: String, size: String, newQty: Int, maxStock: Int) {
        val currentCart = _cart.value.toMutableMap()
        val sizeMap = currentCart[productBarcode]?.toMutableMap() ?: return
        
        if (newQty <= 0) {
            sizeMap.remove(size)
            if (sizeMap.isEmpty()) {
                currentCart.remove(productBarcode)
            } else {
                currentCart[productBarcode] = sizeMap
            }
        } else if (newQty <= maxStock) {
            sizeMap[size] = newQty
            currentCart[productBarcode] = sizeMap
        } else {
            showStatus("Cannot add more than stock limit ($maxStock)")
            return
        }
        _cart.value = currentCart
    }

    fun removeFromCart(productBarcode: String, size: String) {
        val currentCart = _cart.value.toMutableMap()
        val sizeMap = currentCart[productBarcode]?.toMutableMap() ?: return
        sizeMap.remove(size)
        if (sizeMap.isEmpty()) {
            currentCart.remove(productBarcode)
        } else {
            currentCart[productBarcode] = sizeMap
        }
        _cart.value = currentCart
        showStatus("Removed from Cart")
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun checkout(productsList: List<Product>): Boolean {
        if (_cart.value.isEmpty()) return false
        
        viewModelScope.launch {
            var allSuccessful = true
            val salesToRecord = mutableListOf<Sale>()
            val productsToUpdate = mutableListOf<Product>()
            
            for ((barcode, sizeMap) in _cart.value) {
                val dbProduct = productsList.find { it.barcode == barcode } ?: continue
                val dbSizesMap = dbProduct.getSizesMap().toMutableMap()
                
                for ((size, orderQty) in sizeMap) {
                    val availableQty = dbSizesMap[size] ?: 0
                    if (availableQty >= orderQty) {
                        dbSizesMap[size] = availableQty - orderQty
                        
                        salesToRecord.add(
                            Sale(
                                productBarcode = barcode,
                                productName = dbProduct.name,
                                category = dbProduct.category,
                                size = size,
                                quantity = orderQty,
                                sellingPrice = dbProduct.sellingPrice,
                                costPrice = dbProduct.costPrice,
                                totalPrice = dbProduct.sellingPrice * orderQty
                            )
                        )
                    } else {
                        allSuccessful = false
                    }
                }
                
                productsToUpdate.add(dbProduct.copy(sizesString = Product.createSizesString(dbSizesMap)))
            }
            
            if (allSuccessful) {
                // Perform transactions
                productsToUpdate.forEach { repository.updateProduct(it) }
                salesToRecord.forEach { repository.insertSale(it) }
                clearCart()
                showStatus("Sale successful! Receipt printed.")
            } else {
                showStatus("Error checkout: Stock levels checked during processing have changed.")
            }
        }
        return true
    }

    // --- Barcode Actions ---
    fun scanBarcodeSimulated(barcode: String, productsList: List<Product>) {
        _scannedBarcode.value = barcode
        val product = productsList.find { it.barcode == barcode }
        if (product != null) {
            showStatus("Barcode matched: ${product.name}")
        } else {
            showStatus("Barcode $barcode not found in local stock.")
        }
    }

    fun clearScannedBarcode() {
        _scannedBarcode.value = null
    }

    fun generateUniqueBarcode(): String {
        val chars = "0123456789"
        return "DE-" + (1..6).map { chars.random() }.joinToString("")
    }

    // --- Seeding ---
    private suspend fun seedData() {
        val seedProducts = listOf(
            Product(
                name = "DEVET Elite Air Runner",
                category = "Shoes",
                brand = "DEVET",
                supplier = "Prime Shoes Ltd.",
                description = "Modern flagship breathable sport running shoe with deep shock absorption and responsive midsole.",
                barcode = "DE1001",
                imagePaths = "shoe_front,shoe_side,shoe_sole",
                sellingPrice = 119.99,
                costPrice = 65.00,
                sizesString = "39:5,40:10,41:15,42:8,43:4,44:2"
            ),
            Product(
                name = "Velas Comfort Sandal",
                category = "Sandals",
                brand = "Velas",
                supplier = "Ocean Distribution",
                description = "Genuine brown leather straps with an ergonomic orthopedic cork footbed for style and supreme comfort.",
                barcode = "DE1002",
                imagePaths = "sandal_front,sandal_side",
                sellingPrice = 49.99,
                costPrice = 22.00,
                sizesString = "38:4,39:10,40:0,41:8,42:1" // Size 40 is Out of Stock
            ),
            Product(
                name = "Aero-Lite Cloud Slipper",
                category = "Slippers",
                brand = "Apex",
                supplier = "Metro Footwear",
                description = "Waterproof indoor/outdoor summer essential slipper. Extra cushion visual structure.",
                barcode = "DE1003",
                imagePaths = "slipper_top,slipper_side",
                sellingPrice = 19.95,
                costPrice = 8.50,
                sizesString = "40:2,41:3,42:1,43:0" // Low stock values
            ),
            Product(
                name = "Safari Soft-Step Chappal",
                category = "Chappals",
                brand = "WalkEase",
                supplier = "WalkEase Wholesalers",
                description = "Semi-casual daily slip-on perfect for warm climates. Traditional stitched leather trim.",
                barcode = "DE1004",
                imagePaths = "chappal_top",
                sellingPrice = 29.99,
                costPrice = 14.00,
                sizesString = "39:12,40:15,41:3"
            ),
            Product(
                name = "Orthopedic Gel Insoles",
                category = "Accessories",
                brand = "ProFoot",
                supplier = "Supplies Express",
                description = "Deep massaging blue gel foot active arch insoles for fatigue prevention during extensive long shifts.",
                barcode = "DE1005",
                imagePaths = "accessory_insoles",
                sellingPrice = 15.00,
                costPrice = 6.00,
                sizesString = "One Size:28"
            )
        )

        seedProducts.forEach { repository.insertProduct(it) }

        // Seed some representative transactions across the past days
        val pastDaysSales = listOf(
            Sale(productBarcode = "DE1001", productName = "DEVET Elite Air Runner", category = "Shoes", size = "41", quantity = 2, sellingPrice = 119.99, costPrice = 65.00, totalPrice = 239.98, timestamp = System.currentTimeMillis() - 3600000 * 2),
            Sale(productBarcode = "DE1002", productName = "Velas Comfort Sandal", category = "Sandals", size = "39", quantity = 1, sellingPrice = 49.99, costPrice = 22.00, totalPrice = 49.99, timestamp = System.currentTimeMillis() - 3600000 * 5),
            Sale(productBarcode = "DE1004", productName = "Safari Soft-Step Chappal", category = "Chappals", size = "40", quantity = 3, sellingPrice = 29.99, costPrice = 14.00, totalPrice = 89.97, timestamp = System.currentTimeMillis() - 3600000 * 25), // Yesterday
            Sale(productBarcode = "DE1001", productName = "DEVET Elite Air Runner", category = "Shoes", size = "42", quantity = 1, sellingPrice = 119.99, costPrice = 65.00, totalPrice = 119.99, timestamp = System.currentTimeMillis() - 3600000 * 50)  // Three days ago
        )
        pastDaysSales.forEach { repository.insertSale(it) }
    }

    // --- Backup & Data Management (JSON-CSV Exchange) ---
    fun exportToCSVString(productsList: List<Product>): String {
        val sb = StringBuilder()
        sb.append("ID,Name,Category,Brand,Supplier,Selling Price,Cost Price,Barcode,Stock String,Description\n")
        productsList.forEach { p ->
            val cleanName = p.name.replace(",", ";")
            val cleanBrand = p.brand.replace(",", ";")
            val cleanSupplier = p.supplier.replace(",", ";")
            val cleanDesc = p.description.replace(",", ";").replace("\n", " ")
            sb.append("${p.id},\"$cleanName\",${p.category},\"$cleanBrand\",\"$cleanSupplier\",${p.sellingPrice},${p.costPrice},${p.barcode},\"${p.sizesString}\",\"$cleanDesc\"\n")
        }
        return sb.toString()
    }

    fun importFromCSVString(csvContent: String) {
        viewModelScope.launch {
            try {
                val lines = csvContent.lines()
                if (lines.size < 2) return@launch
                
                var importedCount = 0
                for (i in 1 until lines.size) {
                    val line = lines[i].trim()
                    if (line.isEmpty()) continue
                    
                    val parts = parseCsvLine(line)
                    if (parts.size >= 9) {
                        val name = parts[1].replace(";", ",")
                        val category = parts[2]
                        val brand = parts[3].replace(";", ",")
                        val supplier = parts[4].replace(";", ",")
                        val sellingPrice = parts[5].toDoubleOrNull() ?: 0.0
                        val costPrice = parts[6].toDoubleOrNull() ?: 0.0
                        val barcode = parts[7]
                        val sizesString = parts[8]
                        val description = if (parts.size > 9) parts[9].replace(";", ",") else ""

                        val product = Product(
                            name = name,
                            category = category,
                            brand = brand,
                            supplier = supplier,
                            description = description,
                            barcode = barcode.ifBlank { generateUniqueBarcode() },
                            sellingPrice = sellingPrice,
                            costPrice = costPrice,
                            sizesString = sizesString
                        )
                        repository.insertProduct(product)
                        importedCount++
                    }
                }
                showStatus("Successfully imported $importedCount products from template!")
            } catch (e: Exception) {
                showStatus("Import Error: Make sure CSV format matches precisely.")
            }
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        val current = StringBuilder()
        for (char in line) {
            if (char == '\"') {
                inQuotes = !inQuotes
            } else if (char == ',' && !inQuotes) {
                result.add(current.toString().trim())
                current.setLength(0)
            } else {
                current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    fun simulateManualCloudBackup() {
        showStatus("Backup generated! Cloud state uploaded safely to secure hosting.")
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    private fun showStatus(msg: String) {
        _statusMessage.value = msg
    }
}
