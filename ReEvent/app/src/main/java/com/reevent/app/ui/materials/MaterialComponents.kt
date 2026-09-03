package com.reevent.app.ui.materials

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMist
import com.reevent.app.ui.theme.HomePaper

@Composable
fun MaterialFamilyIcon(
    family: MaterialFamily,
    modifier: Modifier = Modifier,
    tint: Color = HomeForest,
    contentDescription: String? = "${family.displayLabel} material",
) {
    Canvas(
        modifier = modifier
            .size(32.dp)
            .then(if (contentDescription == null) Modifier else Modifier.semantics { this.contentDescription = contentDescription }),
    ) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * .075f, cap = StrokeCap.Round)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(tint, Offset(w * x1, h * y1), Offset(w * x2, h * y2), stroke.width, StrokeCap.Round)
        when (family) {
            MaterialFamily.WOOD -> {
                drawCircle(tint, w * .34f, center, style = stroke)
                drawCircle(tint, w * .20f, center, style = stroke)
                line(.50f, .16f, .69f, .02f); line(.50f, .84f, .74f, .98f)
            }
            MaterialFamily.TEXTILES -> {
                listOf(.24f, .50f, .76f).forEach { line(it, .14f, it, .86f); line(.14f, it, .86f, it) }
            }
            MaterialFamily.METAL -> {
                line(.22f, .18f, .78f, .18f); line(.50f, .18f, .50f, .82f); line(.22f, .82f, .78f, .82f)
            }
            MaterialFamily.PLASTIC -> {
                line(.42f, .10f, .58f, .10f); line(.42f, .18f, .58f, .18f)
                val p = Path().apply { moveTo(w*.42f,h*.18f); lineTo(w*.35f,h*.32f); lineTo(w*.32f,h*.84f); lineTo(w*.68f,h*.84f); lineTo(w*.65f,h*.32f); lineTo(w*.58f,h*.18f) }
                drawPath(p, tint, style = stroke)
            }
            MaterialFamily.PAPER_CARD -> {
                drawRect(tint, Offset(w*.20f,h*.25f), Size(w*.56f,h*.58f), style = stroke)
                drawRect(tint, Offset(w*.28f,h*.17f), Size(w*.56f,h*.58f), style = stroke)
            }
            MaterialFamily.GLASS -> {
                val p = Path().apply { moveTo(w*.28f,h*.14f); lineTo(w*.37f,h*.64f); quadraticTo(w*.50f,h*.77f,w*.63f,h*.64f); lineTo(w*.72f,h*.14f); close() }
                drawPath(p, tint, style = stroke); line(.50f,.72f,.50f,.88f); line(.32f,.88f,.68f,.88f)
            }
            MaterialFamily.CERAMIC -> {
                drawArc(tint, 0f, 180f, false, Offset(w*.17f,h*.28f), Size(w*.66f,h*.52f), style = stroke)
                line(.17f,.54f,.83f,.54f); line(.36f,.84f,.64f,.84f)
            }
            MaterialFamily.ELECTRICAL_ELECTRONICS -> {
                drawRect(tint, Offset(w*.18f,h*.26f), Size(w*.46f,h*.46f), style = stroke)
                line(.64f,.40f,.84f,.40f); line(.64f,.58f,.84f,.58f); line(.28f,.17f,.28f,.26f); line(.48f,.17f,.48f,.26f)
            }
            MaterialFamily.ORGANIC -> {
                line(.50f,.86f,.50f,.34f)
                drawOval(tint, Offset(w*.18f,h*.22f), Size(w*.33f,h*.25f), style = stroke)
                drawOval(tint, Offset(w*.49f,h*.10f), Size(w*.34f,h*.27f), style = stroke)
            }
            MaterialFamily.RUBBER -> {
                drawCircle(tint, w*.34f, center, style = stroke); drawCircle(tint, w*.17f, center, style = stroke)
                repeat(6) { index ->
                    val angle = Math.toRadians((index * 60.0)); val c = center
                    val a = Offset(c.x + kotlin.math.cos(angle).toFloat()*w*.34f, c.y + kotlin.math.sin(angle).toFloat()*w*.34f)
                    val b = Offset(c.x + kotlin.math.cos(angle).toFloat()*w*.43f, c.y + kotlin.math.sin(angle).toFloat()*w*.43f)
                    drawLine(tint, a, b, stroke.width, StrokeCap.Round)
                }
            }
            MaterialFamily.MIXED_OTHER -> {
                drawCircle(tint, w*.18f, Offset(w*.30f,h*.34f), style = stroke)
                drawRect(tint, Offset(w*.49f,h*.18f), Size(w*.31f,h*.31f), style = stroke)
                val p = Path().apply { moveTo(w*.22f,h*.82f); lineTo(w*.50f,h*.50f); lineTo(w*.78f,h*.82f); close() }
                drawPath(p, tint, style = stroke)
            }
        }
    }
}

