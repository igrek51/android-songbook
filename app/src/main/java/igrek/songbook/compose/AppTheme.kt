package igrek.songbook.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun AppTheme(
    content: @Composable() () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkSideColors,
        content = {
            ProvideTextStyle(
                value = TextStyle(color = Color.White),
                content = content,
            )
        }
    )
}