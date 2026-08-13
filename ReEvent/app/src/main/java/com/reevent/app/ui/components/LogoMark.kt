package com.reevent.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.reevent.app.R

@Composable
fun LogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
) {
    Image(
        painter = painterResource(R.drawable.reevent_logo),
        contentDescription = "ReEvent logo",
        modifier =
            modifier
                .size(size)
                .clip(RoundedCornerShape(size / 4)),
        contentScale = ContentScale.Crop,
    )
}
