package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RustLeather
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppEntry()
            }
        }
    }
}

@Composable
fun MainAppEntry() {
    val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
    val products by viewModel.products.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var isLoggedIn by remember { mutableStateOf(false) }
    var enteredPasscode by remember { mutableStateOf("") }
    var loginAsAdminProfile by remember { mutableStateOf(true) }

    // Floating Snackbar trigger
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(3500)
            viewModel.clearStatus()
        }
    }

    if (!isLoggedIn) {
        // --- DEVET Interactive Login Page ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF132220), // Premium deep teal obsidian
                            Color(0xFF070B0A)  // Dark carbon slate
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Large styled DEVET Logo
                Spacer(modifier = Modifier.height(24.dp))
                DevetLogo(
                    textColor = Color.White,
                    badgeSize = 72.0
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "RETAIL SECURITY HUB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RustLeather,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Authenticate credentials to open store directory register.",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Profile Selector Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { loginAsAdminProfile = true },
                        colors = CardDefaults.cardColors(
                            containerColor = if (loginAsAdminProfile) Color(0xFF1C2833) else Color(0xFF11171D)
                        ),
                        border = if (loginAsAdminProfile) BorderStroke(1.5.dp, RustLeather) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = if (loginAsAdminProfile) RustLeather else Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Manager / Owner", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { loginAsAdminProfile = false },
                        colors = CardDefaults.cardColors(
                            containerColor = if (!loginAsAdminProfile) Color(0xFF1C2833) else Color(0xFF11171D)
                        ),
                        border = if (!loginAsAdminProfile) BorderStroke(1.5.dp, RustLeather) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PersonOutline, contentDescription = null, tint = if (!loginAsAdminProfile) RustLeather else Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Staff Cashier", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                // Passcode entry
                OutlinedTextField(
                    value = enteredPasscode,
                    onValueChange = { enteredPasscode = it },
                    label = { Text("Store Passcode", color = Color.Gray) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("login_passcode_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = RustLeather,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Text(
                    text = if (loginAsAdminProfile) "Tip: Key in any passcode to proceed (Forces Admin level access)" 
                           else "Tip: Key in any passcode to proceed (Forces restricted Staff level access)",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        viewModel.setAdmin(loginAsAdminProfile)
                        isLoggedIn = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RustLeather)
                ) {
                    Text("Secure Sign In", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        // --- DEVET Unified Main Application Shell ---
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Corporate logo
                            DevetLogo(showText = true, badgeSize = 36.0)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // User Profile Tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isAdmin) Color(0xFFE8F8F5) else Color(0xFFF2F4F4))
                                        .clickable { viewModel.toggleRole() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isAdmin) "ADMIN" else "STAFF",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isAdmin) Color(0xFF16A085) else Color(0xFF5D6D7E)
                                    )
                                }

                                // Explicit Logout icon
                                IconButton(
                                    onClick = { 
                                        isLoggedIn = false
                                        enteredPasscode = ""
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Log Out",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        Divider(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Dashboard,
                        onClick = { viewModel.setScreen(AppScreen.Dashboard) },
                        label = { Text("Dashboard", fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Inventory || currentScreen == AppScreen.ProductForm,
                        onClick = { viewModel.setScreen(AppScreen.Inventory) },
                        label = { Text("Stock Store", fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.Warehouse, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.POS,
                        onClick = { viewModel.setScreen(AppScreen.POS) },
                        label = { Text("Sale POS", fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Reports,
                        onClick = { viewModel.setScreen(AppScreen.Reports) },
                        label = { Text("Reports", fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Settings,
                        onClick = { viewModel.setScreen(AppScreen.Settings) },
                        label = { Text("Backup Tools", fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Crossfade screen swapping animation
                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(150)
                ) { targetScreen ->
                    when (targetScreen) {
                        AppScreen.Dashboard -> DashboardScreen(
                            products = products,
                            sales = sales,
                            isAdmin = isAdmin,
                            viewModel = viewModel
                        )
                        AppScreen.Inventory, AppScreen.ProductForm -> ProductManagementScreen(
                            products = products,
                            editingProduct = editingProduct,
                            isAdmin = isAdmin,
                            viewModel = viewModel
                        )
                        AppScreen.POS -> SalesScreen(
                            products = products,
                            viewModel = viewModel
                        )
                        AppScreen.Reports -> ReportsScreen(
                            products = products,
                            sales = sales,
                            isAdmin = isAdmin,
                            viewModel = viewModel
                        )
                        AppScreen.Settings -> SettingsScreen(
                            products = products,
                            isAdmin = isAdmin,
                            viewModel = viewModel
                        )
                    }
                }

                // Custom glowing floating Snackbar
                statusMessage?.let { status ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2833)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = status,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Dismiss",
                                    color = RustLeather,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .clickable { viewModel.clearStatus() }
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
