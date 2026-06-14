package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.data.Sale
import com.example.ui.theme.BrightRust
import com.example.ui.theme.RustLeather

@Composable
fun ReportsScreen(
    products: List<Product>,
    sales: List<Sale>,
    isAdmin: Boolean,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Stock Reports, 1 = Sales Reports, 2 = Profit analytics

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Report Segmented Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = RustLeather
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Stock Reports", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Sales Registers", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Profit Margins", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTab) {
            0 -> StockReportTab(products, viewModel)
            1 -> SalesReportTab(sales)
            2 -> ProfitReportTab(sales, products, isAdmin)
        }
    }
}

@Composable
fun StockReportTab(products: List<Product>, viewModel: MainViewModel) {
    val lowStock = products.filter { it.getTotalStock() in 1..viewModel.LOW_STOCK_THRESHOLD }
    val outOfStock = products.filter { it.getTotalStock() == 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stock summary metrics card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Styles:", fontSize = 12.sp, color = Color.Gray)
                        Text("${products.size} styles", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Column {
                        Text("Low Stock styles:", fontSize = 12.sp, color = Color.Gray)
                        Text("${lowStock.size} styles", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFE67E22))
                    }
                    Column {
                        Text("Empty Stock styles:", fontSize = 12.sp, color = Color.Gray)
                        Text("${outOfStock.size} styles", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Red)
                    }
                }
            }
        }

        // Low stock Warning List
        item {
            Text(
                text = "Low Stock Watchlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (lowStock.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("No low stock warnings. Outstanding!", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            items(lowStock) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Category: ${item.category} • Barcode: ${item.barcode}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Badge(containerColor = Color(0xFFFDEBD0), contentColor = Color(0xFFD35400)) {
                        Text("${item.getTotalStock()} left", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Out of stock list
        item {
            Text(
                text = "Out of Stock Watchlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }

        if (outOfStock.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("No out of stock warning items.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            items(outOfStock) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                        Text("Brand: ${item.brand} • Barcode: ${item.barcode}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Badge(containerColor = Color(0xFFFADBD8), contentColor = Color.Red) {
                        Text("Sold Out", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SalesReportTab(sales: List<Sale>) {
    val totalRevenue = sales.sumOf { it.totalPrice }
    val totalVolume = sales.sumOf { it.quantity }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Historical Register Ledger", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cumulative Revenue:", fontSize = 12.sp, color = Color.Gray)
                        Text(String.format("$%.2f", totalRevenue), fontWeight = FontWeight.Black, fontSize = 16.sp, color = RustLeather)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pairs Sold count:", fontSize = 12.sp, color = Color.Gray)
                        Text("$totalVolume units", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Text("Best-Selling Categories", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        // Category-wise volume breakdown
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val catVolume = sales.groupBy { it.category }.mapValues { e -> e.value.sumOf { it.quantity } }
                    if (catVolume.isEmpty()) {
                        Text("No sales processed yet.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        catVolume.entries.sortedByDescending { it.value }.forEach { (cat, vol) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Simulated gauge
                                    Box(modifier = Modifier.width(60.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.LightGray)) {
                                        Box(modifier = Modifier.fillMaxHeight().width((vol * 12).dp).background(RustLeather))
                                    }
                                    Text("$vol prs", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Detailed Audit Logs", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        if (sales.isEmpty()) {
            item {
                Text("No transactions logged in database.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            items(sales) { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Size: ${s.size} • Qty: ${s.quantity} • Category: ${s.category}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(
                        text = String.format("$%.2f", s.totalPrice),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ProfitReportTab(sales: List<Sale>, products: List<Product>, isAdmin: Boolean) {
    if (!isAdmin) {
        // Staff view Gated access denied block
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Access Privileges Required",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Product wholesale cost indices, invoice margins, and gross profit tallies are secure blocks restricted exclusively to Admin managers.\n\nPlease check settings switch to demonstrate admin view.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        return
    }

    // Admin view computations
    val totalCost = sales.sumOf { s -> s.costPrice * s.quantity }
    val totalRevenue = sales.sumOf { it.totalPrice }
    val cumulativeProfit = totalRevenue - totalCost
    val profitRatio = if (totalRevenue > 0) (cumulativeProfit / totalRevenue) * 100 else 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F8F5))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Admin Financial Margin Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF16A085))
                    Divider(color = Color(0xFF16A085).copy(alpha = 0.1f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Revenue Realized:", fontSize = 12.sp, color = Color.Gray)
                        Text(String.format("$%.2f", totalRevenue), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Culminated Cost Price:", fontSize = 12.sp, color = Color.Gray)
                        Text(String.format("$%.2f", totalCost), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC0392B))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Profit Gained:", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF16A085))
                        Text(String.format("$%.2f (%.1f%% Margin)", cumulativeProfit, profitRatio), fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF16A085))
                    }
                }
            }
        }

        item {
            Text("Product-wise Cumulative Profit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        if (sales.isEmpty()) {
            item {
                Text("Process sales checkout to audit SKU-wise profits.", fontSize = 11.sp, color = Color.Gray)
            }
        } else {
            val saleGroups = sales.groupBy { it.productBarcode }
            items(saleGroups.keys.toList()) { barcode ->
                val logs = saleGroups[barcode] ?: emptyList()
                val qty = logs.sumOf { it.quantity }
                val rev = logs.sumOf { it.totalPrice }
                val cost = logs.sumOf { it.costPrice * it.quantity }
                val name = logs.firstOrNull()?.productName ?: "Unknown Product"
                val profit = rev - cost

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("SKU: $barcode • $qty prs sold", fontSize = 10.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format("Profit: +$%.2f", profit),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color(0xFF27AE60)
                        )
                        val margin = if (rev > 0) (profit / rev) * 100 else 0.0
                        Text(
                            text = String.format("%.1f%% Margin", margin),
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
