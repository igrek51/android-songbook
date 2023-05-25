@file:OptIn(ExperimentalMaterial3Api::class)

package igrek.songbook.songpreview.quickmenu

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import igrek.songbook.R
import igrek.songbook.cast.CastScrollControl
import igrek.songbook.cast.SongCastLobbyLayout
import igrek.songbook.compose.AppTheme
import igrek.songbook.compose.SwitchWithLabel
import igrek.songbook.compose.md_theme_light_primaryContainer
import igrek.songbook.info.errorcheck.safeAsyncExecutor
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.appFactory

// Singleton
class QuickMenuCast {
    val songCastService by LazyExtractor(appFactory.songCastService)
    val preferencesState by LazyExtractor(appFactory.preferencesState)
    val songPreviewLayoutController by LazyExtractor(appFactory.songPreviewLayoutController)

    val state = QuickMenuCastState()

    var isVisible = false
        set(visible) {
            field = visible
            quickMenuView?.let { quickMenuView ->
                if (visible) {
                    quickMenuView.visibility = View.VISIBLE
                    updateState()
                } else {
                    quickMenuView.visibility = View.GONE
                }
            }
        }
    private var quickMenuView: View? = null

    fun setQuickMenuView(quickMenuView: View) {
        updateState()
        this.quickMenuView = quickMenuView
        val thisMenu = this
        quickMenuView.findViewById<ComposeView>(R.id.compose_quick_menu_cast).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    MainComponent(thisMenu)
                }
            }
        }
    }

    private fun updateState() {
        state.isPresenting = songCastService.isPresenting()
        state.sameSongPresented = songCastService.isSameSongPresented(songPreviewLayoutController.currentSong)
    }
}

class QuickMenuCastState {
    var isPresenting: Boolean by mutableStateOf(false)
    var sameSongPresented: Boolean by mutableStateOf(false)
}

@Composable
private fun MainComponent(controller: QuickMenuCast) {
    Column(Modifier.padding(8.dp)) {
        Text(
            stringResource(R.string.songcast_settings),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 8.dp),
        )

        val songStatusResId = when {
            controller.state.sameSongPresented && controller.state.isPresenting -> R.string.songcast_youre_presenting_this_song
            controller.state.sameSongPresented -> R.string.songcast_youre_spectating_this_song
            else -> R.string.songcast_this_song_is_not_presented
        }
        Text(
            stringResource(songStatusResId),
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp).fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        ScrollControlDropdown(controller)

        SwitchWithLabel(
            stringResource(R.string.songcast_open_presented_song_automatically),
            controller.songCastService.clientOpenPresentedSongs,
            tooltip = stringResource(R.string.songcast_open_presented_song_automatically_tooltip),
        ) {
            controller.songCastService.clientOpenPresentedSongs = it
        }

        SwitchWithLabel(
            stringResource(R.string.songcast_follow_presenters_scroll),
            controller.songCastService.clientFollowScroll,
            tooltip = stringResource(R.string.songcast_follow_presenters_scroll_tooltip),
        ) {
            controller.songCastService.clientFollowScroll = it
        }

        Button(
            onClick = safeAsyncExecutor {
                appFactory.layoutController.g.showLayout(SongCastLobbyLayout::class)
            },
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        ) {
            Icon(
                painterResource(id = R.drawable.cast),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = md_theme_light_primaryContainer,
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.songcast_open_lobby))
        }
    }
}

@Composable
private fun ScrollControlDropdown(controller: QuickMenuCast) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOptionText = stringResource(controller.preferencesState.castScrollControl.descriptionResId)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
    ) {
        TextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = selectedOptionText,
            onValueChange = {},
            label = { Text(stringResource(R.string.songcast_remote_control)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            CastScrollControl.values().forEach { enumOption ->
                DropdownMenuItem(
                    text = { Text(stringResource(enumOption.descriptionResId)) },
                    onClick = {
                        controller.preferencesState.castScrollControl = enumOption
                        expanded = false
                    },
                )
            }
        }
    }

}