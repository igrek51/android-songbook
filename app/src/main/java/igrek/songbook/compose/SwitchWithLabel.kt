package igrek.songbook.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun SwitchWithLabel(
    label: String,
    state: Boolean,
    tooltip: String? = null,
    onStateChange: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null, // This is for removing ripple when Row is clicked
                role = Role.Switch,
                onClick = { onStateChange(!state) }
            ).padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label)
        Spacer(modifier = Modifier.padding(start = 6.dp))
        Switch(
            checked = state,
            onCheckedChange = { onStateChange(it) }
        )
        if (tooltip != null) {
            Tooltip(tooltip)
        }
    }
}
