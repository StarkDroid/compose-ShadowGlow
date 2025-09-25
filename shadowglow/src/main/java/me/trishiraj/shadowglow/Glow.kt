package me.trishiraj.shadowglow

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.LinearGradient as AndroidLinearGradient
import android.graphics.Paint as AndroidPaint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toAndroidTileMode
import androidx.compose.ui.graphics.toArgb
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

@Composable
private fun rememberAnimatedBreathingValue(
    enabled: Boolean,
    intensity: Dp,
    durationMillis: Int
): State<Float> {
    val density = LocalDensity.current
    val intensityPx = remember(intensity) { with(density) { intensity.toPx() } }

    if (!enabled || intensityPx <= 0f || durationMillis <= 0) {
        return remember { mutableFloatStateOf(0f) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathingEffect")
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = intensityPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingValue"
    )
}

/**
 * Applies a drop shadow effect to the composable using a solid color.
 *
 * @param color The color of the shadow.
 * @param borderRadius The radius of the shadow's corners.
 * @param blurRadius The base blur radius of the shadow.
 * @param offsetX The static horizontal offset of the shadow.
 * @param offsetY The static vertical offset of the shadow.
 * @param spread The amount to expand the shadow's bounds before blurring.
 * @param blurStyle The style of the blur effect.
 * @param enableGyroParallax If true, enables a parallax effect on the shadow based on device orientation.
 * @param parallaxSensitivity The maximum displacement for the gyroscope-driven parallax effect.
 * @param enableBreathingEffect If true, enables a breathing (pulsating) animation on the shadow's blur radius.
 * @param breathingEffectIntensity The maximum additional blur radius applied during the breathing animation.
 * @param breathingDurationMillis The duration for one full cycle of the breathing animation.
 * @return A [Modifier] that applies the drop shadow effect.
 */
fun Modifier.shadowGlow(
    color: Color = Color.Black.copy(alpha = 0.4f),
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp,
    spread: Dp = 0.dp,
    blurStyle: ShadowBlurStyle = ShadowBlurStyle.NORMAL,
    enableGyroParallax: Boolean = false,
    parallaxSensitivity: Dp = 4.dp,
    enableBreathingEffect: Boolean = false,
    breathingEffectIntensity: Dp = 4.dp,
    breathingDurationMillis: Int = 1500
): Modifier = composed {
    val parallaxState = if (enableGyroParallax) rememberGyroParallaxState(parallaxSensitivity) else null
    val animatedBreathingValuePx = rememberAnimatedBreathingValue(
        enabled = enableBreathingEffect,
        intensity = breathingEffectIntensity,
        durationMillis = breathingDurationMillis
    )

    this.then(
        Modifier.drawBehind {
            val spreadPx = spread.toPx()
            val baseBlurRadiusPx = blurRadius.toPx()
            val currentAnimatedBlurPx = animatedBreathingValuePx.value
            val totalBlurRadiusPx = (baseBlurRadiusPx + currentAnimatedBlurPx).coerceAtLeast(0f)

            val baseOffsetXPx = offsetX.toPx()
            val baseOffsetYPx = offsetY.toPx()
            val shadowBorderRadiusPx = borderRadius.toPx()

            val dynamicOffsetXPx = parallaxState?.value?.first ?: 0f
            val dynamicOffsetYPx = parallaxState?.value?.second ?: 0f

            val totalOffsetXPx = baseOffsetXPx + dynamicOffsetXPx
            val totalOffsetYPx = baseOffsetYPx + dynamicOffsetYPx

            val shadowColorArgb = color.toArgb()

            if (color.alpha == 0f && totalBlurRadiusPx <= 0f && spreadPx == 0f && dynamicOffsetXPx == 0f && dynamicOffsetYPx == 0f && baseOffsetXPx == 0f && baseOffsetYPx == 0f) {
                return@drawBehind
            }

            val frameworkPaint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.FILL
                this.color = shadowColorArgb
                if (totalBlurRadiusPx > 0f) {
                    maskFilter = BlurMaskFilter(totalBlurRadiusPx, blurStyle.toAndroidBlurStyle())
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
 * (Gyro parallax and breathing effect parameters documented as above)
 */
fun Modifier.shadowGlow(
    gradientColors: List<Color>,
    gradientStartFactorX: Float = 0f,
    gradientStartFactorY: Float = 0f,
    gradientEndFactorX: Float = 1f,
    gradientEndFactorY: Float = 1f,
    gradientColorStops: List<Float>? = null,
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp,
    spread: Dp = 0.dp,
    alpha: Float = 1.0f,
    blurStyle: ShadowBlurStyle = ShadowBlurStyle.NORMAL,
    enableGyroParallax: Boolean = false,
    parallaxSensitivity: Dp = 4.dp,
    enableBreathingEffect: Boolean = false,
    breathingEffectIntensity: Dp = 4.dp,
    breathingDurationMillis: Int = 1500
): Modifier = composed {
    val parallaxState = if (enableGyroParallax) rememberGyroParallaxState(parallaxSensitivity) else null
    val animatedBreathingValuePx = rememberAnimatedBreathingValue(
        enabled = enableBreathingEffect,
        intensity = breathingEffectIntensity,
        durationMillis = breathingDurationMillis
    )

    this.then(
        Modifier.drawBehind {
            if (gradientColors.isEmpty() || alpha == 0f) {
                return@drawBehind
            }
            val spreadPx = spread.toPx()
            val baseBlurRadiusPx = blurRadius.toPx()
            val currentAnimatedBlurPx = animatedBreathingValuePx.value
            val totalBlurRadiusPx = (baseBlurRadiusPx + currentAnimatedBlurPx).coerceAtLeast(0f)
            
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
                    TileMode.Clamp.toAndroidTileMode()
                )
                if (totalBlurRadiusPx > 0f) {
                    maskFilter = BlurMaskFilter(totalBlurRadiusPx, blurStyle.toAndroidBlurStyle())
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
    val baselineOrientation = remember { mutableStateOf<FloatArray?>(null) }

    DisposableEffect(context, sensitivityPx) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        var sensorEventListener: SensorEventListener? = null

        if (rotationSensor != null) {
            sensorEventListener = object : SensorEventListener {
                private val rotationMatrix = FloatArray(9)
                private val currentOrientationAngles = FloatArray(3)

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, currentOrientationAngles)

                        val currentPitch = currentOrientationAngles[1]
                        val currentRoll = currentOrientationAngles[2]

                        if (baselineOrientation.value == null) {
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
            baselineOrientation.value = null
        }

        onDispose {
            sensorEventListener?.let {
                sensorManager.unregisterListener(it)
            }
            parallaxOffset.value = 0f to 0f
            baselineOrientation.value = null
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
