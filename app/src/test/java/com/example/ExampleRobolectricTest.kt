package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.ui.calculator.TaylorCalculatorRoute
import com.example.ui.calculator.TaylorViewModel
import com.example.ui.theme.TaylorCncTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Taylor CNC", appName)
  }

  @Test
  fun `shows error when velocity is zero and computes results when valid`() {
    val viewModel = TaylorViewModel()
    composeTestRule.setContent {
      TaylorCncTheme {
        TaylorCalculatorRoute(viewModel = viewModel)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("velocity_input").performScrollTo().performTextReplacement("0")
    composeTestRule.onNodeWithTag("calculate_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule
      .onNodeWithTag("velocity_error_text", useUnmergedTree = true)
      .performScrollTo()
      .assertIsDisplayed()

    composeTestRule.onNodeWithTag("velocity_input").performScrollTo().performTextReplacement("40")
    composeTestRule.onNodeWithTag("calculate_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("result_tool_life_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("result_new_speed_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("breakdown_card").performScrollTo().assertIsDisplayed()
  }
}
