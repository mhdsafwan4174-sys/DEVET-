package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.theme.BrightRust
import com.example.ui.theme.RustLeather

@Composable
fun SettingsScreen(
    products: List<Product>,
    isAdmin: Boolean,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var backupStatus by remember { mutableStateOf("Database state is healthy") }
    var csvImportInput by remember { mutableStateOf("") }
    var displayedExportCsv by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Role Configuration
        item {
            Text(
                text = "Backup & Role Configurations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Toggle role card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "User Permission Testing",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Slide the switch or toggle in header to switch between Admin manager (view cost margins & margins reports) and Staff user (restricted cost view).",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isAdmin) "Current Role: ADMIN (Full Control)" else "Current Role: STAFF (Restricted)",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Switch(
                            checked = isAdmin,
                            onCheckedChange = { viewModel.setAdmin(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RustLeather,
                                checkedTrackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("role_switch")
                        )
                    }
                }
            }
        }

        // Automatic and Manual Backup Cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Data Resiliency Backups",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Automatic Cloud Backups", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Live replication to secure DEVET hosts", fontSize = 10.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFD4EFDF))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ACTIVE", color = Color(0xFF27AE60), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    Text("Manual Backup:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            backupStatus = "Manual replication successful! Size: ${products.size} styles backed up."
                            viewModel.simulateManualCloudBackup()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate Cloud Backup Sync")
                    }
                    Text(backupStatus, fontSize = 11.sp, color = RustLeather, fontWeight = FontWeight.Bold)
                }
            }
        }

        // CSV Import / Export card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "CSV Product Exchange (Excel Friendly)",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Import products via standard CSV text format below, or export the entire current local inventory for spreadsheet tracking.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    // Export section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                displayedExportCsv = viewModel.exportToCSVString(products)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightRust),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generate CSV", fontSize = 12.sp)
                        }

                        if (displayedExportCsv.isNotBlank()) {
                            Button(
                                onClick = { displayedExportCsv = "" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Hide CSV", fontSize = 12.sp)
                            }
                        }
                    }

                    if (displayedExportCsv.isNotBlank()) {
                        OutlinedTextField(
                            value = displayedExportCsv,
                            onValueChange = {},
                            label = { Text("Exported Inventory CSV") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    // Import section
                    Text("Import Products CSV Block:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = csvImportInput,
                        onValueChange = { csvImportInput = it },
                        placeholder = { Text("Paste CSV lines here...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                    
                    Button(
                        onClick = {
                            if (csvImportInput.isNotBlank()) {
                                viewModel.importFromCSVString(csvImportInput)
                                csvImportInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RustLeather),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = csvImportInput.isNotBlank()
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run CSV Import Parse")
                    }

                    Text(
                        text = "CSV FORMAT:\nID,Name,Category,Brand,Supplier,SellingPrice,CostPrice,Barcode,StockString,Description",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Future Scope
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Future Ready Capabilities Map (Omitted currently):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("• Customer loyalty metric triggers", fontSize = 10.sp, color = Color.Gray)
                    Text("• Integrated Supplier automatic purchase orders", fontSize = 10.sp, color = Color.Gray)
                    Text("• Multi-branch footwear stocks real-time relay", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