@Composable
fun MaterialFamilyPickerField(
    selected: MaterialFamily?,
    onSelected: (MaterialFamily?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Material family",
    allowAny: Boolean = false,
) {
    var open by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth().clickable { open = true },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, HomeLine),
        color = Color.Transparent,
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            selected?.let { MaterialFamilyIcon(it, Modifier.size(28.dp), contentDescription = null) }
            Column(Modifier.weight(1f)) {
                Text(label, color = HomeForest)
                Text(selected?.displayLabel ?: if (allowAny) "Any material" else "Choose a family", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
    if (open) {
        MaterialFamilyPickerDialog(selected, onDismiss = { open = false }, onSelected = { onSelected(it); open = false }, allowAny = allowAny)
    }
}

@Composable
fun MaterialFamilyMultiSelectField(
    selected: Set<MaterialFamily>,
    onSelected: (Set<MaterialFamily>) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Accepted material families",
) {
    var open by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth().clickable { open = true },
        shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, HomeLine), color = Color.Transparent,
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = HomeForest)
            Text(if (selected.isEmpty()) "Any material family" else selected.sortedBy(MaterialFamily::ordinal).joinToString { it.displayLabel })
        }
    }
    if (open) {
        var draft by remember(selected) { mutableStateOf(selected) }
        AlertDialog(
            onDismissRequest = { open = false },
            title = {
                Text(
                    "Accepted families",
                    style = HomeCardTitleStyle.copy(fontSize = 28.sp),
                    color = HomeInk,
                )
            },
            text = { FamilyGrid(draft, { family -> draft = if (family in draft) draft - family else draft + family }, Modifier.heightIn(max = 440.dp)) },
            confirmButton = { TextButton(onClick = { onSelected(draft); open = false }) { Text("Apply") } },
            dismissButton = { TextButton(onClick = { draft = emptySet() }) { Text("Accept any") } },
            containerColor = HomePaper,
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun MaterialFamilyPickerDialog(
    selected: MaterialFamily?,
    onDismiss: () -> Unit,
    onSelected: (MaterialFamily?) -> Unit,
    allowAny: Boolean,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val families = MaterialFamily.entries.filter { it.displayLabel.contains(query.trim(), ignoreCase = true) }
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text("All materials", style = HomeCardTitleStyle.copy(fontSize = 28.sp), color = HomeInk) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(query, { query = it.take(60) }, Modifier.fillMaxWidth(), label = { Text("Search families") }, singleLine = true)
                if (allowAny) TextButton(onClick = { onSelected(null) }) { Text("Any material") }
                FamilyGrid(selected?.let(::setOf).orEmpty(), { onSelected(it) }, Modifier.heightIn(max = 420.dp), families)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        containerColor = HomePaper,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun FamilyGrid(
    selected: Set<MaterialFamily>,
    onToggle: (MaterialFamily) -> Unit,
    modifier: Modifier = Modifier,
    families: List<MaterialFamily> = MaterialFamily.entries,
) {
    LazyVerticalGrid(modifier = modifier, columns = GridCells.Adaptive(112.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(families, key = MaterialFamily::name) { family ->
            val isSelected = family in selected
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("material_family_${family.name}")
                    .clickable { onToggle(family) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) HomeForest else HomeMist,
            ) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    MaterialFamilyIcon(family, tint = if (isSelected) Color.White else HomeForest)
                    Text(family.displayLabel, color = if (isSelected) Color.White else HomeForest, maxLines = 2)
                }
            }
        }
    }
}
