package me.trishiraj.glowsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Default Drop Shadow",
            modifier = Modifier
                .dropShadow()
                .background(Color.White)
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Colored Offset Shadow",
            modifier = Modifier
                .dropShadow(
                    color = Color.Magenta.copy(alpha = 0.5f),
                    offsetX = 8.dp,
                    offsetY = 8.dp,
                    blurRadius = 4.dp
                )
                .background(Color.White)
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Spread Shadow with Rounded Corners",
            modifier = Modifier
                .dropShadow(
                    color = Color.Cyan.copy(alpha = 0.7f),
                    borderRadius = 12.dp,
                    blurRadius = 16.dp, 
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    spread = 6.dp
                )
                .background(Color.White)
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Gradient Drop Shadow",
            modifier = Modifier
                .dropShadow(
                    gradientColors = listOf(Color.Red.copy(alpha = 0.7f), Color.Blue.copy(alpha = 0.7f)),
                    gradientStartFactorX = 0f,
                    gradientStartFactorY = 0f,
                    gradientEndFactorX = 1f,
                    gradientEndFactorY = 1f,
                    gradientTileMode = TileMode.Clamp,
                    borderRadius = 12.dp,
                    blurRadius = 12.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    spread = 2.dp,
                    alpha = 1.0f
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
