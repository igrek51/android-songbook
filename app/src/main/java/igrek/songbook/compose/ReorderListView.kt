package igrek.songbook.compose

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.math.abs
import kotlin.math.roundToInt


class ItemsContainer<T>(
    var items: MutableList<T> = mutableListOf(),
    val modifiedMap: MutableMap<Int, MutableState<Long>> = mutableMapOf(),
    val modifiedAll: MutableState<Long> = mutableStateOf(0),
) {
    fun replaceAll(newList: MutableList<T>) {
        items = newList
        items.indices.forEach { index: Int ->
            if (!modifiedMap.containsKey(index)) {
                modifiedMap[index] = mutableStateOf(0)
            }
        }
        modifiedAll.value += 1
    }

    fun notifyItemChange(index: Int) {
        modifiedMap.getValue(index).value += 1
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun <T> ReorderListView(
    itemsContainer: ItemsContainer<T>,
    scrollState: ScrollState = rememberScrollState(),
    onReorder: (newItems: MutableList<T>) -> Unit,
    itemContent: @Composable (itemsContainer: ItemsContainer<T>, index: Int, modifier: Modifier, reorderButtonModifier: Modifier) -> Unit,
) {
    val draggingIndex: MutableState<Int> = remember { mutableStateOf(-1) }
    val dragTargetIndex: MutableState<Int?> = remember { mutableStateOf(null) }
    val itemHeights: MutableMap<Int, Float> = remember { mutableStateMapOf() }
    val itemAnimatedOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>> = remember { mutableStateMapOf() }
    val scrollDiff: MutableState<Float> = remember { mutableStateOf(0f) }
    val parentViewportHeight: MutableState<Float> = remember { mutableStateOf(0f) }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val scrollJob: MutableState<Job?> = remember { mutableStateOf(null) }
    val reorderButtonModifiers: MutableMap<Int, Modifier> = remember { mutableStateMapOf() }
    val isDraggingMes: MutableMap<Int, State<Boolean>> = remember { mutableStateMapOf() }
    val isDragTargetMes: MutableMap<Int, State<Boolean>> = remember { mutableStateMapOf() }

    val isDragging: State<Boolean> = derivedStateOf {
        draggingIndex.value != -1
    }
    val isDragTargetFirst: State<Boolean> = derivedStateOf {
        dragTargetIndex.value == -1
    }

    itemsContainer.items.indices.forEach { index: Int ->
        reorderButtonModifiers[index] = Modifier.createReorderButtonModifier(
            itemsContainer, index, draggingIndex, dragTargetIndex, itemHeights, itemAnimatedOffsets,
            scrollState, scrollDiff, parentViewportHeight, coroutineScope, scrollJob,
            onReorder,
        )

        itemAnimatedOffsets[index] = remember { Animatable(0f) }

        isDraggingMes[index] = derivedStateOf {
            draggingIndex.value == index
        }

        isDragTargetMes[index] = derivedStateOf {
            dragTargetIndex.value == index
        }
    }

    key(itemsContainer.modifiedAll.value) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .onGloballyPositioned { coordinates: LayoutCoordinates ->
                    parentViewportHeight.value =
                        coordinates.parentLayoutCoordinates?.size?.height?.toFloat() ?: 0f
                },
        ) {
            ReorderListColumn(
                itemsContainer,
                isDragging, isDragTargetFirst, isDraggingMes, isDragTargetMes,
                itemHeights, itemAnimatedOffsets,
                scrollDiff, reorderButtonModifiers, itemContent,
            )
        }
    }
}


@Composable
fun <T> ReorderListColumn(
    itemsContainer: ItemsContainer<T>,
    isDragging: State<Boolean>,
    isDragTargetFirst: State<Boolean>,
    isDraggingMes: Map<Int, State<Boolean>>,
    isDragTargetMes: Map<Int, State<Boolean>>,
    itemHeights: MutableMap<Int, Float>,
    itemAnimatedOffsets: Map<Int, Animatable<Float, AnimationVector1D>>,
    scrollDiff: State<Float>,
    reorderButtonModifiers: Map<Int, Modifier>,
    itemContent: @Composable (itemsContainer: ItemsContainer<T>, index: Int, modifier: Modifier, reorderButtonModifier: Modifier) -> Unit,
) {
    DividerBeforeItem(isDragTargetFirst)

//  logger.debug("recompose all items")
    itemsContainer.items.indices.forEach { index: Int ->
        ReorderListViewItem(
            itemsContainer, index,
            isDragging, isDraggingMes.getValue(index), isDragTargetMes.getValue(index),
            itemAnimatedOffsets.getValue(index), reorderButtonModifiers.getValue(index),
            itemHeights, scrollDiff, itemContent,
        )
    }
}

