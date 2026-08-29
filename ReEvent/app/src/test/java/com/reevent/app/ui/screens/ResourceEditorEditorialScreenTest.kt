package com.reevent.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.reevent.app.ui.theme.ReEventTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class ResourceEditorEditorialScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `resource editor keeps back and save actions while hiding bottom navigation`() {
        var backCount = 0
        var saveCount = 0

        compose.setContent {
            ReEventTheme {
                ResourceEditorEditorialScaffold(
                    isNewResource = true,
                    action = FeatureActionState(),
                    onBack = { backCount += 1 },
                    onNavigate = {},
                    onSave = { saveCount += 1 },
                ) {
                    item { Text("Resource form content") }
                }
            }
        }

        compose.onNodeWithText("Draft saved").assertExists()
        compose.onNodeWithTag("resource_editor_back").performClick()
        compose.onNodeWithTag("resource_editor_save").performClick()
        compose.onNodeWithText("Events").assertDoesNotExist()

        assertEquals(1, backCount)
        assertEquals(1, saveCount)
    }
}
