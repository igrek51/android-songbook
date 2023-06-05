package igrek.songbook.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun <T> SimpleListColumn(
    items: MutableList<T>,
    scrollState: ScrollState = rememberScrollState(),
    itemContent: @Composable (item: T) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(scrollState),
    ) {
        for (index in items.indices) {
            val item = items[index]

            Box(modifier = Modifier.fillMaxWidth()) {
                itemContent(item)
            }
            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )

        }
    }
}

