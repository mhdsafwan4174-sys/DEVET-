package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.data.Sale
import com.example.ui.theme.BrightRust
import com.example.ui.theme.RustLeather

@Composable
fun DashboardScreen(
    products: List<Product>,
    sales: List<Sale>,
    isAdmin: Boolean,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // Math indicators
    val totalProducts = products.size
    val totalStockUnits = products.sumOf { p -> p.getTotalStock() }
    val lowStockCount = products.count { p ->
        val stock = p.getTotalStock()
        stock in 1..viewModel.LOW_STOCK_THRESHOLD
    }
    val outOfStockCount = products.count { p -> p.getTotalStock() == 0 }

    val todayMs = System.currentTimeMillis() - (24 * 3600 * 1000)
    val monthMs = System.currentTimeMillis() - (30L * 24 * 3600 * 1000)

    val dailySalesAmt = sales.filter { it.timestamp >= todayMs }.sumOf { it.totalPrice }
    val monthlySalesAmt = sales.filter { it.timestamp >= monthMs }.sumOf { it.totalPrice }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Footwear Retail Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track boots, sandals, sneakers and shoes with custom size metrics and secure local cash register.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Today's Sales Performance Card (Professional Polish Style)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                RustLeather,
                                RustLeather.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                // Abstract decoration bubble
                Canvas(modifier = Modifier.matchParentSize()) {
                    val canvasWidth = this.size.width
                    val canvasHeight = this.size.height
                    val minDim = minOf(canvasWidth, canvasHeight)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = minDim * 0.45f,
                        center = Offset(canvasWidth * 0.95f, canvasHeight * 1.05f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "TODAY'S REVENUE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("$%.2f", dailySalesAmt),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+12% vs yesterday",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "GOAL ACHIEVED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "78%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Quick Access Buttons Row
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Add Product
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Add Shoe",
                    bgColor = RustLeather,
                    textColor = Color.White,
                    onClick = { viewModel.startAddProduct() },
                    modifier = Modifier.weight(1.5f).testTag("quick_add_product")
                )
                
                // View Inventory
                QuickActionButton(
                    icon = Icons.Default.List,
                    label = "Stock",
                    bgColor = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary,
                    onClick = { viewModel.setScreen(AppScreen.Inventory) },
                    modifier = Modifier.weight(1f).testTag("quick_view_inventory")
                )

                // Create Sale / POS
                QuickActionButton(
                    icon = Icons.Default.ShoppingCart,
                    label = "New Sale",
                    bgColor = BrightRust,
                    textColor = Color.White,
                    onClick = { viewModel.setScreen(AppScreen.POS) },
                    modifier = Modifier.weight(1.3f).testTag("quick_create_sale")
                )

                // View Reports
                QuickActionButton(
                    icon = Icons.Default.Assessment,
                    label = "Reports",
                    bgColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { viewModel.setScreen(AppScreen.Reports) },
                    modifier = Modifier.weight(1.2f).testTag("quick_view_reports")
                )
            }
        }

        // Grid Metrics row 1: Products and Stock totals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricCard(
                    title = "Total Products",
                    value = totalProducts.toString(),
                    description = "Footwear catalog styles",
                    icon = Icons.Default.Category,
                    colorAccent = BrightRust,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Current Stock",
                    value = "$totalStockUnits prs",
                    description = "Total shoes in store",
                    icon = Icons.Default.Warehouse,
                    colorAccent = Color(0xFF008B8B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Grid Metrics row 2: Warnings Low & Out of stock
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricCard(
                    title = "Low Stock Items",
                    value = lowStockCount.toString(),
                    description = "Stock <= ${viewModel.LOW_STOCK_THRESHOLD} units",
                    icon = Icons.Default.Warning,
                    colorAccent = Color(0xFFD35400),
                    isAlert = lowStockCount > 0,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Out of Stock",
                    value = outOfStockCount.toString(),
                    description = "Drafted for restock",
                    icon = Icons.Default.HighlightOff,
                    colorAccent = Color(0xFFE74C3C),
                    isNeutralWarning = outOfStockCount > 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }


        // Sales Metric Card (Includes security filters based on Staff vs Admin view)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sales Performance Register",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF2ECC71)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Daily Revenue",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = String.format("$%.2f", dailySalesAmt),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Divider(
                            modifier = Modifier
                                .height(40.dp)
                                .width(1.dp)
                                .align(Alignment.CenterVertically),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                        Column {
                            Text(
                                text = "Monthly Revenue",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = String.format("$%.2f", monthlySalesAmt),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Recent Products title
        item {
            Text(
                text = "Recently Added Stock",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Listing recent products (max 3)
        if (products.isEmpty()) {
            item {
                Text(
                    text = "No products found. Tap Quick Add above to stock up.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            val recents = products.take(3)
            items(recents) { prod ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { viewModel.startEditProduct(prod) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prod.category.take(1),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = prod.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${prod.brand} • SKU: ${prod.barcode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format("$%.2f", prod.sellingPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val totalStock = prod.getTotalStock()
                        Text(
                            text = if (totalStock == 0) "Out of Stock" else "$totalStock units",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (totalStock == 0) Color.Red else if (totalStock <= viewModel.LOW_STOCK_THRESHOLD) Color(0xFFE67E22) else Color(0xFF2ECC71)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), // WarmSand (Teal border)
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE0F2F1), CircleShape), // Soft light teal circles
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF006A6A), // Professional Teal icon
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    description: String,
    icon: ImageVector,
    colorAccent: Color,
    isAlert: Boolean = false,
    isNeutralWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isAlert -> Color(0xFFFFEFEF)        // #FFEFEF Light Alert Red
        isNeutralWarning -> Color(0xFFF1F5F9) // Slate-50 background
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isAlert -> Color(0xFFFFDADA)        // #FFDADA Border Red
        isNeutralWarning -> Color(0xFFCBD5E1) // Slate-200 border
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isAlert -> Color(0xFFBA1A1A)        // Deep Red
        else -> MaterialTheme.colorScheme.onSurface
    }
    val iconColor = when {
        isAlert -> Color(0xFFBA1A1A)
        else -> colorAccent
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isAlert) Color(0xFFBA1A1A).copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                if (isAlert) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFDADA))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("ALERT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
                    }
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 10.sp,
                color = if (isAlert) Color(0xFFBA1A1A).copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
