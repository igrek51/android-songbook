package igrek.songbook.compose

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
fun <T> ReorderLazyListView(
    items: MutableList<T>,
    scrollState: LazyListState = rememberLazyListState(),
    onReorder: (newItems: MutableList<T>) -> Unit,
    itemContent: @Composable (item: T, modifier: Modifier, reorderButtonModifier: Modifier) -> Unit,
) {
    val draggingIndex: MutableState<Int> = remember { mutableStateOf(-1) }
    val dragTargetIndex: MutableState<Int?> = remember { mutableStateOf(null) }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    val itemHeights: MutableMap<Int, Float> = remember { mutableStateMapOf() }
    val itemAnimatedOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>> = remember { mutableStateMapOf() }
    val reorderButtonModifiers: MutableMap<Int, Modifier> = remember { mutableStateMapOf() }
    val isDraggingMes: MutableMap<Int, State<Boolean>> = remember { mutableStateMapOf() }
    val isDraggings: MutableMap<Int, State<Boolean>> = remember { mutableStateMapOf() }

    items.indices.forEach { index: Int ->
        val reorderButtonModifier = Modifier.createReorderButtonModifier(
            items, index, draggingIndex, dragTargetIndex, itemHeights, itemAnimatedOffsets,
            coroutineScope, onReorder,
        )
        reorderButtonModifiers[index] = reorderButtonModifier

        val offsetYAnimated: Animatable<Float, AnimationVector1D> = remember { Animatable(0f) }
        itemAnimatedOffsets[index] = offsetYAnimated

        val isDraggingMe: State<Boolean> = derivedStateOf {
            draggingIndex.value == index
        }
        isDraggingMes[index] = isDraggingMe

        val isDragging: State<Boolean> = derivedStateOf {
            draggingIndex.value != -1
        }
        isDraggings[index] = isDragging
    }

    LazyColumn(
        modifier = Modifier.fillMaxHeight(),
        state = scrollState,
    ) {
        items.forEachIndexed { index: Int, item: T ->
            item (key = index) {

                ReorderLazyListViewItem<T>(
                    item, index, isDraggingMes.getValue(index), isDraggings.getValue(index), dragTargetIndex,
                    itemHeights, itemAnimatedOffsets.getValue(index), itemContent, reorderButtonModifiers.getValue(index),
                )

            }
        }
    }
}

private fun <T> Modifier.createReorderButtonModifier(
    items: MutableList<T>,
    index: Int,
    draggingIndex: MutableState<Int>,
    dragTargetIndex: MutableState<Int?>,
    itemHeights: MutableMap<Int, Float>,
    itemAnimatedOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>>,
    coroutineScope: CoroutineScope,
    onReorder: (newItems: MutableList<T>) -> Unit,
) = this.pointerInput(index) {
    detectDragGestures(
        onDragStart = { _: Offset ->
            onDragStart(index, draggingIndex, itemAnimatedOffsets, coroutineScope)
        },
        onDragEnd = {
            onDragEnd(items, index, draggingIndex, dragTargetIndex, itemHeights, itemAnimatedOffsets, coroutineScope, onReorder)
        },
        onDragCancel = {
            onDragCancel(index, draggingIndex, dragTargetIndex, itemAnimatedOffsets, coroutineScope)
        },
        onDrag = { change: PointerInputChange, dragAmount: Offset ->
            change.consume()
            onDrag(dragAmount, items, index, dragTargetIndex, itemHeights, itemAnimatedOffsets, coroutineScope)
        }
    )
}

private fun onDragStart(
    index: Int,
    draggingIndex: MutableState<Int>,
    itemAnimatedOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>>,
    coroutineScope: CoroutineScope,
) {
    draggingIndex.value = index
    coroutineScope.launch {
        itemAnimatedOffsets[index]?.snapTo(0f)
    }
}

private fun <T> onDragEnd(
    items: MutableList<T>,
    index: Int,
    draggingIndex: MutableState<Int>,
    dragTargetIndex: MutableState<Int?>,
    itemHeights: MutableMap<Int, Float>,
    itemAnimatedOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>>,
    coroutineScope: CoroutineScope,
    onReorder: (newItems: MutableList<T>) -> Unit,
) {
    val relativateOffset = itemAnimatedOffsets[index]?.targetValue ?: 0f
    val (swapped, movedBy) = calculateItemsToSwap(index, items.size, relativateOffset, itemHeights)
    val minIndex = Integer.min(index, index + swapped)
    val maxIndex = Integer.max(index, index + swapped)
    val endOffset = relativateOffset - movedBy
    when {
        swapped < 0 -> {
            for (i in minIndex + 1..maxIndex) {
                coroutineScope.launch {
                    val heightPx = itemHeights[i] ?: 0f
                    itemAnimatedOffsets[i]?.snapTo(-heightPx)
                    itemAnimatedOffsets[i]?.animateTo(0f)
                }
            }
            coroutineScope.launch {
                itemAnimatedOffsets[index + swapped]?.snapTo(endOffset)
                itemAnimatedOffsets[index + swapped]?.animateTo(0f)
            }
            items.add(index + swapped, items.removeAt(index))
            onReorder(items)
        }
        swapped > 0 -> {
            for (i in minIndex until maxIndex) {
                coroutineScope.launch {
                    val heightPx = itemHeights[i] ?: 0f
                    itemAnimatedOffsets[i]?.snapTo(+heightPx)
                    itemAnimatedOffsets[i]?.animateTo(0f)
                }
            }
            coroutineScope.launch {
                itemAnimatedOffsets[index + swapped]?.snapTo(endOffset)
                itemAnimatedOffsets[index + swapped]?.animateTo(0f)
            }
            items.add(index + swapped, items.removeAt(index))
            onReorder(items)
        }
        else -> coroutineScope.launch {
            itemAnimatedOffsets[index]?.animateTo(0f)
        }
    }

    draggingIndex.value = -1
    dragTargetIndex.value = null
}

