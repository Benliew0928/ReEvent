package com.reevent.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.reevent.app.ui.MockData
import com.reevent.app.ui.ReEventScreen
import com.reevent.app.ui.theme.ReEventTheme

@Preview(showBackground = true)
@Composable
fun ButtonsPreview() {
    ReEventTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrimaryActionButton(text = "Primary Action", onClick = {})
            SecondaryActionButton(text = "Secondary Action", onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReEventBottomBarPreview() {
    ReEventTheme {
        ReEventBottomBar(
            selected = ReEventScreen.Home,
            onNavigate = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResourceCardPreview() {
    ReEventTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ResourceCard(
                item = MockData.resources.first(),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MetricCardPreview() {
    ReEventTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MetricCard(
                value = MockData.metrics.first().value,
                label = MockData.metrics.first().label,
                detail = MockData.metrics.first().detail
            )
        }
    }
}
