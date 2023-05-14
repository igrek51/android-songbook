@file:OptIn(ExperimentalMaterial3Api::class)

package igrek.songbook.songpreview.quickmenu

import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import igrek.songbook.R
import igrek.songbook.cast.CastFocusControl
import igrek.songbook.cast.SongCastLobbyLayout
import igrek.songbook.cast.SongCastService
import igrek.songbook.compose.AppTheme
import igrek.songbook.compose.md_theme_light_primaryContainer
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.safeAsyncExecutor
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

// Singleton
class QuickMenuCast(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songCastService: LazyInject<SongCastService> = appFactory.songCastService,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    val songCastService by LazyExtractor(songCastService)

    var isVisible = false
        set(visible) {
            field = visible
            quickMenuView?.let { quickMenuView ->
                if (visible) {
                    quickMenuView.visibility = View.VISIBLE
                } else {
                    quickMenuView.visibility = View.GONE
                }
            }
        }
    private var quickMenuView: View? = null

    fun setQuickMenuView(quickMenuView: View) {
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
}



@Composable
private fun MainComponent(controller: QuickMenuCast) {
    Column(Modifier.padding(8.dp)) {
        Text("Song Cast settings")

        var expanded by remember { mutableStateOf(false) }
        val selectedOptionText = controller.songCastService.presenterFocusControl.desctiption
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = selectedOptionText,
                onValueChange = {},
                label = { Text("Remote Control") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                CastFocusControl.values().forEach { enumOption ->
                    DropdownMenuItem(
                        text = { Text(text = enumOption.desctiption) },
                        onClick = {
                            controller.songCastService.presenterFocusControl = enumOption
                            expanded = false
                        },
                    )
                }
            }
        }

        SwitchWithLabel("Follow presenter's scroll", controller.songCastService.clientFollowScroll) {
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
private fun SwitchWithLabel(label: String, state: Boolean, onStateChange: (Boolean) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null, // This is for removing ripple when Row is clicked
                role = Role.Switch,
                onClick = {
                    onStateChange(!state)
                }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label)
        Spacer(modifier = Modifier.padding(start = 8.dp))
        Switch(
            checked = state,
            onCheckedChange = {
                onStateChange(it)
            }
        )
    }
}
