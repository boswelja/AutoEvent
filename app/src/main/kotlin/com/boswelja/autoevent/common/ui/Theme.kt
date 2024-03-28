package com.boswelja.autoevent.common.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

const val useMonet = true

val shapes = Shapes(
    small = RoundedCornerShape(50),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun getColors(darkTheme: Boolean): ColorScheme {
    val context = LocalContext.current
    return remember(darkTheme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else {
            if (darkTheme) {
                darkColorScheme()
            } else {
                lightColorScheme()
            }
        }
    }
}

private fun Context.getDarkPrimaryColor(): Color {
    return if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_accent1_200))
    } else {
        Color(0xffffab91)
    }
}

private fun Context.getLightPrimaryColor(): Color {
    return if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_accent1_500))
    } else {
        Color(0xffff5722)
    }
}

private fun Context.getLightBackgroundColor(): Color {
    return if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_50))
    } else {
        Color(0xFFFBE9E7)
    }
}

private fun Context.getLightSurfaceColor(): Color {
    return if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_10))
    } else {
        Color.White
    }
}

private fun Context.getDarkBackgroundColor(): Color {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_900))
    } else {
        Color(0xFF212121)
    }
}

private fun Context.getDarkSurfaceColor(): Color {
    return if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_900))
    } else {
        Color(0xFF212121)
    }
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = getColors(darkTheme = darkTheme),
        shapes = shapes,
        content = content
    )
}
