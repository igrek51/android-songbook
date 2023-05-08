package igrek.songbook.compose

import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat


@Composable
fun RichText(
    resHtml: Int,
    modifier: Modifier = Modifier,
) {
    val html = stringResource(resHtml)
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        factory = { context ->
            TextView(context)
        },
        update = {
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    )
}


@Composable
fun LabelText(
    stringRes: Int,
    modifier: Modifier = Modifier,
) {
    val text = stringResource(stringRes)
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        factory = { context ->
            TextView(context)
        },
        update = {
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            it.text = text
        }
    )
}

@Composable
fun RichText(
    html: String,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = { it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY) }
    )
}