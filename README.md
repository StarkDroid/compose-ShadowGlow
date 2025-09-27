# ShadowGlow: Advanced Drop Shadows for Jetpack Compose

<!-- Banner Placeholder -->
![ShadowGlow Banner](static/banner.png)

[![Maven Central Version](https://img.shields.io/maven-central/v/me.trishiraj/shadowglow)](https://central.sonatype.com/artifact/me.trishiraj/shadowglow)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

ShadowGlow is a highly customizable Jetpack Compose library designed to elevate your UI by providing a versatile and easy-to-use `Modifier.shadowGlow()` extension. Create stunning depth, glow, and dynamic visual effects with features like gyroscope-driven parallax and subtle breathing animations.

<!-- Video Demo Placeholder -->
**Watch a Demo!**
[shadow-glow-sample.webm](https://github.com/user-attachments/assets/416bda7b-4651-4e1c-b2a1-d7a646af1dde)

---

## ✨ Features

*   **🎨 Solid & Gradient Shadows:** Apply shadows with solid colors or beautiful multi-stop linear gradients.
*   **📐 Shape Customization:** Control `borderRadius`, `blurRadius`, `offsetX`, `offsetY`, and `spread` for precise shadow appearances.
*   **🎭 Multiple Blur Styles:** Choose from `NORMAL`, `SOLID`, `OUTER`, and `INNER` blur styles, corresponding to Android's `BlurMaskFilter.Blur`.
*   **🌌 Gyroscope Parallax Effect:** Add a dynamic depth effect where the shadow subtly shifts based on device orientation.
*   **🌬️ Breathing Animation Effect:** Create an engaging pulsating effect by animating the shadow's blur radius.
*   **🚀 Easy to Use:** Apply complex shadows with a simple and fluent Modifier chain.
*   **💻 Compose Multiplatform Ready (Core Logic):** Designed with multiplatform principles in mind (platform-specific implementations for features like gyro would be needed).
*   **📱 Theme Friendly:** Works seamlessly with light and dark themes.

---

## 🌟 Why Choose ShadowGlow?

*   **Unparalleled Customization:** Fine-tune every aspect of your shadow to perfectly match your app's design language.
*   **Dynamic & Interactive:** Go beyond static shadows with unique parallax and breathing effects that respond to the user and device.
*   **Simplified API:** Achieve complex effects with an intuitive and developer-friendly API.
*   **Lightweight & Performant:** Built with performance considerations for smooth UI rendering.

---

## 🛠️ Installation

ShadowGlow is available on Maven Central. Add the dependency to your `build.gradle.kts` (or `build.gradle`) file:

**Kotlin DSL (`build.gradle.kts`):**

```kotlin
dependencies {
    implementation("me.trishiraj:shadowglow:1.0.0")
}
```

**Groovy DSL (`build.gradle`):**

```groovy
dependencies {
    implementation 'me.trishiraj:shadowglow:1.0.0'
}
```

---

## 🚀 Usage Guide

Applying a shadow is as simple as using the `Modifier.shadowGlow()` extension.

### Basic Solid Color Shadow

```kotlin
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.trishiraj.shadowGlow // Import the extension

// ...

Text(
    text = "Solid Shadow",
    modifier = Modifier
        .shadowGlow(
            color = Color.Black.copy(alpha = 0.5f),
            borderRadius = 8.dp,
            blurRadius = 10.dp,
            offsetX = 4.dp,
            offsetY = 4.dp
        )
        .background(Color.White) // Apply background to the composable itself
        .padding(16.dp)
)
```

### Basic Gradient Shadow

```kotlin
import me.trishiraj.shadowGlow
// Import the extension
// ... other necessary imports

Text(
    text = "Gradient Shadow",
    modifier = Modifier
        .shadowGlow(
            gradientColors = listOf(Color.Magenta.copy(alpha = 0.6f), Color.Blue.copy(alpha = 0.6f)),
            borderRadius = 12.dp,
            blurRadius = 16.dp,
            offsetY = 8.dp
        )
        .background(Color.White)
        .padding(16.dp)
)
```

---

## ⚙️ Customization Deep Dive

ShadowGlow offers a rich set of parameters to tailor your shadows:

### Common Parameters (for both solid and gradient shadows)

*   `borderRadius: Dp = 0.dp`: The corner radius for the shadow shape.
*   `blurRadius: Dp = 8.dp`: The primary blur radius for the shadow.
*   `offsetX: Dp = 0.dp`: Static horizontal offset of the shadow.
*   `offsetY: Dp = 4.dp`: Static vertical offset of the shadow.
*   `spread: Dp = 0.dp`: Expands or contracts the shadow's boundaries *before* blurring. Positive values expand.
*   `blurStyle: ShadowBlurStyle = ShadowBlurStyle.NORMAL`:
    *   `ShadowBlurStyle.NORMAL`: Standard blur.
    *   `ShadowBlurStyle.SOLID`: Solid, sharp-edged shadow (blur has no effect).
    *   `ShadowBlurStyle.OUTER`: Blurs only the outer edge of the shadow.
    *   `ShadowBlurStyle.INNER`: Blurs only the inner edge of the shadow (can create interesting inset effects if spread is negative).
*   `enableGyroParallax: Boolean = false`: Set to `true` to enable the gyroscope-driven parallax effect.
*   `parallaxSensitivity: Dp = 4.dp`: The maximum displacement (in Dp) for the parallax effect when the device is tilted.
*   `enableBreathingEffect: Boolean = false`: Set to `true` to enable the breathing (pulsating blur) animation.
*   `breathingEffectIntensity: Dp = 4.dp`: The maximum *additional* blur radius applied during one cycle of the breathing animation. The blur will animate between `blurRadius` and `blurRadius + breathingEffectIntensity`.
*   `breathingDurationMillis: Int = 1500`: The duration (in milliseconds) for one full cycle of the breathing animation (e.g., expand and contract).

### Solid Color Shadow Specific

*   `color: Color = Color.Black.copy(alpha = 0.4f)`: The solid color of the shadow.

### Gradient Shadow Specific

*   `gradientColors: List<Color>`: A list of colors to create the linear gradient.
*   `gradientStartFactorX: Float = 0f`: The X-coordinate start factor for the gradient (0.0 for left, 1.0 for right of the composable).
*   `gradientStartFactorY: Float = 0f`: The Y-coordinate start factor for the gradient (0.0 for top, 1.0 for bottom).
*   `gradientEndFactorX: Float = 1f`: The X-coordinate end factor for the gradient.
*   `gradientEndFactorY: Float = 1f`: The Y-coordinate end factor for the gradient.
*   `gradientColorStops: List<Float>? = null`: Optional list of relative positions (0.0 to 1.0) for each color in `gradientColors`. If null, colors are distributed evenly.
*   `gradientTileMode: TileMode = TileMode.Clamp`: How the gradient should be tiled if it doesn't fill the bounds (`Clamp`, `Repeated`, `Mirror`).
*   `alpha: Float = 1.0f`: Overall alpha transparency for the gradient shadow (0.0f to 1.0f).

### Example: Fully Featured Shadow

```kotlin
import me.trishiraj.shadowGlow
import me.trishiraj.shadowGlow.ShadowBlurStyle
// ... other necessary imports

Text(
    text = "Feature-Rich Shadow",
    modifier = Modifier
        .shadowGlow(
            color = Color.Green.copy(alpha = 0.7f),
            borderRadius = 20.dp,
            blurRadius = 12.dp,
            offsetX = (-4).dp,
            offsetY = 8.dp,
            spread = 2.dp,
            blurStyle = ShadowBlurStyle.OUTER,
            enableGyroParallax = true,
            parallaxSensitivity = 6.dp,
            enableBreathingEffect = true,
            breathingEffectIntensity = 5.dp,
            breathingDurationMillis = 2000
        )
        .background(Color.DarkGray)
        .padding(24.dp)
)
```

---

## 📚 API Reference

The primary API is the `Modifier.shadowGlow()` extension function. It has two overloads:

1.  `shadowGlow(color: Color, ...)`: For solid color shadows.
2.  `shadowGlow(gradientColors: List<Color>, ...)`: For gradient shadows.

Refer to the "Customization Deep Dive" section above for details on all available parameters and their default values. For more in-depth information, please refer to the KDoc comments within the source code.
Or simply refer to the sample app given within `app > src > ...MainActivity.kt`

---

## 🙌 Contributing

Contributions are welcome! Whether it's bug reports, feature requests, or pull requests, please feel free to contribute.

1.  Fork the repository.
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

---

## 📜 License

ShadowGlow is licensed under the Apache License, Version 2.0. See the [LICENSE](https://www.apache.org/licenses/LICENSE-2.0.txt) file for details.
