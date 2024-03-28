package com.boswelja.autoevent.main.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.boswelja.autoevent.R
import com.boswelja.autoevent.common.ui.AppTheme
import com.boswelja.autoevent.notificationeventextractor.ui.BlocklistScreen

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

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
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                                    }
                                }
                            }
                        )
                    }
                ) {
                    NavigationScreen(
                        modifier = Modifier,
                        contentPadding = it,
                        navController = navController
                    )
                }
            }
        }
    }
}

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
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
            )
        }
    }
}
