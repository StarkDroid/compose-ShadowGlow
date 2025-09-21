package me.trishiraj.glowsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.trishiraj.composeglow.ShadowBlurStyle
import me.trishiraj.composeglow.dropShadow
import me.trishiraj.glowsample.ui.theme.GlowSampleTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlowSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InteractiveDropShadowScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun InteractiveDropShadowScreen(modifier: Modifier = Modifier) {
    val isInDarkMode = isSystemInDarkTheme()

    var isGradientMode by remember { mutableStateOf(false) }
    val defaultSolidColor = if (isInDarkMode) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f)
    var shadowColor by remember { mutableStateOf(defaultSolidColor) }
    val gradientColors = listOf(Color.Red.copy(alpha = 0.7f), Color.Blue.copy(alpha = 0.7f))
    var gradientAlpha by remember { mutableFloatStateOf(1.0f) }

    var borderRadius by remember { mutableFloatStateOf(16f) }
    var blurRadius by remember { mutableFloatStateOf(16f) } // Base blur radius
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(4f) }
    var spread by remember { mutableFloatStateOf(0f) }

    var shadowBlurStyle by remember { mutableStateOf(ShadowBlurStyle.NORMAL) }
    var enableGyroParallax by remember { mutableStateOf(false) }
    var parallaxSensitivity by remember { mutableFloatStateOf(4f) }

    var enableBreathingEffect by remember { mutableStateOf(false) }
    var breathingIntensity by remember { mutableFloatStateOf(4f) } // Slider value for Dp
    var breathingDuration by remember { mutableFloatStateOf(1500f) } // Slider value for Millis

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val dynamicModifier = if (isGradientMode) {
            Modifier.dropShadow(
                gradientColors = gradientColors,
                alpha = gradientAlpha,
                borderRadius = borderRadius.dp,
                blurRadius = blurRadius.dp,
                offsetX = offsetX.dp,
                offsetY = offsetY.dp,
                spread = spread.dp,
                blurStyle = shadowBlurStyle,
                enableGyroParallax = enableGyroParallax,
                parallaxSensitivity = parallaxSensitivity.dp,
                enableBreathingEffect = enableBreathingEffect,
                breathingEffectIntensity = breathingIntensity.dp,
                breathingDurationMillis = breathingDuration.toInt()
            )
        } else {
            Modifier.dropShadow(
                color = shadowColor,
                borderRadius = borderRadius.dp,
                blurRadius = blurRadius.dp,
                offsetX = offsetX.dp,
                offsetY = offsetY.dp,
                spread = spread.dp,
                blurStyle = shadowBlurStyle,
                enableGyroParallax = enableGyroParallax,
                parallaxSensitivity = parallaxSensitivity.dp,
                enableBreathingEffect = enableBreathingEffect,
                breathingEffectIntensity = breathingIntensity.dp,
                breathingDurationMillis = breathingDuration.toInt()
            )
        }

        Card(
            modifier = dynamicModifier
                .fillMaxWidth(0.8f)
                .height(180.dp), // Increased height for more text
            shape = RoundedCornerShape(borderRadius.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Shadowed Card", fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isGradientMode) "Mode: Gradient" else "Mode: Solid Color",
                    fontSize = 12.sp
                )
                Text("Base Blur: ${blurRadius.roundToInt()}dp, Style: $shadowBlurStyle", fontSize = 12.sp)
                if (enableGyroParallax) {
                    Text("Gyro Parallax: Enabled (${parallaxSensitivity.roundToInt()}dp)", fontSize = 12.sp)
                }
                if (enableBreathingEffect) {
                    Text("Breathing Effect: Enabled (Intensity: ${breathingIntensity.roundToInt()}dp, Duration: ${breathingDuration.toInt()}ms)", fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Configuration", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ControlRow("Gradient Mode:", checkbox = true, checked = isGradientMode, onCheckedChange = { isGradientMode = it })
            ControlRow("Gyro Parallax:", checkbox = true, checked = enableGyroParallax, onCheckedChange = { enableGyroParallax = it })
            ControlRow("Breathing Effect:", checkbox = true, checked = enableBreathingEffect, onCheckedChange = { enableBreathingEffect = it })

            SliderControl("Border Radius: ${borderRadius.roundToInt()}dp", borderRadius, 0f, 50f) { borderRadius = it }
            SliderControl("Base Blur Radius: ${blurRadius.roundToInt()}dp", blurRadius, 0f, 50f) { blurRadius = it }
            SliderControl("Offset X: ${offsetX.roundToInt()}dp", offsetX, -50f, 50f) { offsetX = it }
            SliderControl("Offset Y: ${offsetY.roundToInt()}dp", offsetY, -50f, 50f) { offsetY = it }
            SliderControl("Spread: ${spread.roundToInt()}dp", spread, 0f, 30f) { spread = it }
            
            if (isGradientMode) {
                SliderControl("Gradient Alpha: ${String.format("%.2f", gradientAlpha)}", gradientAlpha, 0f, 1f) { gradientAlpha = it }
            }
            if (enableGyroParallax) {
                SliderControl("Parallax Sensitivity: ${parallaxSensitivity.roundToInt()}dp", parallaxSensitivity, 0f, 20f) { parallaxSensitivity = it }
            }
            if (enableBreathingEffect) {
                SliderControl("Breathing Intensity: ${breathingIntensity.roundToInt()}dp", breathingIntensity, 0f, 20f) { breathingIntensity = it }
                SliderControl("Breathing Duration: ${breathingDuration.toInt()}ms", breathingDuration, 500f, 5000f) { breathingDuration = it }
            }

            Text("Blur Style:", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                ShadowBlurStyle.entries.forEach { style ->
                    Button(
                        onClick = { shadowBlurStyle = style },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (shadowBlurStyle == style) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(style.name, fontSize = 10.sp)
                    }
                }
            }
            
            if (!isGradientMode) {
                Text("Shadow Color:", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    val adaptiveBlack = if (isInDarkMode) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f)
                    val commonColors = mapOf(
                        "Default" to adaptiveBlack,
                        "Red" to Color.Red.copy(alpha = 0.5f),
                        "Blue" to Color.Blue.copy(alpha = 0.5f),
                        "Green" to Color.Green.copy(alpha = 0.6f)
                    )
                    LaunchedEffect(isInDarkMode, defaultSolidColor) {
                        if (shadowColor != defaultSolidColor && (shadowColor == Color.Black.copy(alpha = 0.4f) || shadowColor == Color.White.copy(alpha = 0.5f))) {
                           shadowColor = defaultSolidColor // Adjust if the theme changed and an old default was selected
                        }
                    }

                    commonColors.forEach { (name, color) ->
                        Button(
                            onClick = { shadowColor = color },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (shadowColor == color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(name, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlRow(label: String, checkbox: Boolean = false, checked: Boolean = false, onCheckedChange: (Boolean) -> Unit = {}, content: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        if (checkbox) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        } else {
            content()
        }
    }
}

@Composable
fun SliderControl(label: String, value: Float, rangeStart: Float, rangeEnd: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Slider(value = value, onValueChange = onValueChange, valueRange = rangeStart..rangeEnd)
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InteractiveDropShadowScreenPreview() {
    GlowSampleTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            InteractiveDropShadowScreen()
        }
    }
}