private fun onDragCancel(
    index: Int,
    draggingIndex: MutableState<Int>,
    dragTargetIndex: MutableState<Int?>,
    itemAnimatedOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>>,
    coroutineScope: CoroutineScope,
) {
    draggingIndex.value = -1
    dragTargetIndex.value = null
    coroutineScope.launch {
        itemAnimatedOffsets[index]?.animateTo(0f)
    }
}

private fun <T> onDrag(
    dragAmount: Offset,
    items: MutableList<T>,
    index: Int,
    dragTargetIndex: MutableState<Int?>,
    itemHeights: MutableMap<Int, Float>,
    itemAnimatedOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>>,
    coroutineScope: CoroutineScope,
) {
    val offsetYAnimatedVal = itemAnimatedOffsets[index]?.targetValue ?: 0f

    val (swapped, _) = calculateItemsToSwap(index, items.size, offsetYAnimatedVal, itemHeights)
    dragTargetIndex.value = when {
        swapped < 0 -> index + swapped - 1
        swapped > 0 -> index + swapped
        else -> null
    }

    coroutineScope.launch {
        itemAnimatedOffsets[index]?.snapTo(offsetYAnimatedVal + dragAmount.y)
    }
}

@SuppressLint("ModifierParameter")
@Composable
private fun <T> ReorderLazyListViewItem(
    item: T,
    index: Int,
    isDraggingMe: State<Boolean>,
    isDragging: State<Boolean>,
    dragTargetIndex: MutableState<Int?>,
    itemHeights: MutableMap<Int, Float>,
    offsetYAnimated: Animatable<Float, AnimationVector1D>,
    itemContent: @Composable (item: T, modifier: Modifier, reorderButtonModifier: Modifier) -> Unit,
    reorderButtonModifier: Modifier,
) {
    var itemModifier = Modifier
        .offset { IntOffset(0, offsetYAnimated.value.roundToInt() ) }
        .fillMaxWidth()
        .onGloballyPositioned { coordinates: LayoutCoordinates ->
            itemHeights[index] = coordinates.size.height.toFloat()
        }
    if (isDraggingMe.value) {
        itemModifier = itemModifier.background(Color.LightGray.copy(alpha = 0.15f))
    }

    DividerBeforeItem(index, dragTargetIndex)

    itemContent(item, itemModifier, reorderButtonModifier)

    DividerAfterItem(index, isDragging, dragTargetIndex)
}

@Composable
private fun DividerBeforeItem(
    index: Int,
    dragTargetIndex: MutableState<Int?>,
) {
    if (index == 0 && dragTargetIndex.value == -1) {
        Divider(
            thickness = 3.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun DividerAfterItem(
    index: Int,
    isDragging: State<Boolean>,
    dragTargetIndex: MutableState<Int?>,
) {
    if (dragTargetIndex.value == index) {
        Divider(
            thickness = 3.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    } else if (isDragging.value) {
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        )
    } else {
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        )
    }
}

private fun calculateItemsToSwap(
    itemIndex: Int,
    itemsCount: Int,
    offsetY: Float,
    itemHeights: Map<Int, Float>,
): Pair<Int, Float> {
    val thisItemHeight: Float = itemHeights[itemIndex] ?: return 0 to 0f
    var swapped = 0
    var movedBy = 0f
    var overlapY: Float = abs(offsetY)
    when {
        offsetY < 0 -> {
            while (true) {
                val newPosition: Int = itemIndex + swapped
                if (newPosition <= 0) return swapped to movedBy
                val nextItemHeight = itemHeights[newPosition - 1] ?: thisItemHeight // guess the height is the same
                if (overlapY <= thisItemHeight / 2 + nextItemHeight / 2)
                    return swapped to movedBy
                overlapY -= nextItemHeight
                movedBy -= nextItemHeight
                swapped -= 1
            }
        }
        offsetY > 0 -> {
            while (true) {
                val newPosition: Int = itemIndex + swapped
                if (newPosition >= itemsCount - 1) return swapped to movedBy
                val nextItemHeight = itemHeights[newPosition + 1] ?: thisItemHeight
                if (overlapY <= thisItemHeight / 2 + nextItemHeight / 2)
                    return swapped to movedBy
                overlapY -= nextItemHeight
                movedBy += nextItemHeight
                swapped += 1
            }
        }
        else -> return 0 to 0f
    }
}
