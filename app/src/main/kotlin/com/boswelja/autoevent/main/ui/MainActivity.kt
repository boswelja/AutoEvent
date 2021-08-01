package com.boswelja.autoevent.main.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.boswelja.autoevent.R
import com.boswelja.autoevent.common.ui.AppTheme
import com.boswelja.autoevent.notificationeventextractor.ui.BlocklistScreen
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding

class MainActivity : AppCompatActivity() {

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val currentDestination by navController.currentBackStackEntryAsState()
            val navigateUpVisible = remember(currentDestination) {
                val route = currentDestination?.destination?.route
                route != null && route != Destinations.HOME.name
            }

            AppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            modifier = Modifier.statusBarsPadding(),
                            title = { Text(stringResource(R.string.app_name)) },
                            navigationIcon = {
                                AnimatedVisibility(
                                    visible = navigateUpVisible,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    IconButton(
                                        onClick = { navController.navigate(Destinations.HOME.name) }
                                    ) {
                                        Icon(Icons.Default.ArrowBack, null)
                                    }
                                }
                            },
                            backgroundColor = Color.Transparent,
                            contentColor = MaterialTheme.colors.onBackground,
                            elevation = 0.dp
                        )
                    }
                ) {
                    NavigationScreen(
                        contentPadding = rememberInsetsPaddingValues(
                            insets = LocalWindowInsets.current.systemBars,
                            applyTop = false,
                            applyBottom = true,
                            additionalBottom = 16.dp,
                            additionalEnd = 16.dp,
                            additionalStart = 16.dp,
                            additionalTop = 16.dp
                        ),
                        navController = navController
                    )
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun NavigationScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    navController: NavHostController
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Destinations.HOME.name
    ) {
        composable(Destinations.HOME.name) {
            MainScreen(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                onNavigate = { navController.navigate(it.name) }
            )
        }
        composable(Destinations.BLOCKLIST.name) {
            BlocklistScreen(
                modifier = Modifier.fillMaxSize().padding(contentPadding)
            )
        }
    }
}
