package com.reevent.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.reevent.app.R
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.ResourceCardModel
import com.reevent.app.ui.ResourceTone
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.theme.ReEventTheme

private val previewResource =
    ResourceCardModel(
        title = "Display Stand Kit",
        owner = "School Open Day",
        category = "Exhibition",
        price = "RM 360",
        quantity = "10 stands",
        location = "Subang Jaya",
        impact = "Repairable minor dents",
        tone = ResourceTone.Repair,
        imageRes = R.drawable.resource_display_stand,
    )

@Preview(showBackground = true)
@Composable
private fun ButtonsPreview() {
    ReEventTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PrimaryActionButton(text = "Primary action", onClick = {})
            SecondaryActionButton(text = "Secondary action", onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReEventBottomBarPreview() {
    ReEventTheme {
        androidx.compose.runtime.CompositionLocalProvider(LocalUserRole provides UserRole.ORGANIZER) {
            ReEventBottomBar(selected = TopLevelDestination.HOME, onNavigate = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResourceCardPreview() {
    ReEventTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ResourceCard(item = previewResource, onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MetricCardPreview() {
    ReEventTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MetricCard(value = "83%", label = "Recovery rate", detail = "Target 90% by close-out")
        }
    }
}
