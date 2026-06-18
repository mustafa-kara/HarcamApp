package com.mustafakara.harcam.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps a category's stable iconKey (design.md §7) to a Material icon. Keeping the mapping in one
 * place means categories store keys, never icons — so the icon set can evolve without a migration.
 *
 * One visual language only: every glyph is `Icons.Outlined.*` (uniform stroke weight, no filled
 * outliers) and reads as a calm finance pictogram rather than a literal toy (no game-pad, no
 * abstract shapes). The keys stay stable; only the rendered vector changed.
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
        "favorite" -> Icons.Outlined.FavoriteBorder
        "sports_esports" -> Icons.Outlined.Theaters
        else -> Icons.Outlined.MoreHoriz
    }
}
