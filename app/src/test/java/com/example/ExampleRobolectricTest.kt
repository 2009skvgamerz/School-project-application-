package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.theme.StJosephTheme
import androidx.compose.material3.Text
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleRobolectricTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basic_title_renders() {
        composeTestRule.setContent {
            StJosephTheme {
                Text("St. Joseph's Higher Secondary")
            }
        }
        composeTestRule.onNodeWithText("St. Joseph's Higher Secondary").assertExists()
    }
}
