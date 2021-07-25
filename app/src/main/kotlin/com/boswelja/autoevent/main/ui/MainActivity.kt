package com.boswelja.autoevent.main.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.autoevent.R
import com.boswelja.autoevent.common.ui.AppTheme

class MainActivity : AppCompatActivity() {

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_name)) },
                            backgroundColor = Color.Transparent,
                            contentColor = MaterialTheme.colors.onBackground,
                            elevation = 0.dp
                        )
                    }
                ) {
                    MainScreen(
                        modifier = Modifier
                            .padding(it)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
