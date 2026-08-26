package com.reevent.app.ui.materials

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.ui.theme.ReEventTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MaterialComponentsTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `accessible picker exposes all eleven families`() {
        compose.setContent {
            ReEventTheme { MaterialFamilyPickerField(null, {}, allowAny = true) }
        }
        compose.onNodeWithText("Any material").performClick()
        MaterialFamily.entries.forEach { family ->
            compose.onNodeWithText("Search families").performTextReplacement(family.displayLabel)
            compose.onNodeWithTag("material_family_${family.name}").assertIsDisplayed()
        }
    }
}
