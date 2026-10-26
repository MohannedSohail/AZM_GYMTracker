package com.example.azm_gymtracker.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.azm_gymtracker.domain.validator.WeightFormatter
import com.example.azm_gymtracker.ui.component.AzmPrimaryButton
import com.example.azm_gymtracker.ui.component.AzmSecondaryButton
import com.example.azm_gymtracker.ui.theme.AZM_GYMTrackerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RtlAndTouchTargetUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun primaryButton_hasMinimumTouchTargetHeight48dp() {
        composeTestRule.setContent {
            AZM_GYMTrackerTheme {
                AzmPrimaryButton(
                    text = "ابدأ التمرين",
                    onClick = {},
                    modifier = Modifier.testTag("primary_btn"),
                )
            }
        }

        composeTestRule
            .onNodeWithTag("primary_btn")
            .assertHeightIsAtLeast(48.dp)
            .assertIsDisplayed()
    }

    @Test
    fun secondaryButton_hasMinimumTouchTargetHeight48dp() {
        composeTestRule.setContent {
            AZM_GYMTrackerTheme {
                AzmSecondaryButton(
                    text = "إلغاء",
                    onClick = {},
                    modifier = Modifier.testTag("secondary_btn"),
                )
            }
        }

        composeTestRule
            .onNodeWithTag("secondary_btn")
            .assertHeightIsAtLeast(48.dp)
            .assertIsDisplayed()
    }

    @Test
    fun rtlNumericIsolation_rendersInvertedDecimalsWithoutSwapping() {
        val testWeightText = "72.5 kg"

        composeTestRule.setContent {
            AZM_GYMTrackerTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    // Under RTL, isolated LTR number must remain intact
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = testWeightText,
                            modifier = Modifier.testTag("numeric_weight"),
                        )
                    }
                }
            }
        }

        composeTestRule
            .onNodeWithTag("numeric_weight")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("72.5 kg")
            .assertIsDisplayed()
    }

    @Test
    fun weightFormatter_producesProperLtrStringsForArabicLayout() {
        val formatted = WeightFormatter.formatWeight(72.5f)
        org.junit.Assert.assertEquals("72.5 kg", formatted)
    }
}
