@file:OptIn(ExperimentalFoundationApi::class)

package igrek.songbook.songselection.listview

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import igrek.songbook.R
import igrek.songbook.compose.colorTextSubtitle
import igrek.songbook.compose.colorTextTitle
import igrek.songbook.compose.itemBorderStroke
import igrek.songbook.songselection.tree.SongTreeItem
import igrek.songbook.util.mainScope
import kotlinx.coroutines.launch

class SongItemsContainer(
    var items: List<SongTreeItem> = mutableListOf(),
    val modifiedAll: MutableState<Long> = mutableStateOf(0),
) {
    fun replaceAll(newList: List<SongTreeItem>) {
        items = newList
        modifiedAll.value += 1
    }
}

@Composable
fun SongListComposable(
    itemsContainer: SongItemsContainer,
    scrollState: LazyListState = rememberLazyListState(),
    onItemClick: (item: SongTreeItem) -> Unit,
    onItemMore: (item: SongTreeItem) -> Unit,
) {
    key(itemsContainer.modifiedAll.value) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            state = scrollState,
        ) {
            itemsContainer.items.forEachIndexed { index: Int, item: SongTreeItem ->
                item (key = item.song?.id ?: item.category?.id ?: index) {
                    SongTreeItemComposable(item, onItemClick, onItemMore)
                }
            }
        }
    }
}

@Composable
fun SongTreeItemComposable(
    item: SongTreeItem,
    onItemClick: (item: SongTreeItem) -> Unit,
    onItemMore: (item: SongTreeItem) -> Unit,
) {
    Row (
        Modifier.padding(0.dp)
            .border(itemBorderStroke)
            .combinedClickable(
                onClick = {
                    Handler(Looper.getMainLooper()).post {
                        mainScope.launch {
                            onItemClick(item)
                        }
                    }
                },
                onLongClick = {
                    mainScope.launch {
                        onItemMore(item)
                    }
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val song = item.song
        val iconId: Int = when {
            item.category != null -> R.drawable.folder
            item.customCategory != null -> R.drawable.folder
            song != null && song.isCustom() -> R.drawable.edit
            else -> R.drawable.note
        }

        Icon(
            painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.padding(start = 6.dp, end = 2.dp).size(24.dp),
            tint = Color.White,
        )

        Column (
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp, horizontal = 4.dp),
        ) {
            when {
                item.category != null -> {
                    Text(
                        modifier = Modifier.padding(vertical = 6.dp),
                        text = item.category.displayName.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        color = colorTextTitle,
                    )
                }

                item.customCategory != null -> {
                    Text(
                        modifier = Modifier.padding(vertical = 6.dp),
                        text = item.customCategory.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = colorTextTitle,
                    )
                }

                song != null -> {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = colorTextTitle,
                    )
                    val artist = song.displayCategories()
                    if (artist.isNotEmpty()) {
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorTextSubtitle,
                        )
                    }
                }
            }
        }

        if (song != null) {
            IconButton(
                onClick = {
                    mainScope.launch {
                        onItemMore(item)
                    }
                },
            ) {
                Icon(
                    painterResource(id = R.drawable.more),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White,
                )
            }
        }
    }
}