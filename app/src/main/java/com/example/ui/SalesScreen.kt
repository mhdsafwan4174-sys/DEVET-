package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.theme.BrightRust
import com.example.ui.theme.RustLeather

@Composable
fun SalesScreen(
    products: List<Product>,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val cart by viewModel.cart.collectAsState()
    var selectedProductForCart by remember { mutableStateOf<Product?>(null) }
    var selectedSizeForCart by remember { mutableStateOf("") }
    var buyQty by remember { mutableStateOf(1) }

    var showCameraSim by remember { mutableStateOf(false) }
    var showReceiptInvoice by remember { mutableStateOf(false) }

    // POS Search States
    var posSearchQuery by remember { mutableStateOf("") }
    val filteredPosList = products.filter { p ->
        p.name.contains(posSearchQuery, ignoreCase = true) ||
        p.brand.contains(posSearchQuery, ignoreCase = true) ||
        p.barcode.contains(posSearchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // POS Header Row with Simulator Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DEVET Point of Sale (POS)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Camera simulation trigger
            Button(
                onClick = { showCameraSim = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan Barcode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Live visual search bar
        TextField(
            value = posSearchQuery,
            onValueChange = { posSearchQuery = it },
            placeholder = { Text("Search shoes, sandals or enter barcode...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (posSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { posSearchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pos_search_input"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Split view: Left pane is product selection, Right is active cart items list
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Catalog Column (60% weight on horizontal desktop or full screen on small)
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Shoe Catalog Selection", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                
                if (filteredPosList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No footwear products found.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPosList) { prod ->
                            val totalQty = prod.getTotalStock()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedProductForCart?.id == prod.id) MaterialTheme.colorScheme.secondaryContainer 
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable {
                                        selectedProductForCart = prod
                                        selectedSizeForCart = prod.getSizesMap().keys.firstOrNull() ?: ""
                                        buyQty = 1
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${prod.brand} • ${prod.category} • SKU: ${prod.barcode}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    // Sizes available list
                                    Text(
                                        text = "In Stock: ${prod.sizesString.replace(",", " | ")}",
                                        fontSize = 9.sp,
                                        color = if (totalQty == 0) Color.Red else Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = String.format("$%.2f", prod.sellingPrice),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right active cart summary Column (40% weight)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "POS Billing Drawer",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    if (cart.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color.Gray)
                                Text("Billing drawer empty.", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        // Cart Listing
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val itemsList = cart.flatMap { (b, m) -> m.map { (s, q) -> Triple(b, s, q) } }
                            items(itemsList) { (barcode, size, qty) ->
                                val matchedProd = products.find { it.barcode == barcode } ?: return@items
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(matchedProd.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Size: $size • $qty prs", fontSize = 10.sp, color = Color.Gray)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val stockMax = matchedProd.getSizesMap()[size] ?: 0
                                        IconButton(
                                            onClick = { viewModel.updateCartQty(barcode, size, qty - 1, stockMax) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp))
                                        }

                                        Text("$qty", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                        IconButton(
                                            onClick = { viewModel.updateCartQty(barcode, size, qty + 1, stockMax) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                        }

                                        IconButton(
                                            onClick = { viewModel.removeFromCart(barcode, size) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Calculate grand tallies
                        val totalBill = cart.entries.sumOf { (barcode, m) ->
                            val prod = products.find { it.barcode == barcode }
                            val price = prod?.sellingPrice ?: 0.0
                            m.entries.sumOf { (_, q) -> price * q }
                        }

                        // Subtotal pricing
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Units Qty:", fontSize = 11.sp, color = Color.Gray)
                            val totalUnits = cart.values.sumOf { it.values.sum() }
                            Text("$totalUnits pairs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Bill:", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                            Text(String.format("$%.2f", totalBill), fontSize = 13.sp, fontWeight = FontWeight.Black, color = RustLeather)
                        }

                        // Clear and Checkout Buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.clearCart() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Clear", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val checkoutSuccess = viewModel.checkout(products)
                                    if (checkoutSuccess) {
                                        showReceiptInvoice = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RustLeather),
                                modifier = Modifier.weight(1.5f).testTag("pos_checkout_button"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Checkout ($)", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Selected Product Detail Drawer mapping
        selectedProductForCart?.let { prod ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("Add Selection Details:", fontSize = 11.sp, color = RustLeather, fontWeight = FontWeight.Bold)
                        Text(prod.name, fontWeight = FontWeight.Black, fontSize = 13.sp, maxLines = 1)
                        
                        // Sizes selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Choose Size:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            
                            val sizes = prod.getSizesMap()
                            sizes.entries.forEach { (sz, stock) ->
                                if (stock > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (selectedSizeForCart == sz) RustLeather else MaterialTheme.colorScheme.surface)
                                            .border(1.dp, if (selectedSizeForCart == sz) Color.Transparent else Color.LightGray, RoundedCornerShape(4.dp))
                                            .clickable { selectedSizeForCart = sz }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(sz, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (selectedSizeForCart == sz) Color.White else Color.Black)
                                    }
                                }
                            }
                        }
                    }

                    // Stepper quantity and add button
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        val availableStockAmount = prod.getSizesMap()[selectedSizeForCart] ?: 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { buyQty = (buyQty - 1).coerceAtLeast(1) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null)
                            }
                            Text("$buyQty", fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { buyQty = (buyQty + 1).coerceAtLeast(1) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                        
                        Text("Limit: $availableStockAmount prs available", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))

                        Button(
                            onClick = {
                                if (selectedSizeForCart.isNotBlank()) {
                                    viewModel.addToCart(prod, selectedSizeForCart, buyQty)
                                    buyQty = 1
                                } else {
                                    viewModel.simulateManualCloudBackup() // status alert
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightRust),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_to_cart_button"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Add to Cart", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Camera Simulation dialog
    if (showCameraSim) {
        CameraScanSimulatorDialog(
            products = products,
            onDismiss = { showCameraSim = false },
            onScan = { barcode ->
                viewModel.scanBarcodeSimulated(barcode, products)
                // If scanned inside POS, automatically find product and select it
                val p = products.find { it.barcode == barcode }
                if (p != null) {
                    selectedProductForCart = p
                    selectedSizeForCart = p.getSizesMap().keys.firstOrNull() ?: ""
                    buyQty = 1
                }
                showCameraSim = false
            }
        )
    }

    // Receipt Invoice Modal
    if (showReceiptInvoice) {
        ThermalReceiptDialog(
            onDismiss = { showReceiptInvoice = false }
        )
    }
}

@Composable
fun CameraScanSimulatorDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onScan: (barcode: String) -> Unit
) {
    var timerVal by remember { mutableStateOf(1f) }
    
    // Laser animation
    val infiniteTransition = rememberInfiniteTransition()
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DEVET Barcode Scanner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                // Camera box simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .border(2.dp, RustLeather, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw video camera guidelines and animated red laser
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        
                        // Crop target box
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.3f),
                            topLeft = Offset(w * 0.15f, h * 0.2f),
                            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.6f),
                            style = strokestyle
                        )
                        
                        // Animated red laser line
                        val laserY = (h * 0.2f) + (h * 0.6f * laserOffset)
                        drawLine(
                            color = Color.Red,
                            start = Offset(w * 0.15f, laserY),
                            end = Offset(w * 0.85f, laserY),
                            strokeWidth = 3.dp.toPx()
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.Green, modifier = Modifier.size(32.dp))
                        Text("Active Camera Feed Simulation", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Align footwear barcode within bracket", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    }
                }

                // Barcode simulation list button targets
                Text("Select a barcode to simulate scan:", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
                
                LazyColumn(
                    modifier = Modifier.height(130.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(products) { prod ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onScan(prod.barcode) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(prod.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Badge(containerColor = RustLeather, contentColor = Color.White) {
                                Text(prod.barcode, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Text(
                    text = "Simulating real camera viewport triggers scanning match instantaneously in POS list.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private val strokestyle = stroke(width = 4f)
private fun stroke(width: Float) = Stroke(width = width)

@Composable
fun ThermalReceiptDialog(
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
                Text("Transaction Success", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF27AE60))

                // Beautiful Thermal Receipt representation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDFE)),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Logo in receipt
                        Text("D E V E T", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp, color = Color.Black)
                        Text("RETAIL STOCK SYSTEMS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Footwear, Shoes and Accessories", fontSize = 9.sp, color = Color.Gray)
                        Text("----------------------------------------", color = Color.LightGray)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Receipt #ID:", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            Text("TXN-38290", fontSize = 10.sp, color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cashier:", fontSize = 10.sp, color = Color.Black)
                            Text("System Register", fontSize = 10.sp, color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status:", fontSize = 10.sp, color = Color.Black)
                            Text("PAID / SECURED CASH", fontSize = 10.sp, color = Color(0xFF27AE60), fontWeight = FontWeight.Bold)
                        }

                        Text("----------------------------------------", color = Color.LightGray)

                        // Sample items printed representation
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Item(s) Ordered", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("Amount", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("1x DEVET Air Elite (Sz 41)", fontSize = 10.sp, color = Color.Black)
                            Text("$119.99", fontSize = 10.sp, color = Color.Black)
                        }

                        Text("----------------------------------------", color = Color.LightGray)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax (VAT 5%):", fontSize = 10.sp, color = Color.Black)
                            Text("$6.00", fontSize = 10.sp, color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid:", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            Text("$125.99", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }

                        Text("----------------------------------------", color = Color.LightGray)
                        Text("Thank you for shopping at DEVET!", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Returns accepted within 14 days with sticker tag", fontSize = 8.sp, color = Color.Gray)
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
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = RustLeather),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print thermal", color = Color.White)
                    }
                }
            }
        }
    }
}
