package com.reevent.app.ui.screens

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reevent.app.core.data.AppResult
import com.reevent.app.core.data.PartnerRepository
import com.reevent.app.core.location.DeviceLocationProvider
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.PartnerCandidate
import com.reevent.app.core.model.PartnerDiscoveryRequest
import com.reevent.app.core.model.PartnerDiscoveryResult
import com.reevent.app.core.model.PartnerMapFilters
import com.reevent.app.core.model.ProgrammeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PartnerMapPresentation { MAP, LIST }
enum class PartnerLocationPermission { NOT_REQUESTED, GRANTED, DENIED, PERMANENTLY_DENIED }

internal fun reducePartnerLocationPermission(granted: Boolean, permanent: Boolean = false): PartnerLocationPermission = when {
    granted -> PartnerLocationPermission.GRANTED
    permanent -> PartnerLocationPermission.PERMANENTLY_DENIED
    else -> PartnerLocationPermission.DENIED
}

data class PartnerMapUiState(
    val resourceId: String? = null,
    val filters: PartnerMapFilters = PartnerMapFilters(),
    val result: PartnerDiscoveryResult = PartnerDiscoveryResult(),
    val selectedCandidate: PartnerCandidate? = null,
    val presentation: PartnerMapPresentation = PartnerMapPresentation.MAP,
    val locationPermission: PartnerLocationPermission = PartnerLocationPermission.NOT_REQUESTED,
    val loading: Boolean = false,
    val mapLoading: Boolean = false,
    val mapError: String? = null,
    val error: String? = null,
) {
    val isResourceContext: Boolean get() = resourceId != null
}

@HiltViewModel
class PartnerMapViewModel @Inject constructor(
    private val partners: PartnerRepository,
    private val deviceLocations: DeviceLocationProvider,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PartnerMapUiState())
    val state: StateFlow<PartnerMapUiState> = mutableState.asStateFlow()
    private var discoveryJob: Job? = null
    private var deviceLocation: GeoLocation? = null

    fun load(resourceId: String?) {
        if (mutableState.value.resourceId != resourceId) {
            mutableState.value = PartnerMapUiState(resourceId = resourceId)
            deviceLocation = null
        }
        discover()
    }

    fun setMaterial(material: String?) = updateFilters { copy(material = material?.takeIf(String::isNotBlank)) }

    fun toggleType(type: ProgrammeType) = updateFilters {
        copy(programmeTypes = if (type in programmeTypes) programmeTypes - type else programmeTypes + type)
    }

    fun setMaximumDistance(distanceKm: Double?) = updateFilters { copy(maximumDistanceKm = distanceKm) }

    fun setPickupOnly(value: Boolean) = updateFilters { copy(pickupOnly = value) }

    fun setPresentation(presentation: PartnerMapPresentation) {
        mutableState.value = mutableState.value.copy(presentation = presentation)
    }

    fun select(candidate: PartnerCandidate?) {
        mutableState.value = mutableState.value.copy(selectedCandidate = candidate)
    }

    fun locationDenied(permanent: Boolean = false) {
        mutableState.value = mutableState.value.copy(locationPermission = reducePartnerLocationPermission(false, permanent))
    }

    @SuppressLint("MissingPermission")
    fun locationGranted() {
        mutableState.value = mutableState.value.copy(locationPermission = reducePartnerLocationPermission(true))
        viewModelScope.launch {
            val location = deviceLocations.currentApproximateLocation()
            if (location == null) {
                mutableState.value = mutableState.value.copy(
                    error = "Your current location is unavailable. The saved resource or event location is still being used.",
                )
            } else {
                useDeviceLocation(location)
            }
        }
    }

    fun useDeviceLocation(location: GeoLocation) {
        deviceLocation = location
        mutableState.value = mutableState.value.copy(locationPermission = PartnerLocationPermission.GRANTED)
        discover()
    }

    fun mapLoading() {
        mutableState.value = mutableState.value.copy(mapLoading = true, mapError = null)
    }

    fun mapLoaded() {
        mutableState.value = mutableState.value.copy(mapLoading = false, mapError = null)
    }

    fun mapFailed(reason: String?) {
        mutableState.value = mutableState.value.copy(
            mapLoading = false,
            mapError = reason?.takeIf(String::isNotBlank) ?: "Map tiles are unavailable. Use the programme list below.",
            presentation = PartnerMapPresentation.LIST,
        )
    }

    private fun updateFilters(transform: PartnerMapFilters.() -> PartnerMapFilters) {
        mutableState.value = mutableState.value.copy(filters = mutableState.value.filters.transform())
        discover()
    }

    private fun discover() {
        discoveryJob?.cancel()
        val current = mutableState.value
        discoveryJob = viewModelScope.launch {
            mutableState.value = current.copy(loading = true, error = null, selectedCandidate = null)
            when (
                val result = partners.discoverProgrammes(
                    PartnerDiscoveryRequest(
                        resourceId = current.resourceId,
                        deviceLocation = deviceLocation,
                        filters = current.filters,
                    ),
                )
            ) {
                is AppResult.Success -> mutableState.value = mutableState.value.copy(
                    result = result.value,
                    loading = false,
                    error = null,
                )
                is AppResult.Failure -> mutableState.value = mutableState.value.copy(
                    loading = false,
                    error = "Partner programmes could not be loaded. Check your connection and retry.",
                )
            }
        }
    }
}
