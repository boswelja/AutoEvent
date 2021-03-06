package com.boswelja.autoevent.common.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController

const val useMonet = true

val shapes = Shapes(
    small = RoundedCornerShape(50),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun getColors(darkTheme: Boolean): Colors {
    val context = LocalContext.current
    return remember(darkTheme) {
        if (darkTheme) {
            val darkPrimary = context.getDarkPrimaryColor()
            darkColors(
                primary = darkPrimary,
                primaryVariant = darkPrimary,
                secondary = darkPrimary,
                secondaryVariant = darkPrimary,
                background = context.getDarkBackgroundColor(),
                surface = context.getDarkSurfaceColor()
            )
        } else {
            val lightPrimary = context.getLightPrimaryColor()
            lightColors(
                primary = lightPrimary,
                primaryVariant = lightPrimary,
                secondary = lightPrimary,
                secondaryVariant = lightPrimary,
                background = context.getLightBackgroundColor(),
                surface = context.getLightSurfaceColor()
            )
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
    val systemUiController = rememberSystemUiController()

    MaterialTheme(
        colors = getColors(darkTheme = darkTheme),
        shapes = shapes
    ) {
        ProvideWindowInsets {
            val darkIcons = MaterialTheme.colors.background.luminance() > 0.5f
            LaunchedEffect(darkTheme) {
                systemUiController.setSystemBarsColor(Color.Transparent, darkIcons)
            }
            content()
        }
    }
}
