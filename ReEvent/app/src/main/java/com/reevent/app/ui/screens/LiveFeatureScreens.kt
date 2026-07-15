package com.reevent.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.User
import com.reevent.app.core.data.ProgrammeMatcher
import com.reevent.app.ui.components.LogoMark
import com.reevent.app.ui.theme.ReEventBackground
import java.util.UUID

@Composable
fun OrganizerHomeLiveScreen(user: User, onAddResource: (String) -> Unit, onPassport: (String) -> Unit, onImpact: (String) -> Unit, onProfile: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val events by viewModel.events(user.id).collectAsState(emptyList())
    FeatureScaffold("Organiser workspace", "Account", onProfile, viewModel) {
        if (events.isEmpty()) {
            item {
                EmptyPanel("Create your first event", "Events organise the resources, handovers and impact you track.") {
                    viewModel.createEvent(user) { onAddResource(it.id) }
                }
            }
        } else {
            items(events, key = Event::id) { event ->
                EventCard(event, onAddResource, onImpact)
            }
        }
    }
}

@Composable
fun AddResourceLiveScreen(user: User, eventId: String, onSaved: (String) -> Unit, onBack: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    var title by rememberSaveable { mutableStateOf("") }
    var material by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { photoUri = it }
    FeatureScaffold("Add a resource", "Back", onBack, viewModel) {
        item {
            Text("Create a traceable item for this event. It is saved locally first and syncs when connected.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Resource name") }, singleLine = true)
            OutlinedTextField(material, { material = it }, Modifier.fillMaxWidth(), label = { Text("Material") }, singleLine = true)
            OutlinedTextField(quantity, { quantity = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text("Quantity") }, singleLine = true)
            OutlinedButton(
                onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (photoUri == null) "Add photo (optional)" else "Photo selected") }
            Button(
                onClick = {
                    val now = System.currentTimeMillis(); val resourceId = UUID.randomUUID().toString()
                    val resource = ResourceItem(resourceId, eventId, user.id, title.trim(), "General", material.trim(), ResourceCondition.GOOD,
                        quantity.toIntOrNull() ?: 1, "items", ResourceStatus.AVAILABLE, 0, emptyList(), now, now)
                    val passport = ResourcePassport(UUID.randomUUID().toString(), resourceId, "reevent://passport/$resourceId", "[]", now, now)
                    viewModel.saveResource(resource, passport, photoUri) { onSaved(resourceId) }
                }, modifier = Modifier.fillMaxWidth(), enabled = title.isNotBlank() && material.isNotBlank()
            ) { Text("Save resource and passport") }
        }
    }
}

@Composable
fun PassportLiveScreen(resourceId: String, onMatch: (String) -> Unit, onBack: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    val resource by viewModel.resource(resourceId).collectAsState(null)
    val passport by viewModel.passport(resourceId).collectAsState(null)
    FeatureScaffold("Digital passport", "Back", onBack, viewModel) {
        item {
            if (resource == null) EmptyPanel("Resource unavailable", "It may not be accessible for this workspace.") {}
            else {
                Text(resource!!.title, style = MaterialTheme.typography.headlineMedium)
                Text("${resource!!.quantity} ${resource!!.unit} • ${resource!!.material}")
                HorizontalDivider()
                Text("Passport code", fontWeight = FontWeight.Bold)
                Text(passport?.qrPayload ?: "Passport is syncing")
                Button(onClick = { onMatch(resourceId) }, modifier = Modifier.fillMaxWidth()) { Text("Find circular partners") }
            }
        }
    }
}

@Composable
fun MatchingLiveScreen(resourceId: String, onBack: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    val resource by viewModel.resource(resourceId).collectAsState(null)
    val programmes by viewModel.programmes().collectAsState(emptyList())
    val matches = resource?.let { ProgrammeMatcher.rank(it, programmes) }.orEmpty()
    FeatureScaffold("Circular matches", "Back", onBack, viewModel) {
        if (matches.isEmpty()) item { EmptyPanel("No eligible partner yet", "Add a partner programme or refresh when you are online.") {} }
        items(matches, key = { it.id }) { programme ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(programme.name, style = MaterialTheme.typography.titleMedium); Text(programme.location.ifBlank { "Location to be confirmed" }); Text("Accepts: ${programme.acceptedMaterials.ifEmpty { listOf("all materials") }.joinToString()}") } }
        }
    }
}

@Composable
fun MarketplaceLiveScreen(user: User, onPassport: (String) -> Unit, onProfile: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val resources by viewModel.marketplace().collectAsState(emptyList())
    FeatureScaffold("Marketplace", "Account", onProfile, viewModel) {
        if (resources.isEmpty()) item { EmptyPanel("No public resources yet", "Resources marked available by organisers will appear here.") {} }
        items(resources, key = { it.id }) { ResourceLine(it) { onPassport(it.id) } }
    }
}

