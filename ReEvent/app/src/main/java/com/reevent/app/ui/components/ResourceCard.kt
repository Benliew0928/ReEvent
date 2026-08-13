package com.reevent.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reevent.app.ui.ResourceCardModel
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary

@Composable
fun ResourceCard(
    item: ResourceCardModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val compact = resourceCardLayoutForWidth(currentWindowWidthDp().value) == ResourceCardLayout.COMPACT
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = ReEventSurface,
        border = BorderStroke(1.dp, ReEventLine),
        tonalElevation = 0.dp,
    ) {
        if (compact) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ResourceCardImage(
                    item = item,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(154.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ReEventMintSoft),
                )
                ResourceCardDetails(item)
            }
        } else {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ResourceCardImage(
                    item = item,
                    modifier =
                        Modifier
                            .size(106.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ReEventMintSoft),
                )
                Spacer(Modifier.width(14.dp))
                ResourceCardDetails(item, Modifier.weight(1f))
            }
        }
    }
}

internal enum class ResourceCardLayout { COMPACT, EXPANDED }

internal fun resourceCardLayoutForWidth(widthDp: Float): ResourceCardLayout =
    if (widthDp < 360f) ResourceCardLayout.COMPACT else ResourceCardLayout.EXPANDED

@Composable
private fun ResourceCardImage(
    item: ResourceCardModel,
    modifier: Modifier,
) {
    ResourcePhotoImage(item.photoPath, item.imageRes, item.title, modifier)
}

@Composable
private fun ResourceCardDetails(
    item: ResourceCardModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusChip(text = item.tone.label, color = item.tone.color)
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.category,
                style = MaterialTheme.typography.labelMedium,
                color = ReEventTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(9.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            color = ReEventInk,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = item.owner,
            style = MaterialTheme.typography.bodyMedium,
            color = ReEventTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.price,
                style = MaterialTheme.typography.titleMedium,
                color = ReEventGreenDeep,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.quantity,
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventTextSecondary,
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = item.impact,
            style = MaterialTheme.typography.labelMedium,
            color = ReEventBlue,
        )
    }
}
