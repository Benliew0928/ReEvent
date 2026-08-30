package com.reevent.app.ui

import android.content.Context
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.createGraph
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventSaveNavigationTest {
    @Test
    fun savingNewEventRemovesCreateEditorFromBackStack() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val nav = eventNavController()
            nav.navigate(EventEditorRoute())

            nav.openEventAfterSave("event-1")

            assertTrue(nav.currentDestination?.hasRoute<EventDetailRoute>() == true)
            assertTrue(nav.popBackStack())
            assertTrue(nav.currentDestination?.hasRoute<EventListRoute>() == true)
        }
    }

    @Test
    fun savingEventEditsReusesDetailWithoutLeavingDuplicateRoutes() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val nav = eventNavController()
            nav.navigate(EventDetailRoute("event-1"))
            nav.navigate(EventEditorRoute("event-1"))

            nav.openEventAfterSave("event-1")

            assertTrue(nav.currentDestination?.hasRoute<EventDetailRoute>() == true)
            assertTrue(nav.popBackStack())
            assertTrue(nav.currentDestination?.hasRoute<EventListRoute>() == true)
        }
    }

    @Test
    fun savingResourceRemovesAddResourcePageFromBackStack() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val nav = eventNavController()
            nav.navigate(EventDetailRoute("event-1"))
            nav.navigate(OrganizerAddRoute("event-1"))

            nav.openEventAfterResourceSave("event-1")

            assertTrue(nav.currentDestination?.hasRoute<EventDetailRoute>() == true)
            assertTrue(nav.popBackStack())
            assertTrue(nav.currentDestination?.hasRoute<EventListRoute>() == true)
        }
    }

    private fun eventNavController(): TestNavHostController {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return TestNavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            graph = createGraph(startDestination = EventListRoute) {
                composable<EventListRoute> { }
                composable<EventEditorRoute> { }
                composable<EventDetailRoute> { }
                composable<OrganizerAddRoute> { }
            }
        }
    }
}