@Composable
fun ParticipantReturnLiveScreen(user: User, onMarketplace: () -> Unit, onProfile: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    val resources by viewModel.marketplace().collectAsState(emptyList())
    FeatureScaffold("Participant returns", "Account", onProfile, viewModel) {
        item { Button(onClick = onMarketplace, modifier = Modifier.fillMaxWidth()) { Text("Browse available resources") } }
        if (resources.isNotEmpty()) item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Quick return", style = MaterialTheme.typography.titleMedium); Text(resources.first().title); Button(onClick = { viewModel.createReturn(user, resources.first()) }) { Text("Create return request") } } } }
        if (transactions.isEmpty()) item { EmptyPanel("No return activity", "Your requests will appear here with their handover status.") {} }
        items(transactions, key = { it.id }) { transaction -> Card(Modifier.fillMaxWidth()) { Text("${transaction.type.name.lowercase().replaceFirstChar(Char::titlecase)} • ${transaction.status.name}", Modifier.padding(16.dp)) } }
    }
}

@Composable
fun PartnerWorkbenchLiveScreen(user: User, onMap: () -> Unit, onProfile: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val programmes by viewModel.programmes(user.id).collectAsState(emptyList())
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    FeatureScaffold("Partner workbench", "Account", onProfile, viewModel) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { viewModel.createProgramme(user) }, modifier = Modifier.weight(1f)) { Text("Add programme") }; OutlinedButton(onClick = onMap, modifier = Modifier.weight(1f)) { Text("View matches") } } }
        if (programmes.isEmpty()) item { EmptyPanel("No programmes", "Create a programme so organisers can find your circular services.") {} }
        items(programmes, key = { it.id }) { programme -> Card(Modifier.fillMaxWidth()) { Text(programme.name, Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium) } }
        if (transactions.isNotEmpty()) item { Text("Assigned handovers", style = MaterialTheme.typography.titleLarge) }
        items(transactions, key = { it.id }) { transaction -> Card(Modifier.fillMaxWidth()) { Text("${transaction.status.name} • ${transaction.quantity} item(s)", Modifier.padding(16.dp)) } }
    }
}

@Composable
fun PartnerMapLiveScreen(onBack: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    val programmes by viewModel.programmes().collectAsState(emptyList())
    FeatureScaffold("Partner network", "Back", onBack, viewModel) {
        if (programmes.isEmpty()) item { EmptyPanel("No active partners", "Partner programmes become visible once they are active.") {} }
        items(programmes, key = { it.id }) { programme -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(programme.name, style = MaterialTheme.typography.titleMedium); Text(programme.location.ifBlank { "Location pending" }) } } }
    }
}

@Composable
fun ImpactLiveScreen(eventId: String, onBack: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    val records by viewModel.impact(eventId).collectAsState(emptyList())
    FeatureScaffold("Event impact", "Back", onBack, viewModel) {
        if (records.isEmpty()) item { EmptyPanel("No impact recorded", "Impact is added when a handover or recovery is completed.") {} }
        items(records, key = { it.id }) { record -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("${record.materialDivertedKg} kg diverted", style = MaterialTheme.typography.titleMedium); Text("${record.emissionsAvoidedKg} kg emissions avoided") } } }
    }
}

@Composable private fun FeatureScaffold(title: String, actionLabel: String, onAction: () -> Unit, viewModel: FeatureViewModel, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    val action by viewModel.action.collectAsState()
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { LogoMark(size = 42.dp); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.headlineMedium); Text("Live workspace", style = MaterialTheme.typography.labelLarge) }; OutlinedButton(onClick = onAction) { Text(actionLabel) } }
        action.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
        action.notice?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        if (action.loading) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator(Modifier.height(24.dp)) }
    }
}

@Composable private fun EventCard(event: Event, onAddResource: (String) -> Unit, onImpact: (String) -> Unit) = Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(event.name, style = MaterialTheme.typography.titleLarge); Text(event.venue.ifBlank { "Venue to be confirmed" }); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { onAddResource(event.id) }) { Text("Add resource") }; OutlinedButton(onClick = { onImpact(event.id) }) { Text("Impact") } } } }
@Composable private fun ResourceLine(resource: ResourceItem, onClick: () -> Unit) = Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(resource.title, style = MaterialTheme.typography.titleMedium); Text("${resource.quantity} ${resource.unit} • ${resource.status.name.lowercase()}") }; OutlinedButton(onClick = onClick) { Text("View") } } }
@Composable private fun EmptyPanel(title: String, detail: String, action: @Composable () -> Unit) = Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(detail); action() } }
