package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.theme.BrightRust
import com.example.ui.theme.RustLeather

@Composable
fun ProductManagementScreen(
    products: List<Product>,
    editingProduct: Product?,
    isAdmin: Boolean,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    if (currentScreen == AppScreen.ProductForm) {
        ProductForm(
            editingProduct = editingProduct,
            isAdmin = isAdmin,
            onSave = { name, category, brand, supplier, description, barcode, imagePaths, sellingPrice, costPrice, sizesMap ->
                viewModel.saveProduct(name, category, brand, supplier, description, barcode, imagePaths, sellingPrice, costPrice, sizesMap)
            },
            onCancel = { viewModel.setScreen(AppScreen.Inventory) },
            viewModel = viewModel
        )
    } else {
        InventoryList(
            products = products,
            isAdmin = isAdmin,
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@Composable
fun InventoryList(
    products: List<Product>,
    isAdmin: Boolean,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedStockStatus by viewModel.selectedStockStatus.collectAsState()
    val selectedSizeFilter by viewModel.selectedSizeFilter.collectAsState()

    var showAdjustDialogProduct by remember { mutableStateOf<Product?>(null) }
    var showBarcodeLabelProduct by remember { mutableStateOf<Product?>(null) }

    // Filtering logic
    val filteredProducts = products.filter { p ->
        // Search
        val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) || 
                            p.brand.contains(searchQuery, ignoreCase = true) || 
                            p.barcode.contains(searchQuery, ignoreCase = true)
        
        // Category
        val matchesCategory = selectedCategory == "All" || p.category == selectedCategory
        
        // Stock Status
        val totalStock = p.getTotalStock()
        val matchesStatus = when (selectedStockStatus) {
            "All" -> true
            "In Stock" -> totalStock > viewModel.LOW_STOCK_THRESHOLD
            "Low Stock" -> totalStock in 1..viewModel.LOW_STOCK_THRESHOLD
            "Out of Stock" -> totalStock == 0
            else -> true
        }

        // Size filter
        val matchesSize = selectedSizeFilter == "All" || p.getSizesMap().containsKey(selectedSizeFilter)

        matchesSearch && matchesCategory && matchesStatus && matchesSize
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Add Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search shoes, barcode, brand...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("inventory_search_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            FloatingActionButton(
                onClick = { viewModel.startAddProduct() },
                containerColor = RustLeather,
                contentColor = Color.White,
                modifier = Modifier
                    .testTag("add_product_button")
                    .size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Shoe")
            }
        }

        // Advanced filter row
        ExpansionFiltersSection(viewModel)

        // Count indicator
        Text(
            text = "Viewing ${filteredProducts.size} styles out of ${products.size}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        // Product Catalog Cards
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching footwear found",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Try adjusting filters or import products.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCatalogItemCard(
                        product = product,
                        isAdmin = isAdmin,
                        onEdit = { viewModel.startEditProduct(product) },
                        onDuplicate = { viewModel.duplicateProduct(product) },
                        onDelete = { viewModel.deleteProduct(product) },
                        onAdjustStock = { showAdjustDialogProduct = product },
                        onPrintBarcode = { showBarcodeLabelProduct = product },
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Interactive stock adjustments dialog
    showAdjustDialogProduct?.let { product ->
        StockAdjustDialog(
            product = product,
            onDismiss = { showAdjustDialogProduct = null },
            onConfirm = { size, change, isDamaged ->
                viewModel.adjustStockForDamagedOrReturned(product, size, change, isDamaged)
                showAdjustDialogProduct = null
            }
        )
    }

    // Barcode Sticker Label Dialog
    showBarcodeLabelProduct?.let { product ->
        BarcodeStickerDialog(
            product = product,
            onDismiss = { showBarcodeLabelProduct = null }
        )
    }
}

@Composable
fun ExpansionFiltersSection(viewModel: MainViewModel) {
    val categories = listOf("All", "Shoes", "Chappals", "Sandals", "Slippers", "Accessories")
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val statuses = listOf("All", "In Stock", "Low Stock", "Out of Stock")
    val selectedStockStatus by viewModel.selectedStockStatus.collectAsState()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Categories scroll
            Text("Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.take(3).forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.updateCategoryFilter(cat) },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
                categories.drop(3).forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.updateCategoryFilter(cat) },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }

            // Stock Status
            Text("Stock Level:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                statuses.forEach { stat ->
                    FilterChip(
                        selected = selectedStockStatus == stat,
                        onClick = { viewModel.updateStockStatusFilter(stat) },
                        label = { Text(stat, fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCatalogItemCard(
    product: Product,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: () -> Unit,
    onPrintBarcode: () -> Unit,
    viewModel: MainViewModel
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${product.brand} • ${product.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = String.format("$%.2f", product.sellingPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Sizes status text block
            val totalStock = product.getTotalStock()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (totalStock == 0) Color(0xFFFADBD8)
                            else if (totalStock <= viewModel.LOW_STOCK_THRESHOLD) Color(0xFFFDEBD0)
                            else Color(0xFFD4EFDF)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (totalStock == 0) "OUT OF STOCK"
                               else if (totalStock <= viewModel.LOW_STOCK_THRESHOLD) "LOW STOCK ($totalStock)"
                               else "IN STOCK ($totalStock)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (totalStock == 0) Color(0xFFC0392B)
                                else if (totalStock <= viewModel.LOW_STOCK_THRESHOLD) Color(0xFFD35400)
                                else Color(0xFF27AE60)
                    )
                }

                Text(
                    text = "Barcode: ${product.barcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Size Breakdown:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand details"
                    )
                }
            }

            // Mini grid of sizes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val sizeMap = product.getSizesMap()
                if (sizeMap.isEmpty()) {
                    Text("No sizes configured.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    sizeMap.entries.take(8).forEach { (sz, qty) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (qty == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    else if (qty <= 3) Color(0xFFFDEBD0)
                                    else MaterialTheme.colorScheme.primaryContainer
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$sz:$qty",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (qty == 0) Color.Gray 
                                        else if (qty <= 3) Color(0xFFD35400)
                                        else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Expanded view containing full actions, supplier details, cost price (Admin Gated), and damage/return adjustment options
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(8.dp))

                if (product.description.isNotBlank()) {
                    Text(
                        text = "Description:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Text(
                        text = product.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = "Supplier: ${product.supplier.ifBlank { "N/A" }}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Cost price & product profit (ADMIN GATED)
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cost Price: " + String.format("$%.2f", product.costPrice),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC0392B)
                        )
                        val profit = product.sellingPrice - product.costPrice
                        val profitPct = if (product.sellingPrice > 0) (profit / product.sellingPrice) * 100 else 0.0
                        Text(
                            text = String.format("Gross Profit: $%.2f (%.1f%%)", profit, profitPct),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF27AE60)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Angle Gallery Viewer simulation if images exist
                if (product.imagePaths.isNotBlank()) {
                    Text(
                        text = "Visual Angles Gallery:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val angles = product.imagePaths.split(",")
                        angles.take(4).forEach { ang ->
                            FootwearVisual(
                                category = product.category,
                                angle = when (ang) {
                                    "shoe_front" -> "Front"
                                    "shoe_side" -> "Side"
                                    "shoe_sole" -> "Sole"
                                    "sandal_front" -> "Front"
                                    "sandal_side" -> "Side"
                                    else -> "Top"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAdjustStock,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adjust Stock", fontSize = 10.sp)
                    }

                    Button(
                        onClick = onPrintBarcode,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print Label", fontSize = 10.sp)
                    }
                    
                    Button(
                        onClick = onDuplicate,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D6D7E), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Duplicate", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductForm(
    editingProduct: Product?,
    isAdmin: Boolean,
    onSave: (String, String, String, String, String, String, String, Double, Double, Map<String, Int>) -> Unit,
    onCancel: () -> Unit,
    viewModel: MainViewModel
) {
    var name by remember { mutableStateOf(editingProduct?.name ?: "") }
    var category by remember { mutableStateOf(editingProduct?.category ?: "Shoes") }
    var brand by remember { mutableStateOf(editingProduct?.brand ?: "") }
    var supplier by remember { mutableStateOf(editingProduct?.supplier ?: "") }
    var description by remember { mutableStateOf(editingProduct?.description ?: "") }
    var barcode by remember { mutableStateOf(editingProduct?.barcode ?: "") }
    var sellingPriceStr by remember { mutableStateOf(editingProduct?.sellingPrice?.toString() ?: "") }
    var costPriceStr by remember { mutableStateOf(editingProduct?.costPrice?.toString() ?: "") }

    // Multi-select sizes map
    val defaultSizes = listOf("36", "37", "38", "39", "40", "41", "42", "43", "44", "45")
    val initialSelectedSizes = editingProduct?.getSizesMap() ?: emptyMap()
    val selectedSizesMap = remember { mutableStateMapOf<String, Int>().apply { putAll(initialSelectedSizes) } }

    val categories = listOf("Shoes", "Chappals", "Sandals", "Slippers", "Accessories")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = if (editingProduct == null) "Add Footwear Product" else "Edit Footwear Product",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Form Fields
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name *") },
                modifier = Modifier.fillMaxWidth().testTag("form_product_name"),
                singleLine = true
            )
        }

        item {
            Text("Category *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(3).forEach { cat ->
                    ElevatedAssistChip(
                        onClick = { category = cat },
                        label = { Text(cat) },
                        colors = AssistChipDefaults.elevatedAssistChipColors(
                            containerColor = if (category == cat) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.drop(3).forEach { cat ->
                    ElevatedAssistChip(
                        onClick = { category = cat },
                        label = { Text(cat) },
                        colors = AssistChipDefaults.elevatedAssistChipColors(
                            containerColor = if (category == cat) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Product Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode / SKU Number") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = { barcode = viewModel.generateUniqueBarcode() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Auto Gen")
                }
            }
        }

        // Standard Sizes selection matrix with live quantity inputs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Specify Sizing Ranges & Stock Quantities",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Select active sizes to configure their currently on-hand stockpile quantities.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    // Display Grid of togglable sizes in 2 rows of 5
                    val sizeChunks = defaultSizes.chunked(5)
                    sizeChunks.forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { sz ->
                                val isActive = selectedSizesMap.containsKey(sz)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isActive) RustLeather else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable {
                                            if (isActive) selectedSizesMap.remove(sz)
                                            else selectedSizesMap[sz] = 5
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sz,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Quantities for active sizes list
                    if (selectedSizesMap.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Define Quantity per Size:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        selectedSizesMap.entries.sortedBy { it.key }.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Size ${entry.key}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            val currentVal = selectedSizesMap[entry.key] ?: 0
                                            selectedSizesMap[entry.key] = (currentVal - 1).coerceAtLeast(0)
                                        }
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = (selectedSizesMap[entry.key] ?: 0).toString(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        modifier = Modifier.width(32.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

                                    IconButton(
                                        onClick = {
                                            val currentVal = selectedSizesMap[entry.key] ?: 0
                                            selectedSizesMap[entry.key] = currentVal + 1
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pricing Cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Pricing Schema Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    OutlinedTextField(
                        value = sellingPriceStr,
                        onValueChange = { sellingPriceStr = it },
                        label = { Text("Selling Price * ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (isAdmin) {
                        OutlinedTextField(
                            value = costPriceStr,
                            onValueChange = { costPriceStr = it },
                            label = { Text("Product Cost Price ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Profit panel calculation
                        val sell = sellingPriceStr.toDoubleOrNull() ?: 0.0
                        val cost = costPriceStr.toDoubleOrNull() ?: 0.0
                        val profit = sell - cost
                        val margin = if (sell > 0) (profit / sell) * 100 else 0.0

                        if (sell > 0 && cost > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F8F5))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = String.format("Auto Gross Profit Forecast: $%.2f (%.1f%% Margin)", profit, margin),
                                    color = Color(0xFF1ABC9C),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else if (editingProduct != null && editingProduct.costPrice > 0) {
                        // Cost hidden indicator
                        Text("Product Cost Price hidden from Staff role permissions", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val finalSell = sellingPriceStr.toDoubleOrNull() ?: 0.0
                        val finalCost = costPriceStr.toDoubleOrNull() ?: 0.0
                        // Default size mapped if empty
                        val finalSizes = if (selectedSizesMap.isEmpty()) mapOf("40" to 5) else selectedSizesMap.toMap()
                        
                        if (name.isNotBlank() && finalSell > 0.0) {
                            onSave(name, category, brand, supplier, description, barcode, "shoe_front,shoe_side,shoe_sole", finalSell, finalCost, finalSizes)
                        } else {
                            viewModel.simulateManualCloudBackup() // status alert
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RustLeather),
                    modifier = Modifier.weight(1f).testTag("save_product_button")
                ) {
                    Text("Save Product", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StockAdjustDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (size: String, changeAmount: Int, isDamaged: Boolean) -> Unit
) {
    var sizeTarget by remember { mutableStateOf(product.getSizesMap().keys.firstOrNull() ?: "40") }
    var changeAmount by remember { mutableStateOf(1) }
    var isDamaged by remember { mutableStateOf(true) } // true = damaged deduction/removal, false = custom return addition

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Stock Quick Correction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Text("${product.name}", fontSize = 12.sp, color = Color.Gray)

                // Select size in a horizontal grid row
                Text("Select Size to Adjust:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    product.getSizesMap().keys.forEach { sz ->
                        FilterChip(
                            selected = sizeTarget == sz,
                            onClick = { sizeTarget = sz },
                            label = { Text(sz) }
                        )
                    }
                }

                XToggleRow(
                    label = "Correction Reason:",
                    optionLeft = "Damaged Shoe",
                    optionRight = "Customer Return",
                    isSelectedRight = !isDamaged,
                    onToggle = { isDamaged = !it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isDamaged) "Deduct Quantity:" else "Restore/Return Quantity:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { changeAmount = (changeAmount - 1).coerceAtLeast(1) }) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }
                        Text("$changeAmount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { changeAmount += 1 }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Close")
                    }
                    Button(
                        onClick = {
                            val magnitude = if (isDamaged) -changeAmount else changeAmount
                            onConfirm(sizeTarget, magnitude, isDamaged)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RustLeather),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BarcodeStickerDialog(
    product: Product,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DEVET Barcode Sticker",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Render Barcode Sticker Simulation
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "DEVET FOOTWEAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                        Text(
                            text = product.name.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "PRICE: " + String.format("$%.2f", product.sellingPrice),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )

                        // Draw Barcode Lines on Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            val segments = 32
                            val step = this.size.width / segments
                            val barcodeSeed = product.barcode.hashCode()
                            val randomGen = java.util.Random(barcodeSeed.toLong())

                            for (i in 0 until segments) {
                                val isBar = randomGen.nextBoolean()
                                if (isBar) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = androidx.compose.ui.geometry.Offset(i * step, 0f),
                                        size = androidx.compose.ui.geometry.Size(step * 0.7f, this.size.height)
                                    )
                                }
                            }
                        }

                        Text(
                            text = product.barcode,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Text(
                    text = "Standard printable label sticker layout tailored for 2x1\" footwear retail adhesive tags.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Close")
                    }
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = RustLeather),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Print Label", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun XToggleRow(
    label: String,
    optionLeft: String,
    optionRight: String,
    isSelectedRight: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggle(false) }
                    .background(if (!isSelectedRight) RustLeather else Color.Transparent)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionLeft,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isSelectedRight) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggle(true) }
                    .background(if (isSelectedRight) RustLeather else Color.Transparent)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionRight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelectedRight) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
