package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrightRust
import com.example.ui.theme.RustLeather

@Composable
fun DevetLogo(
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    badgeSize: Double = 42.0
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Geometric Shoe Emblem - Craft signature drawn with Canvas
        Canvas(modifier = Modifier.size(badgeSize.dp)) {
            val w = size.width
            val h = size.height

            // Background capsule
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(RustLeather, BrightRust),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                ),
                cornerRadius = CornerRadius(w * 0.3f, h * 0.3f)
            )

            // Dynamic athletic/shoe wings icon
            val path = Path().apply {
                // Sole line
                moveTo(w * 0.25f, h * 0.72f)
                quadraticTo(w * 0.5f, h * 0.78f, w * 0.75f, h * 0.72f)
                // Back/heel
                lineTo(w * 0.78f, h * 0.45f)
                quadraticTo(w * 0.7f, h * 0.35f, w * 0.6f, h * 0.5f)
                // Tongue/tongue arch
                quadraticTo(w * 0.5f, h * 0.3f, w * 0.4f, h * 0.45f)
                // Toe/vamp
                quadraticTo(w * 0.28f, h * 0.52f, w * 0.22f, h * 0.65f)
                close()
            }
            drawPath(
                path = path,
                color = Color.White
            )

            // Dynamic diagonal speed lines representing retail motion
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(w * 0.35f, h * 0.82f),
                end = Offset(w * 0.65f, h * 0.82f),
                strokeWidth = 2.dp.toPx()
            )
        }

        if (showText) {
            Column {
                Text(
                    text = "DEVET",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 2.sp,
                    color = textColor
                )
                Text(
                    text = "RETAIL FOOTWEAR WORKSPACE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun FootwearVisual(
    category: String,
    angle: String,
    modifier: Modifier = Modifier,
    customColorSeed: Int = 0
) {
    // Generate theme colors based on category/seed
    val baseColor = when (category) {
        "Shoes" -> Color(0xFFD35400)      // Rust Orange
        "Sandals" -> Color(0xFF1ABC9C)    // Teal Sandal
        "Slippers" -> Color(0xFF9B59B6)   // Purple Cozy Slipper
        "Chappals" -> Color(0xFFF1C40F)   // Warm Yellow Chappal
        else -> Color(0xFF34495E)         // Slate accessories
    }

    val finalColor = if (customColorSeed != 0) {
        Color(customColorSeed)
    } else {
        baseColor
    }

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val w = size.width
                val h = size.height
                val r = Math.min(w, h) * 0.4f

                when (category) {
                    "Shoes" -> {
                        when (angle) {
                            "Front View" -> {
                                // Draw a front sneaker silhouette facing forward
                                drawOval(finalColor, Offset(cx - r * 0.5f, cy - r * 0.4f), Size(r * 1.0f, r * 1.2f))
                                drawOval(finalColor.copy(alpha = 0.8f), Offset(cx - r * 0.4f, cy - r * 0.2f), Size(r * 0.8f, r * 0.9f))
                                // Laces laces
                                for (i in 0..3) {
                                    drawLine(Color.White, Offset(cx - r * 0.2f, cy - r * 0.1f + (i * 12f)), Offset(cx + r * 0.2f, cy - r * 0.1f + (i * 12f)), strokeWidth = 3f)
                                }
                                // Midsole sole
                                drawRoundRect(Color.White, Offset(cx - r * 0.55f, cy + r * 0.68f), Size(r * 1.1f, r * 0.15f), CornerRadius(10f, 10f))
                            }
                            "Back View" -> {
                                // Heel/collar backing profile
                                drawOval(finalColor, Offset(cx - r * 0.45f, cy - r * 0.5f), Size(r * 0.9f, r * 1.2f))
                                drawRoundRect(finalColor.copy(alpha = 0.7f), Offset(cx - r * 0.3f, cy - r * 0.4f), Size(r * 0.6f, r * 0.8f))
                                // Heel loop tab
                                drawRoundRect(RustLeather, Offset(cx - r * 0.1f, cy - r * 0.7f), Size(r * 0.2f, r * 0.3f), CornerRadius(5f, 5f))
                                // Sole strip
                                drawRect(Color.White, Offset(cx - r * 0.5f, cy + r * 0.65f), Size(r * 1.0f, r * 0.12f))
                            }
                            "Left Side View", "Right Side View" -> {
                                val isLeft = angle.startsWith("Left")
                                val toeOffset = if (isLeft) -r * 0.8f else r * 0.8f
                                val heelOffset = if (isLeft) r * 0.6f else -r * 0.6f

                                val path = Path().apply {
                                    moveTo(cx + heelOffset, cy + r * 0.4f) // Back heel
                                    quadraticTo(cx + heelOffset * 1.1f, cy - r * 0.2f, cx + heelOffset * 0.6f, cy - r * 0.4f) // Collar
                                    quadraticTo(cx, cy - r * 0.5f, cx + toeOffset * 0.2f, cy - r * 0.1f) // Lace tray
                                    quadraticTo(cx + toeOffset * 0.7f, cy + r * 0.1f, cx + toeOffset, cy + r * 0.4f) // Toe cap
                                    quadraticTo(cx, cy + r * 0.45f, cx + heelOffset, cy + r * 0.4f) // Bottom line
                                }
                                drawPath(path, finalColor)

                                // White athletic streak swoop logo
                                val swoop = Path().apply {
                                    moveTo(cx + heelOffset * 0.3f, cy)
                                    quadraticTo(cx, cy - r * 0.1f, cx + toeOffset * 0.4f, cy + r * 0.1f)
                                    quadraticTo(cx, cy + r * 0.1f, cx + heelOffset * 0.3f, cy)
                                }
                                drawPath(swoop, Color.White.copy(alpha = 0.9f))

                                // Sole
                                drawRoundRect(Color.White, Offset(cx - r * 0.9f, cy + r * 0.38f), Size(r * 1.8f, r * 0.12f), CornerRadius(8f, 8f))
                            }
                            "Top View" -> {
                                // Looking inside from top
                                drawOval(finalColor, Offset(cx - r * 0.35f, cy - r * 1.0f), Size(r * 0.7f, r * 2.0f))
                                // Footbed opening collar
                                drawOval(Color(0xFF2C3E50), Offset(cx - r * 0.22f, cy + r * 0.1f), Size(r * 0.44f, r * 0.6f))
                                // Laces
                                for (i in 0..4) {
                                    drawLine(Color.White, Offset(cx - r * 0.15f, cy - r * 0.5f + (i * 15f)), Offset(cx + r * 0.15f, cy - r * 0.5f + (i * 15f)), strokeWidth = 4f)
                                }
                            }
                            "Sole View" -> {
                                // Bottom sole with traction treads
                                drawOval(Color.White, Offset(cx - r * 0.35f, cy - r * 1.0f), Size(r * 0.7f, r * 2.0f))
                                // Grid patterned tread lines
                                for (i in -4..5) {
                                    drawLine(finalColor, Offset(cx - r * 0.28f, cy + (i * 20f)), Offset(cx + r * 0.28f, cy + (i * 20f)), strokeWidth = 3f)
                                }
                            }
                            else -> {
                                drawCircle(finalColor, r)
                            }
                        }
                    }
                    "Sandals" -> {
                        // Strappy sandle visual
                        drawOval(Color(0xFFD0C3B7), Offset(cx - r * 0.35f, cy - r * 0.9f), Size(r * 0.7f, r * 1.8f)) // cork sole
                        // Stitched cross straps
                        drawRect(finalColor, Offset(cx - r * 0.32f, cy - r * 0.4f), Size(r * 0.64f, r * 0.2f))
                        drawRect(finalColor, Offset(cx - r * 0.32f, cy + r * 0.1f), Size(r * 0.64f, r * 0.2f))
                        // Sole stitch outline
                        drawOval(Color.White, Offset(cx - r * 0.32f, cy - r * 0.85f), Size(r * 0.64f, r * 1.7f), style = Stroke(width = 3f))
                    }
                    "Slippers", "Chappals" -> {
                        // Slipper loop straps / v-strap
                        drawOval(Color(0xFFF5B041), Offset(cx - r * 0.35f, cy - r * 0.9f), Size(r * 0.7f, r * 1.8f)) // flat footbed
                        // Draw V strap chappal
                        val path = Path().apply {
                            moveTo(cx, cy - r * 0.4f) // anchor near toe split
                            lineTo(cx - r * 0.35f, cy + r * 0.1f) // left strap
                            lineTo(cx - r * 0.25f, cy + r * 0.18f)
                            lineTo(cx, cy - r * 0.3f)
                            lineTo(cx + r * 0.25f, cy + r * 0.18f)
                            lineTo(cx + r * 0.35f, cy + r * 0.1f)
                            close()
                        }
                        drawPath(path, finalColor)
                    }
                    else -> {
                        // Accessroy or packaging box
                        drawRoundRect(finalColor, Offset(cx - r * 0.7f, cy - r * 0.5f), Size(r * 1.4f, r * 1.0f), CornerRadius(15f, 15f))
                        drawRoundRect(Color.White, Offset(cx - r * 0.5f, cy - r * 0.3f), Size(r * 1.0f, r * 0.6f), CornerRadius(5f,5f), style = Stroke(width = 3f))
                        // Draw a small decorative accent line on the box instead of Composable Text
                        drawLine(Color.White, Offset(cx - r * 0.3f, cy), Offset(cx + r * 0.3f, cy), strokeWidth = 4f)
                    }
                }
            }
            Text(
                text = angle,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
