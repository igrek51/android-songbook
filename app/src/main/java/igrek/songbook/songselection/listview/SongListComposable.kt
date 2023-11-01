@file:OptIn(ExperimentalFoundationApi::class)

package igrek.songbook.songselection.listview

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.songselection.listview.items.AbstractListItem
import igrek.songbook.songselection.listview.items.CategoryListItem
import igrek.songbook.songselection.listview.items.CustomCategoryListItem
import igrek.songbook.songselection.listview.items.SongListItem
import igrek.songbook.util.mainScope
import kotlinx.coroutines.launch

class SongItemsContainer(
    var items: List<AbstractListItem> = mutableListOf(),
    val modifiedAll: MutableState<Long> = mutableStateOf(0),
    val modifiedMap: MutableMap<Int, MutableState<Long>> = mutableMapOf(),
    private val itemToIndex: MutableMap<AbstractListItem, Int> = mutableMapOf(),
) {
    fun replaceAll(newList: List<AbstractListItem>) {
        items = newList
        items.forEachIndexed { index, item ->
            if (!modifiedMap.containsKey(index)) {
                modifiedMap[index] = mutableStateOf(0)
            }
            itemToIndex[item] = index
        }
        modifiedAll.value += 1
    }

    fun notifyItemChange(item: AbstractListItem) {
        val index = itemToIndex[item] ?: return
        modifiedMap.getValue(index).value += 1
    }
}

typealias PostButtonComposable = @Composable (item: AbstractListItem, onItemClick: (item: AbstractListItem) -> Unit, onItemMore: ((item: AbstractListItem) -> Unit)?) -> Unit

@Composable
fun SongListComposable(
    itemsContainer: SongItemsContainer,
    scrollState: LazyListState = rememberLazyListState(),
    onItemClick: (item: AbstractListItem) -> Unit,
    onItemMore: ((item: AbstractListItem) -> Unit)? = null,
    postButtonContent: PostButtonComposable? = null,
) {
    key(itemsContainer.modifiedAll.value) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            state = scrollState,
        ) {
            itemsContainer.items.forEachIndexed { index: Int, item: AbstractListItem ->
                item (key = item.id() ?: index) {
                    SongTreeItemComposable(itemsContainer, item, index, onItemClick, onItemMore, postButtonContent)
                }
            }
        }
    }
}

@Composable
fun SongTreeItemComposable(
    itemsContainer: SongItemsContainer,
    item: AbstractListItem,
    index: Int,
    onItemClick: (item: AbstractListItem) -> Unit,
    onItemMore: ((item: AbstractListItem) -> Unit)? = null,
    postButtonContent: PostButtonComposable? = null,
) {
    key(itemsContainer.modifiedMap.getValue(index).value) {
        logger.debug("recompose item $index")
        Row(
            Modifier
                .defaultMinSize(minHeight = 48.dp)
                .padding(0.dp)
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
                            onItemMore?.invoke(item)
                        }
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SongItemIconComposable(item)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            ) {
                SongItemTextComposable(item)
            }

            when (postButtonContent == null) {
                true -> SongItemPostButtonComposable(item, onItemClick, onItemMore)
                else -> postButtonContent(item, onItemClick, onItemMore)
            }
        }
    }
}

@Composable
fun SongItemIconComposable(
    item: AbstractListItem,
) {
    val iconId: Int = when {
        item is CategoryListItem -> R.drawable.folder
        item is CustomCategoryListItem -> R.drawable.folder
        item is SongListItem && item.song.isCustom() -> R.drawable.edit
        else -> R.drawable.note
    }
    Icon(
        painterResource(id = iconId),
        contentDescription = null,
        modifier = Modifier.padding(start = 6.dp, end = 2.dp).size(24.dp),
        tint = Color.White,
    )
}

@Composable
fun SongItemTextComposable(
    item: AbstractListItem,
) {
    when (item) {
        is CategoryListItem -> {
            Text(
                modifier = Modifier.padding(vertical = 6.dp),
                text = item.category.displayName.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = colorTextTitle,
            )
        }

        is CustomCategoryListItem -> {
            Text(
                modifier = Modifier.padding(vertical = 6.dp),
                text = item.customCategory.name,
                style = MaterialTheme.typography.titleSmall,
                color = colorTextTitle,
            )
        }

        is SongListItem -> {
            Text(
                text = item.song.title,
                style = MaterialTheme.typography.titleSmall,
                color = colorTextTitle,
            )
            val artist = item.song.displayCategories()
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

@Composable
fun SongItemPostButtonComposable(
    item: AbstractListItem,
    onItemClick: (item: AbstractListItem) -> Unit,
    onItemMore: ((item: AbstractListItem) -> Unit)? = null,
) {
    when {
        item is SongListItem && onItemMore != null -> {
            IconButton(
                modifier = Modifier.padding(0.dp).size(40.dp, 40.dp),
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