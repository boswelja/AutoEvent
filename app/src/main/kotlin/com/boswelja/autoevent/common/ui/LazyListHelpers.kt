package com.boswelja.autoevent.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@ExperimentalAnimationApi
@Composable
fun <T> LazyItemScope.AnimatedVisibilityItem(
    modifier: Modifier = Modifier,
    exitAnim: ExitTransition = fadeOut() + shrinkVertically(),
    remove: Boolean,
    item: T,
    onItemRemoved: (T) -> Unit,
    content: @Composable LazyItemScope.(T) -> Unit
) {
    val visible = remember {
        MutableTransitionState(true)
    }

    if (remove && visible.isIdle && !visible.currentState) {
        onItemRemoved(item)
    }

    AnimatedVisibility(
        modifier = modifier,
        visibleState = visible,
        exit = exitAnim
    ) {
        content(item)
    }

    LaunchedEffect(key1 = remove) {
        visible.targetState = !remove
    }
}
