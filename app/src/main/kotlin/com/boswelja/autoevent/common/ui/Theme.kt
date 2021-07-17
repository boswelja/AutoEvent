package com.boswelja.autoevent.common.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DeepOrange200 = Color(0xffffab91)
private val DeepOrange500 = Color(0xffff5722)

private val DarkColors = darkColors(
    primary = DeepOrange200,
    primaryVariant = DeepOrange200,
    secondary = DeepOrange200,
    secondaryVariant = DeepOrange200
)
private val LightColors = lightColors(
    primary = DeepOrange500,
    primaryVariant = DeepOrange500,
    secondary = DeepOrange500,
    secondaryVariant = DeepOrange500
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors
    ) {
        val statusBarColor = MaterialTheme.colors.primary
        val navBarColor = MaterialTheme.colors.surface
        LaunchedEffect(darkTheme) {
            systemUiController.setStatusBarColor(statusBarColor)
            systemUiController.setNavigationBarColor(navBarColor)
        }
        content()
    }
}
