package com.mustafakara.harcam

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mustafakara.harcam.core.ui.components.BudgetProgressBar
import com.mustafakara.harcam.core.ui.components.EmptyState
import com.mustafakara.harcam.core.ui.components.PrimaryButton
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.model.BudgetStatus
import com.mustafakara.harcam.domain.model.Currency
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose smoke tests for the design-system building blocks the screens compose from
 * (architecture.md §9) — render + interaction without the Hilt graph.
 */
class ComponentSmokeTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun emptyState_showsCopyAndFiresAction() {
        var clicked = false
        composeRule.setContent {
            HarcamTheme {
                EmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No spending logged yet",
                    description = "Add your first expense to see it here.",
                    actionText = "Add expense",
                    onAction = { clicked = true },
                )
            }
        }
        composeRule.onNodeWithText("No spending logged yet").assertIsDisplayed()
        composeRule.onNodeWithText("Add expense").performClick()
        assertTrue(clicked)
    }

    @Test
    fun budgetProgressBar_overBudget_showsOverByText() {
        composeRule.setContent {
            HarcamTheme {
                BudgetProgressBar(
                    status = BudgetStatus(categoryId = null, spent = 1200.0, limit = 1000.0),
                    currency = Currency.TRY,
                    formatter = MoneyFormatter(),
                    label = "Monthly budget",
                )
            }
        }
        // "Over by …" text proves the OVER level renders with text, not color alone.
        composeRule.onNodeWithText("Over by", substring = true).assertIsDisplayed()
    }

    @Test
    fun primaryButton_loading_isDisabled() {
        composeRule.setContent {
            HarcamTheme {
                PrimaryButton(text = "Save", onClick = {}, loading = true)
            }
        }
        composeRule.onNodeWithText("Save").assertDoesNotExist()
    }

    @Test
    fun primaryButton_disabled_blocksClick() {
        var clicked = false
        composeRule.setContent {
            HarcamTheme {
                PrimaryButton(text = "Save", onClick = { clicked = true }, enabled = false)
            }
        }
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        assertTrue(!clicked)
    }
}
