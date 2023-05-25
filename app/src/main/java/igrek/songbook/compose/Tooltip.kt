@file:OptIn(ExperimentalMaterial3Api::class)

package igrek.songbook.compose

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

@Composable
fun Tooltip(
    hint: String,
) {
    val tooltipState = remember { PlainTooltipState() }
    val scope = rememberCoroutineScope()
    PlainTooltipBox(
        tooltip = { Text(hint) },
        tooltipState = tooltipState,
    ) {
        IconButton(
            onClick = { scope.launch { tooltipState.show() } },
            modifier = Modifier.tooltipAnchor(),
        ) {
            Icon(
                Icons.Filled.Info,
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = md_theme_light_primaryContainer,
                contentDescription = null,
            )
        }
    }
}

@Composable
fun Tooltip(
    hintResId: Int,
) {
    Tooltip(stringResource(id = hintResId))
}