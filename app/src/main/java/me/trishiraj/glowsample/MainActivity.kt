package me.trishiraj.glowsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.trishiraj.composeglow.ShadowBlurStyle
import me.trishiraj.composeglow.dropShadow
import me.trishiraj.glowsample.ui.theme.GlowSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlowSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DropShadowDemoScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DropShadowDemoScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically), // Adjusted spacing
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Default Drop Shadow (Normal Blur)",
            modifier = Modifier
                .dropShadow()
                .background(Color.White)
                .padding(16.dp)
        )

        Text(
            text = "Colored Offset Shadow (Solid Blur)",
            modifier = Modifier
                .dropShadow(
                    color = Color.Magenta.copy(alpha = 0.7f),
                    offsetX = 8.dp,
                    offsetY = 8.dp,
                    blurRadius = 8.dp,
                    blurStyle = ShadowBlurStyle.SOLID
                )
                .background(Color.White)
                .padding(16.dp)
        )

        Text(
            text = "Spread Shadow (Inner Blur)",
            modifier = Modifier
                .dropShadow(
                    color = Color.Cyan.copy(alpha = 0.7f),
                    borderRadius = 12.dp,
                    blurRadius = 16.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    spread = 6.dp,
                    blurStyle = ShadowBlurStyle.INNER
                )
                .background(Color.White)
                .padding(16.dp)
        )

        Text(
            text = "Gradient Drop Shadow (Outer Blur)",
            modifier = Modifier
                .dropShadow(
                    gradientColors = listOf(Color.Red.copy(alpha = 0.7f), Color.Blue.copy(alpha = 0.7f)),
                    gradientStartFactorX = 0f,
                    gradientStartFactorY = 0f,
                    gradientEndFactorX = 1f,
                    gradientEndFactorY = 1f,
                    borderRadius = 12.dp,
                    blurRadius = 16.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    spread = 2.dp,
                    alpha = 1f,
                    blurStyle = ShadowBlurStyle.OUTER
                )
                .background(Color.White)
                .padding(16.dp)
        )

        Text(
            text = "Gyro Parallax Shadow (Tilt Device)",
            modifier = Modifier
                .dropShadow(
                    color = Color.Green.copy(alpha = 0.6f),
                    borderRadius = 8.dp,
                    blurRadius = 10.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    spread = 1.dp,
                    blurStyle = ShadowBlurStyle.NORMAL,
                    enableGyroParallax = true,
                    parallaxSensitivity = 8.dp
                )
                .background(Color.White)
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DropShadowDemoScreenPreview() {
    GlowSampleTheme {
        DropShadowDemoScreen()
    }
}
