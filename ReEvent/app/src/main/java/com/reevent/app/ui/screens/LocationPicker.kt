package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.reevent.app.BuildConfig
import com.reevent.app.core.data.AppResult
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.PlaceSuggestion
import com.reevent.app.core.network.MapTilerHttpConfiguration
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson

@Composable
fun LocationPickerDialog(
    initialLocation: GeoLocation?,
    onDismiss: () -> Unit,
    onSelected: (GeoLocation) -> Unit,
    search: suspend (String, GeoLocation?) -> AppResult<List<PlaceSuggestion>>,
    reverse: suspend (GeoLocation) -> AppResult<PlaceSuggestion>,
    modifier: Modifier = Modifier,
    initialQuery: String = "",
) {
    var query by rememberSaveable(initialLocation?.displayAddress, initialQuery) {
        mutableStateOf(initialLocation?.displayAddress ?: initialQuery)
    }
    var selected by remember(initialLocation) { mutableStateOf(initialLocation) }
    var suggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var reverseMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(query) {
        if (query.trim().length < 3 || query == selected?.displayAddress) {
            suggestions = emptyList()
            return@LaunchedEffect
        }
        delay(350)
        searching = true
        when (val result = search(query.trim(), selected)) {
            is AppResult.Success -> {
                suggestions = result.value.take(5)
                searchError = if (result.value.isEmpty()) "No Malaysian address matched this search." else null
            }
            is AppResult.Failure -> {
                suggestions = emptyList()
                searchError = "Address search is unavailable. Check your connection and try again."
            }
        }
        searching = false
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text("Choose exact location") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        searchError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search Malaysian addresses") },
                    supportingText = { Text("Search starts after 3 characters and a 350 ms pause.") },
                    trailingIcon = { if (searching) CircularProgressIndicator() },
                    singleLine = true,
                )
                suggestions.forEach { suggestion ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selected = suggestion.location
                            query = suggestion.label
                            suggestions = emptyList()
                            searchError = null
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ReEventLine),
                    ) {
                        Text(suggestion.label, Modifier.padding(10.dp))
                    }
                }
                searchError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                LocationPinMap(
                    location = selected,
                    onMovePin = { latitude, longitude ->
                        val adjusted = GeoLocation(
                            displayAddress = selected?.displayAddress ?: query.ifBlank { "Adjusted business point" },
                            latitude = latitude,
                            longitude = longitude,
                        )
                        selected = adjusted
                        reverseMessage = "Checking adjusted pin address…"
                        scope.launch {
                            when (val result = reverse(adjusted)) {
                                is AppResult.Success -> {
                                    val normalized = result.value.location.copy(displayAddress = result.value.label)
                                    selected = normalized
                                    query = normalized.displayAddress
                                    reverseMessage = null
                                }
                                is AppResult.Failure -> {
                                    reverseMessage = "The pin moved, but its address could not be refreshed. The selected label was preserved."
                                }
                            }
                        }
                    },
                )
                Text(
                    if (selected == null) "Select an address to place the pin." else "Long-press the map to move the exact business pin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ReEventTextSecondary,
                )
                reverseMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary) }
                selected?.let {
                    Text("${it.displayAddress}\n${"%.6f".format(it.latitude)}, ${"%.6f".format(it.longitude)}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { selected?.let(onSelected) }, enabled = selected != null) { Text("Use this point") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun LocationPinMap(
    location: GeoLocation?,
    onMovePin: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val key = BuildConfig.MAPTILER_API_KEY
    if (key.isBlank()) {
        Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
            Text("Map preview requires MAPTILER_API_KEY. Search results still provide validated coordinates.", Modifier.padding(12.dp))
        }
        return
    }
    val context = LocalContext.current
    val baseStyle = remember(context, key) {
        MapTilerHttpConfiguration.ensureInitialized(context)
        BaseStyle.Uri("https://api.maptiler.com/maps/streets-v2/style.json?key=$key")
    }
    val camera = rememberCameraState(
        CameraPosition(
            target = Position(location?.longitude ?: 101.9758, location?.latitude ?: 4.2105),
            zoom = if (location == null) 5.0 else 15.0,
        ),
    )
    LaunchedEffect(location?.latitude, location?.longitude) {
        if (location != null) camera.position = camera.position.copy(target = Position(location.longitude, location.latitude), zoom = 15.0)
    }
    val sourceJson = remember(location) {
        if (location == null) {
            "{\"type\":\"FeatureCollection\",\"features\":[]}"
        } else {
            FeatureCollection(
                listOf(
                    Feature(
                        id = JsonPrimitive("selected-location"),
                        geometry = Point(Position(location.longitude, location.latitude)),
                        properties = mapOf("kind" to JsonPrimitive("selected-location")),
                    ),
                ),
            ).toJson()
        }
    }
    Box(
        modifier = modifier.fillMaxWidth().height(260.dp).semantics {
            contentDescription = "Location pin map. Long press to move the pin."
        },
    ) {
        MaplibreMap(
            modifier = Modifier.fillMaxWidth().height(260.dp),
            baseStyle = baseStyle,
            cameraState = camera,
            onMapLongClick = { position, _ ->
                onMovePin(position.latitude, position.longitude)
                ClickResult.Consume
            },
        ) {
            // MapLibre sources are tied to the style composition supplied here.
            val source = rememberGeoJsonSource(GeoJsonData.JsonString(sourceJson))
            CircleLayer(
                id = "location-pin",
                source = source,
                radius = const(11.dp),
                color = const(ReEventGreen),
                strokeWidth = const(4.dp),
                strokeColor = const(Color.White),
            )
        }
        Text(
            "© MapTiler © OpenStreetMap",
            modifier = Modifier.align(Alignment.BottomStart).padding(5.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
