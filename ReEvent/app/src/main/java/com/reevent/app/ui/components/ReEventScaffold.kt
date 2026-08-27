package com.reevent.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.theme.ReEventBackground

@Composable
fun ReEventScaffold(
    selected: TopLevelDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    showBottomNavigation: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val usesRail = showBottomNavigation && selected != null && maxWidth >= 600.dp
        Row(Modifier.fillMaxSize()) {
            if (usesRail) {
                ReEventNavigationRail(
                    selected = requireNotNull(selected),
                    onNavigate = onNavigate,
                )
            }
            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = ReEventBackground,
                bottomBar = {
                    if (showBottomNavigation && !usesRail) {
                        selected?.let { ReEventBottomBar(selected = it, onNavigate = onNavigate) }
                    }
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ReEventBackground),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    content(innerPadding)
                }
            }
        }
    }
}

@Composable
fun ReEventLazyColumn(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    val width = currentWindowWidthDp()
    val horizontalPadding =
        when {
            width < 360.dp -> 12.dp
            width >= 840.dp -> 28.dp
            else -> ScreenPadding
        }

    LazyColumn(
        modifier =
            modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
        contentPadding =
            PaddingValues(
                start = horizontalPadding,
                top = paddingValues.calculateTopPadding() + 14.dp,
                end = horizontalPadding,
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}
