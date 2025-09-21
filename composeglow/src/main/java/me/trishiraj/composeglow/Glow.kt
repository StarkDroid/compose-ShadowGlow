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
    val parallaxState = if (enableGyroParallax) rememberGyroParallaxState(parallaxSensitivity) else null

    this.then(
        Modifier.drawBehind {
            val spreadPx = spread.toPx()
            val blurRadiusPx = blurRadius.toPx()
            val baseOffsetXPx = offsetX.toPx()
            val baseOffsetYPx = offsetY.toPx()
            val shadowBorderRadiusPx = borderRadius.toPx()

            val dynamicOffsetXPx = parallaxState?.value?.first ?: 0f
            val dynamicOffsetYPx = parallaxState?.value?.second ?: 0f

            val totalOffsetXPx = baseOffsetXPx + dynamicOffsetXPx
            val totalOffsetYPx = baseOffsetYPx + dynamicOffsetYPx

            val shadowColorArgb = color.toArgb()

            if (color.alpha == 0f && blurRadiusPx == 0f && spreadPx == 0f && dynamicOffsetXPx == 0f && dynamicOffsetYPx == 0f && baseOffsetXPx == 0f && baseOffsetYPx == 0f) {
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
    val parallaxState = if (enableGyroParallax) rememberGyroParallaxState(parallaxSensitivity) else null

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

            val dynamicOffsetXPx = parallaxState?.value?.first ?: 0f
            val dynamicOffsetYPx = parallaxState?.value?.second ?: 0f

            val totalOffsetXPx = baseOffsetXPx + dynamicOffsetXPx
            val totalOffsetYPx = baseOffsetYPx + dynamicOffsetYPx

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
private fun rememberGyroParallaxState(sensitivity: Dp): State<Pair<Float, Float>> {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sensitivityPx = remember(sensitivity) { with(density) { sensitivity.toPx() } }

    val parallaxOffset = remember { mutableStateOf(0f to 0f) }
    val baselineOrientation = remember { mutableStateOf<FloatArray?>(null) } // Stores baseline [pitch, roll]

    DisposableEffect(context, sensitivityPx) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        var sensorEventListener: SensorEventListener? = null

        if (rotationSensor != null) {
            sensorEventListener = object : SensorEventListener {
                private val rotationMatrix = FloatArray(9)
                private val currentOrientationAngles = FloatArray(3) // For current pitch, roll, azimuth

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, currentOrientationAngles)

                        val currentPitch = currentOrientationAngles[1]
                        val currentRoll = currentOrientationAngles[2]

                        if (baselineOrientation.value == null) {
                            // Capture baseline on first valid sensor event
                            baselineOrientation.value = floatArrayOf(currentPitch, currentRoll)
                        } else {
                            val basePitch = baselineOrientation.value!![0]
                            val baseRoll = baselineOrientation.value!![1]

                            val deltaPitch = currentPitch - basePitch
                            val deltaRoll = currentRoll - baseRoll

                            val newOffsetX = -sin(deltaRoll) * sensitivityPx
                            val newOffsetY = sin(deltaPitch) * sensitivityPx
                            parallaxOffset.value = newOffsetX to newOffsetY
                        }
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
             // No rotation vector sensor, reset baseline if it was somehow set previously
            baselineOrientation.value = null
        }

        onDispose {
            sensorEventListener?.let {
                sensorManager.unregisterListener(it)
            }
            parallaxOffset.value = 0f to 0f // Reset offset
            baselineOrientation.value = null // Reset baseline
            object : DisposableEffectResult {
                override fun dispose() {}
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
