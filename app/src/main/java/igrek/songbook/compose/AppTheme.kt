package igrek.songbook.compose

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val textStyle = LocalTextStyle.current.merge(TextStyle(color = Color.White))
    MaterialTheme(
        colorScheme = DarkSideColors,
        content = {
            CompositionLocalProvider(
                LocalTextStyle provides textStyle,
                LocalRippleTheme provides WhiteRippleTheme,
                content = content,
            )
        }
    )
}

private object WhiteRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.White

    @Composable
    override fun rippleAlpha() = defaultRippleAlpha
}

private val defaultRippleAlpha = RippleAlpha(
    pressedAlpha = 0.12f,
    focusedAlpha = 0.12f,
    draggedAlpha = 0.16f,
    hoveredAlpha = 0.08f,
)
