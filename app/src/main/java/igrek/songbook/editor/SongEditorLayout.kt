@file:OptIn(ExperimentalMaterial3Api::class)

package igrek.songbook.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import igrek.songbook.R
import igrek.songbook.compose.md_theme_dark_surfaceVariant
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.util.mainScope
import kotlinx.coroutines.launch


@Composable
internal fun MainComponent(controller: SongEditorLayoutController) {
    val state = controller.state
    Column {
        OutlinedTextField(
            value = state.songTitle,
            onValueChange = { state.songTitle = it },
            label = { Text(stringResource(R.string.edit_song_title_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        ArtistTextField(controller)
        ChordsNotationPicker(controller)

        ContentLabel(controller)
        EditorToolbar(controller)
        ContentTextField(controller)
    }
}

@Composable
private fun ArtistTextField(controller: SongEditorLayoutController) {
    val state = controller.state
    Box {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused)
                        state.artistAutocompleteExpanded.value = false
                },
            value = state.artist,
            onValueChange = {
                state.artist = it
                controller.fillArtistAutocompleteOptions()
            },
            label = { Text(stringResource(R.string.edit_song_category_name_label)) },
            singleLine = true,
        )
        DropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = state.artistAutocompleteExpanded.value,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
            onDismissRequest = {
                state.artistAutocompleteExpanded.value = false
            },
        ) {
            state.artistAutocompleteOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        state.artist = option
                        state.artistAutocompleteExpanded.value = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ChordsNotationPicker(controller: SongEditorLayoutController) {
    val state = controller.state
    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = state.chordsNotationExpanded.value,
        onExpandedChange = {
            state.chordsNotationExpanded.value = !state.chordsNotationExpanded.value
        }
    ) {
        val textRes = ChordsNotation.parseById(state.chordsNotationId)?.displayNameResId
        val textValue = textRes?.let { stringResource(it) } ?: ""
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = textValue,
            onValueChange = { },
            label = { Text(stringResource(R.string.edit_song_chords_notation)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = state.chordsNotationExpanded.value
                )
            },
        )
        ExposedDropdownMenu(
            expanded = state.chordsNotationExpanded.value,
            onDismissRequest = {
                state.chordsNotationExpanded.value = false
            }
        ) {
            controller.chordNotationOptions.forEach { selectionOption: ChordsNotation ->
                DropdownMenuItem(
                    text = { Text(stringResource(selectionOption.displayNameResId)) },
                    onClick = {
                        state.chordsNotationId = selectionOption.id
                        state.chordsNotationExpanded.value = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ContentLabel(controller: SongEditorLayoutController) {
    Row (verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.edit_song_content_label_hint))

        IconButton(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .size(40.dp, 40.dp),
            onClick = {
                mainScope.launch {
                    controller.openUrlChordFormat()
                }
            },
        ) {
            Icon(
                painterResource(id = R.drawable.help),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun EditorToolbar(controller: SongEditorLayoutController) {
    val scrollState = rememberScrollState()
    val editorTransformer = controller.editorTransformer
    val state = controller.state
    Row (modifier = Modifier.horizontalScroll(scrollState)) {
        FlatButton(R.string.edit_song_transform_chords) {
            controller.showTransformMenu()
        }
        FlatButton(R.string.chords_editor_reformat_trim) {
            controller.wrapHistoryContext {
                editorTransformer.reformatAndTrimEditor()
            }
        }
        FlatButton(R.string.edit_song_validate_chords) {
            controller.validateChords()
        }
        FlatButton(R.string.edit_song_detect_chords) {
            controller.wrapHistoryContext {
                editorTransformer.detectChords(keepIndentation = true)
            }
        }
        FlatButton(R.string.chords_editor_select_line) {
            editorTransformer.selectNextLine()
            state.contentFocusRequester.requestFocus()
        }
        FlatButton(R.string.edit_song_copy) {
            editorTransformer.onCopyClick()
        }
        FlatButton(R.string.edit_song_paste) {
            editorTransformer.onPasteClick()
            state.contentFocusRequester.requestFocus()
        }
        FlatButton(R.string.chords_editor_duplicate) {
            controller.wrapHistoryContext {
                editorTransformer.duplicateSelection()
            }
            state.contentFocusRequester.requestFocus()
        }
        FlatButton(R.string.edit_song_undo) {
            controller.undoChange()
        }
        FlatButton(R.string.edit_song_add_chord_splitter) {
            editorTransformer.addChordSplitter()
            state.contentFocusRequester.requestFocus()
        }
        FlatButton(R.string.edit_song_add_chord) {
            editorTransformer.onWrapChordClick()
            state.contentFocusRequester.requestFocus()
        }
        FlatButton(R.string.left_arrow) {
            controller.quickCursorMove(-1)
            state.contentFocusRequester.requestFocus()
        }
        FlatButton(R.string.right_arrow) {
            controller.quickCursorMove(+1)
            state.contentFocusRequester.requestFocus()
        }
    }
}

@Composable
private fun ContentTextField(controller: SongEditorLayoutController) {
    val state = controller.state
    val fontFamily = when (controller.preferencesState.chordsEditorFontTypeface) {
        FontTypeface.SANS_SERIF -> FontFamily.SansSerif
        FontTypeface.SERIF -> FontFamily.Serif
        FontTypeface.MONOSPACE -> FontFamily.Monospace
    }
    OutlinedTextField(
        modifier = Modifier
            .padding(vertical = 0.1.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 100.dp)
            .horizontalScroll(state.horizontalScroll)
            .focusRequester(state.contentFocusRequester),
        value = state.lyricsContent.value,
        onValueChange = {
            controller.onLyricsFieldChange(it)
        },
        label = { Text(stringResource(R.string.edit_song_content_label)) },
        placeholder = { Text(stringResource(R.string.edit_song_content_hint)) },
        singleLine = false,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Default),
        textStyle = TextStyle.Default.copy(
            fontFamily = fontFamily,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
        ),
    )
}

@Composable
private fun FlatButton(
    textResId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = {
            mainScope.launch {
                onClick()
            }
        },
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .heightIn(min = 40.dp),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = md_theme_dark_surfaceVariant,
            contentColor = Color.White,
        ),
    ) {
        Text(stringResource(textResId))
    }
}
