package me.trishiraj.composeglow

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.graphics.BlurMaskFilter
import android.graphics.Paint as AndroidPaint
import android.graphics.Shader as AndroidShader
import android.graphics.LinearGradient as AndroidLinearGradient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Defines the style of the blur effect for the shadow, corresponding to `BlurMaskFilter.Blur`.
 */
enum class ShadowBlurStyle {
    NORMAL, SOLID, OUTER, INNER
}

/** Converts [ShadowBlurStyle] to the Android-specific `BlurMaskFilter.Blur`. */
internal fun ShadowBlurStyle.toAndroidBlurStyle(): BlurMaskFilter.Blur {
    return when (this) {
        ShadowBlurStyle.NORMAL -> BlurMaskFilter.Blur.NORMAL
        ShadowBlurStyle.SOLID -> BlurMaskFilter.Blur.SOLID
        ShadowBlurStyle.OUTER -> BlurMaskFilter.Blur.OUTER
        ShadowBlurStyle.INNER -> BlurMaskFilter.Blur.INNER
    }
}

/**
 * Applies a drop shadow effect to the composable using a solid color.
 *
 * @param color The color of the shadow.
 * @param borderRadius The radius of the shadow's corners.
 * @param blurRadius The blur radius of the shadow.
 * @param offsetX The static horizontal offset of the shadow.
 * @param offsetY The static vertical offset of the shadow.
 * @param spread The amount to expand the shadow's bounds before blurring.
 * @param blurStyle The style of the blur effect.
 * @param enableGyroParallax If true, enables a parallax effect on the shadow based on device orientation.
 * @param parallaxSensitivity The maximum displacement for the gyroscope-driven parallax effect.
 * @return A [Modifier] that applies the drop shadow effect.
 */
fun Modifier.dropShadow(
    color: Color = Color.Black.copy(alpha = 0.4f),
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp,
    spread: Dp = 0.dp,
    blurStyle: ShadowBlurStyle = ShadowBlurStyle.NORMAL,
    enableGyroParallax: Boolean = false,
    parallaxSensitivity: Dp = 4.dp
): Modifier = composed {
    val parallaxOffset = if (enableGyroParallax) rememberGyroParallaxOffset(parallaxSensitivity) else null

    this.then(
        Modifier.drawBehind {
            val spreadPx = spread.toPx()
            val blurRadiusPx = blurRadius.toPx()
            val baseOffsetXPx = offsetX.toPx()
            val baseOffsetYPx = offsetY.toPx()
            val shadowBorderRadiusPx = borderRadius.toPx()

            val totalOffsetXPx = baseOffsetXPx + (parallaxOffset?.value?.first ?: 0f)
            val totalOffsetYPx = baseOffsetYPx + (parallaxOffset?.value?.second ?: 0f)

            val shadowColorArgb = color.toArgb()

            if (color.alpha == 0f && blurRadiusPx == 0f && spreadPx == 0f && totalOffsetXPx == baseOffsetXPx && totalOffsetYPx == baseOffsetYPx) {
                return@drawBehind
            }

            val frameworkPaint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.FILL
                this.color = shadowColorArgb
                if (blurRadiusPx > 0f) {
                    maskFilter = BlurMaskFilter(blurRadiusPx, blurStyle.toAndroidBlurStyle())
                }
            }
            val left = -spreadPx + totalOffsetXPx
            val top = -spreadPx + totalOffsetYPx
            val right = size.width + spreadPx + totalOffsetXPx
            val bottom = size.height + spreadPx + totalOffsetYPx

            drawShadowShape(left, top, right, bottom, shadowBorderRadiusPx, frameworkPaint)
        }
    )
}

/**
 * Applies a drop shadow effect to the composable using a linear gradient.
 * (Gyro parallax parameters documented as above)
 */