@SuppressLint("ModifierParameter")
@Composable
private fun <T> ReorderListViewItem(
    itemsContainer: ItemsContainer<T>,
    index: Int,
    isDragging: State<Boolean>,
    isDraggingMe: State<Boolean>,
    isDragTargetMe: State<Boolean>,
    offsetYAnimated: Animatable<Float, AnimationVector1D>,
    reorderButtonModifier: Modifier,
    itemHeights: MutableMap<Int, Float>,
    scrollDiff: State<Float>,
    itemContent: @Composable (itemsContainer: ItemsContainer<T>, index: Int, modifier: Modifier, reorderButtonModifier: Modifier) -> Unit,
) {
    key(itemsContainer.modifiedMap.getValue(index).value) {
        //logger.debug("recompose item $index")

        var itemModifier = Modifier
            .offset { IntOffset(0, offsetYAnimated.value.roundToInt()) }
            .fillMaxWidth()
            .onGloballyPositioned { coordinates: LayoutCoordinates ->
                itemHeights[index] = coordinates.size.height.toFloat()
            }
        if (isDraggingMe.value) {
            itemModifier = itemModifier
                .offset { IntOffset(0, scrollDiff.value.roundToInt()) }
                .background(Color.LightGray.copy(alpha = 0.15f))
        }

        itemContent(itemsContainer, index, itemModifier, reorderButtonModifier)

        DividerAfterItem(isDragging, isDragTargetMe)
    }
}

@Composable
private fun DividerBeforeItem(
    isDragTargetFirst: State<Boolean>,
) {
    if (isDragTargetFirst.value) {
        Divider(
            thickness = 3.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun DividerAfterItem(
    isDragging: State<Boolean>,
    isDragTargetMe: State<Boolean>,
) {
    if (isDragTargetMe.value) {
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

private fun <T> Modifier.createReorderButtonModifier(
    itemsContainer: ItemsContainer<T>,
    index: Int,
    draggingIndex: MutableState<Int>,
    dragTargetIndex: MutableState<Int?>,
    itemHeights: MutableMap<Int, Float>,
    itemAnimatedOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>>,
    scrollState: ScrollState,
    scrollDiff: MutableState<Float>,
    parentViewportHeight: MutableState<Float>,
    coroutineScope: CoroutineScope,
    scrollJob: MutableState<Job?>,
    onReorder: (newItems: MutableList<T>) -> Unit,
) = this.pointerInput(index) {
    detectDragGestures(

        onDragStart = { _: Offset ->
            draggingIndex.value = index
            dragTargetIndex.value = null
            scrollDiff.value = 0f
            coroutineScope.launch {
                itemAnimatedOffsets[index]?.snapTo(0f)
            }
        },

        onDragEnd = {
            val relativateOffset = (itemAnimatedOffsets[index]?.targetValue ?: 0f) + scrollDiff.value
            val (swapped, movedBy) = calculateItemsToSwap(index, itemsContainer.items.size, relativateOffset, itemHeights)
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
                    
                    itemsContainer.items.add(index + swapped, itemsContainer.items.removeAt(index))
                    for (i in minIndex..maxIndex) {
                        itemsContainer.notifyItemChange(i)
                    }
                    onReorder(itemsContainer.items)
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
                    
                    itemsContainer.items.add(index + swapped, itemsContainer.items.removeAt(index))
                    for (i in minIndex..maxIndex) {
                        itemsContainer.notifyItemChange(i)
                    }
                    onReorder(itemsContainer.items)
                }
                else -> coroutineScope.launch {
                    itemAnimatedOffsets[index]?.animateTo(0f)
                }
            }

            draggingIndex.value = -1
            dragTargetIndex.value = null
            scrollJob.value?.cancel()
            scrollJob.value = null
        },

        onDragCancel = {
            draggingIndex.value = -1
            dragTargetIndex.value = null
            scrollJob.value?.cancel()
            scrollJob.value = null
            coroutineScope.launch {
                itemAnimatedOffsets[index]?.animateTo(0f)
            }
        },

        onDrag = { change: PointerInputChange, dragAmount: Offset ->
            change.consume()

            scrollJob.value?.cancel()
            scrollJob.value = null

            val offsetYAnimatedVal = itemAnimatedOffsets[index]?.targetValue ?: 0f
            val relativateOffset = offsetYAnimatedVal + scrollDiff.value
            val thisHeight = itemHeights[index] ?: 0f

            val (swapped, _) = calculateItemsToSwap(index, itemsContainer.items.size, relativateOffset, itemHeights)
            dragTargetIndex.value = when {
                swapped < 0 -> index + swapped - 1
                swapped > 0 -> index + swapped
                else -> null
            }

            // overscroll
            var priorVisibleHeight = thisHeight / 2 - scrollState.value
            for (i in 0 until index) {
                priorVisibleHeight += itemHeights[i] ?: 0f
            }
            val beyondVisibleHeight = parentViewportHeight.value - priorVisibleHeight
            val borderArea = thisHeight * 2.5f
            var overscrolledY = 0f
            when {
                offsetYAnimatedVal < 0 && scrollState.canScrollBackward -> {
                    val overscrolled = priorVisibleHeight + relativateOffset - borderArea
                    if (overscrolled < 0)
                        overscrolledY = overscrolled
                }
                offsetYAnimatedVal > 0 && scrollState.canScrollForward -> {
                    val overscrolled = -beyondVisibleHeight + relativateOffset + borderArea
                    if (overscrolled > 0)
                        overscrolledY = overscrolled
                }
            }

            coroutineScope.launch {
                itemAnimatedOffsets[index]?.snapTo(offsetYAnimatedVal + dragAmount.y)
            }

            if (overscrolledY != 0f) {
                val scrollBy = overscrolledY * 0.07f
                scrollJob.value = coroutineScope.launch {
                    while ((scrollState.canScrollForward && scrollBy > 0) || (scrollState.canScrollBackward && scrollBy < 0)) {
                        yield()
                        scrollDiff.value += scrollState.scrollBy(scrollBy)
                        delay(20)
                    }
                }
            }
        }
    )
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
