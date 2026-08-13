package com.reevent.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.reevent.app.ui.components.ResourceCardLayout
import com.reevent.app.ui.components.resourceCardLayoutForWidth
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AdaptiveLayoutTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `compact and expanded resource card branches preserve the 360dp breakpoint`() {
        assertEquals(ResourceCardLayout.COMPACT, resourceCardLayoutForWidth(359f))
        assertEquals(ResourceCardLayout.EXPANDED, resourceCardLayoutForWidth(360f))
    }

    @Test
    fun `two pane branches preserve the 420dp breakpoint`() {
        assertEquals(TwoPaneLayout.STACKED, twoPaneLayoutForWidth(419f))
        assertEquals(TwoPaneLayout.SIDE_BY_SIDE, twoPaneLayoutForWidth(420f))
    }

    @Test
    fun `compact branch renders a stacked structure`() {
        compose.setContent {
            AdaptiveTwoPane(
                layout = TwoPaneLayout.STACKED,
                first = { Text("First pane") },
                second = { Text("Second pane") },
            )
        }

        compose.onNodeWithTag("adaptive-stacked").assertIsDisplayed()
        compose.onNodeWithText("First pane").assertIsDisplayed()
        compose.onNodeWithText("Second pane").assertIsDisplayed()
    }

    @Test
    fun `expanded branch renders a side by side structure`() {
        compose.setContent {
            AdaptiveTwoPane(
                layout = TwoPaneLayout.SIDE_BY_SIDE,
                first = { Text("First pane") },
                second = { Text("Second pane") },
            )
        }

        compose.onNodeWithTag("adaptive-side-by-side").assertIsDisplayed()
        compose.onNodeWithText("First pane").assertIsDisplayed()
        compose.onNodeWithText("Second pane").assertIsDisplayed()
    }
}