fun Modifier.dropShadow(
    gradientColors: List<Color>,
    gradientStartFactorX: Float = 0f,
    gradientStartFactorY: Float = 0f,
    gradientEndFactorX: Float = 1f,
    gradientEndFactorY: Float = 1f,
    gradientColorStops: List<Float>? = null,
    gradientTileMode: TileMode = TileMode.Clamp,
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp,
    spread: Dp = 0.dp,
    alpha: Float = 1.0f,
    blurStyle: ShadowBlurStyle = ShadowBlurStyle.NORMAL,
    enableGyroParallax: Boolean = false,
    parallaxSensitivity: Dp = 4.dp
): Modifier = composed {
    val parallaxOffset = if (enableGyroParallax) rememberGyroParallaxOffset(parallaxSensitivity) else null

    this.then(
        Modifier.drawBehind {
            if (gradientColors.isEmpty() || alpha == 0f) {
                return@drawBehind
            }
            val spreadPx = spread.toPx()
            val blurRadiusPx = blurRadius.toPx()
            val baseOffsetXPx = offsetX.toPx()
            val baseOffsetYPx = offsetY.toPx()
            val shadowBorderRadiusPx = borderRadius.toPx()

            val totalOffsetXPx = baseOffsetXPx + (parallaxOffset?.value?.first ?: 0f)
            val totalOffsetYPx = baseOffsetYPx + (parallaxOffset?.value?.second ?: 0f)

            val actualStartX = gradientStartFactorX * size.width
            val actualStartY = gradientStartFactorY * size.height
            val actualEndX = gradientEndFactorX * size.width
            val actualEndY = gradientEndFactorY * size.height

            val frameworkPaint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.FILL
                this.alpha = (alpha.coerceIn(0f, 1f) * 255).toInt()
                shader = AndroidLinearGradient(
                    actualStartX, actualStartY, actualEndX, actualEndY,
                    gradientColors.map { it.toArgb() }.toIntArray(),
                    gradientColorStops?.toFloatArray(),
                    gradientTileMode.toAndroidTileMode()
                )
                if (blurRadiusPx > 0f) {
                    maskFilter = BlurMaskFilter(blurRadiusPx, blurStyle.toAndroidBlurStyle())
                }
            }
            val left = -spreadPx + totalOffsetXPx
            val top = -spreadPx + totalOffsetYPx
            val right = size.width + spreadPx + totalOffsetXPx
            val bottom = size.height + spreadPx + totalOffsetYPx

            drawShadowShape(left, top, right, bottom, shadowBorderRadiusPx, frameworkPaint)
        }
    )
}

@Composable
private fun rememberGyroParallaxOffset(sensitivity: Dp): State<Pair<Float, Float>> {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sensitivityPx = remember(sensitivity) { with(density) { sensitivity.toPx() } }

    val parallaxOffset = remember {
        mutableStateOf(0f to 0f) // Pair<offsetX, offsetY>
    }

    DisposableEffect(context, sensitivityPx) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor == null) {
            onDispose {
                object : DisposableEffectResult {
                    override fun dispose() {}
                }
            }
            // return@DisposableEffect is not strictly needed here if onDispose is the last statement,
            // but kept for clarity that no further setup for this effect will occur.
            // However, the above onDispose block already correctly returns DisposableEffectResult.
        } else {
            val sensorListener = object : SensorEventListener {
                private val rotationMatrix = FloatArray(9)
                private val orientationAngles = FloatArray(3)

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)

                        val roll = orientationAngles[2]
                        val pitch = orientationAngles[1]

                        val newOffsetX = -sin(roll) * sensitivityPx
                        val newOffsetY = sin(pitch) * sensitivityPx
                        parallaxOffset.value = newOffsetX to newOffsetY
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)

            onDispose {
                sensorManager.unregisterListener(sensorListener)
                parallaxOffset.value = 0f to 0f
                object : DisposableEffectResult {
                    override fun dispose() {}
                }
            }
        }
    }
    return parallaxOffset
}

private fun DrawScope.drawShadowShape(left: Float, top: Float, right: Float, bottom: Float, cornerRadiusPx: Float, paint: AndroidPaint) {
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawRoundRect(left, top, right, bottom, cornerRadiusPx, cornerRadiusPx, paint)
    }
}

private fun TileMode.toAndroidTileMode(): AndroidShader.TileMode {
    return when (this) {
        TileMode.Clamp -> AndroidShader.TileMode.CLAMP
        TileMode.Repeated -> AndroidShader.TileMode.REPEAT
        TileMode.Mirror -> AndroidShader.TileMode.MIRROR
        else -> AndroidShader.TileMode.CLAMP
    }
}
