package com.mustafakara.harcam.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps a category's stable iconKey (design.md §7) to a Material icon. Keeping the mapping in one
 * place means categories store keys, never icons — so the icon set can evolve without a migration.
 */
object CategoryIcons {
    val available: List<String> = listOf(
        "restaurant", "directions_bus", "receipt_long",
        "shopping_bag", "favorite", "sports_esports", "category",
    )

    fun forKey(key: String): ImageVector = when (key) {
        "restaurant" -> Icons.Outlined.Restaurant
        "directions_bus" -> Icons.Outlined.DirectionsBus
        "receipt_long" -> Icons.Outlined.ReceiptLong
        "shopping_bag" -> Icons.Outlined.ShoppingBag
        "favorite" -> Icons.Outlined.Favorite
        "sports_esports" -> Icons.Outlined.SportsEsports
        else -> Icons.Outlined.Category
    }
}
