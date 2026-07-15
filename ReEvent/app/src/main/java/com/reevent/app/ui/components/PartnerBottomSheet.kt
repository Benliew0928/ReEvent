package com.reevent.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reevent.app.ui.PartnerMatch
import com.reevent.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerBottomSheet(
    partner: PartnerMatch?,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    if (partner == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ReEventSurface,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = ScreenPadding)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(Space16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = partner.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = ReEventInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(Space4))
                    Text(
                        text = partner.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReEventTextSecondary
                    )
                }
                StatusChip(text = partner.tone.label, color = partner.tone.color)
            }

            Text(
                text = partner.detail,
                style = MaterialTheme.typography.bodyLarge,
                color = ReEventTextSecondary
            )

            HorizontalDivider(color = ReEventLine)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space16)
            ) {
                InfoRow(label = "Match Score", value = partner.score, modifier = Modifier.weight(1f))
                InfoRow(label = "Distance", value = partner.distance, modifier = Modifier.weight(1f))
            }

            InfoRow(label = "Current Offer", value = partner.offer)

            Spacer(Modifier.height(Space8))

            PrimaryActionButton(
                text = "Route to Partner",
                onClick = {
                    onDismiss()
                    onAccept()
                }
            )
        }
    }
}
