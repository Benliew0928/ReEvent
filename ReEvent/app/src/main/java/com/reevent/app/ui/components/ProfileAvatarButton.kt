package com.reevent.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reevent.app.ui.theme.HomeEditorialFont
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeSage

/** The shared profile control used by editorial page headers. */
@Composable
fun ProfileAvatarButton(
    displayName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initials =
        displayName
            .trim()
            .split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "ME" }

    Surface(
        onClick = onClick,
        modifier = modifier.size(54.dp),
        shape = CircleShape,
        color = HomeSage,
        tonalElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                color = HomeInk,
                fontFamily = HomeEditorialFont,
                fontSize = 22.sp,
                modifier = Modifier.semantics {
                    contentDescription = "Profile for ${displayName.ifBlank { "signed-in user" }}"
                },
            )
        }
    }
}
