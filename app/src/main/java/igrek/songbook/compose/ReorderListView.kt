package igrek.songbook.compose

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import igrek.songbook.info.logger.LoggerFactory.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

val itemBorderStroke = BorderStroke(0.5.dp, colorItemListBorder)


class ItemsContainer<T>(
    var items: MutableList<T> = mutableListOf(),
    val modifiedMap: MutableMap<Int, MutableState<Long>> = mutableMapOf(),
    val modifiedAll: MutableState<Long> = mutableStateOf(0),
    val itemHeights: MutableMap<Int, Float> = mutableMapOf(),
    val itemBiasOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>> = mutableMapOf(),
    val itemStablePositions: MutableMap<Int, Animatable<Float, AnimationVector1D>> = mutableMapOf(), // ID to position offset
    val reorderButtonModifiers: MutableMap<Int, Modifier> = mutableMapOf(),
    val itemModifiers: MutableMap<Int, Modifier> = mutableMapOf(),
    val isDraggingMes: MutableMap<Int, State<Boolean>> = mutableMapOf(),
    val indexToPositionMap: MutableMap<Int, Int> = mutableMapOf(), // item index (ID) on list to real displayed position index
    val positionToIndexMap: MutableMap<Int, Int> = mutableMapOf(), // real displayed index to item index (ID) on list
    var totalRelativeSwapOffset: Float = 0f,
    val overscrollDiff: MutableState<Float> = mutableStateOf(0f),
    val parentViewportWidth: MutableState<Float> = mutableStateOf(0f),
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

    fun notifyItemChange(position: Int) {
        val index = positionToIndexMap[position] ?: return
        modifiedMap.getValue(index).value += 1
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun <T> ReorderListView(
    itemsContainer: ItemsContainer<T>,
    scrollState: ScrollState = rememberScrollState(),
    onReorder: (newItems: MutableList<T>) -> Unit,
    onLoad: () -> Unit,
    itemContent: @Composable (itemsContainer: ItemsContainer<T>, id: Int, modifier: Modifier) -> Unit,
) {
    val draggingIndex: MutableState<Int> = remember { mutableStateOf(-1) }
    val parentViewportHeight: MutableState<Float> = remember { mutableStateOf(0f) }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val scrollJob: MutableState<Job?> = remember { mutableStateOf(null) }

    itemsContainer.items.indices.forEach { index: Int ->
        itemsContainer.indexToPositionMap[index] = index
        itemsContainer.positionToIndexMap[index] = index

        itemsContainer.itemBiasOffsets[index] = Animatable(0f)
        itemsContainer.itemStablePositions[index] = Animatable(0f)

        itemsContainer.isDraggingMes[index] = derivedStateOf {
            draggingIndex.value == index
        }
        if (!itemsContainer.reorderButtonModifiers.containsKey(index)) {
            itemsContainer.reorderButtonModifiers[index] = Modifier.createReorderButtonModifier(
                itemsContainer, index, draggingIndex, scrollState, parentViewportHeight,
                coroutineScope, scrollJob, onReorder,
            )
        }
        itemsContainer.itemModifiers[index] = Modifier.createItemModifier(
            itemsContainer, index,
        )
    }

    key(itemsContainer.modifiedAll.value) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .onGloballyPositioned { coordinates: LayoutCoordinates ->
                    parentViewportHeight.value =
                        coordinates.parentLayoutCoordinates?.size?.height?.toFloat() ?: 0f
                    itemsContainer.parentViewportWidth.value =
                        coordinates.parentLayoutCoordinates?.size?.width?.toFloat() ?: 0f
                },
        ) {

            logger.debug("recomposing all items")
            itemsContainer.items.indices.forEach { index: Int ->
                ReorderListViewItem(itemsContainer, index, itemContent)
            }
        }
    }

    LaunchedEffect(itemsContainer.modifiedAll.value) {
        onLoad()
    }
}

@Composable
private fun <T> ReorderListViewItem(
    itemsContainer: ItemsContainer<T>,
    index: Int,
    itemContent: @Composable (itemsContainer: ItemsContainer<T>, id: Int, modifier: Modifier) -> Unit,
) {
    logger.debug("recomposing item $index")
    key(itemsContainer.modifiedMap.getValue(index).value) {
        val itemModifier = itemsContainer.itemModifiers.getValue(index)
        itemContent(itemsContainer, index, itemModifier)
    }
}

private fun <T> Modifier.createItemModifier(
    itemsContainer: ItemsContainer<T>,
    index: Int,
): Modifier {
    val stablePosition: Animatable<Float, AnimationVector1D> = itemsContainer.itemStablePositions.getValue(index)
    val offsetBias: Animatable<Float, AnimationVector1D> = itemsContainer.itemBiasOffsets.getValue(index)
    val isDraggingMe: State<Boolean> = itemsContainer.isDraggingMes.getValue(index)
    return this
        .offset { IntOffset(0, stablePosition.value.roundToInt() + offsetBias.value.roundToInt()) }
        .fillMaxWidth()
        .border(itemBorderStroke)
        .onGloballyPositioned { coordinates: LayoutCoordinates ->
            itemsContainer.itemHeights[index] = coordinates.size.height.toFloat()
        }
        .drawBehind {
            if (isDraggingMe.value) {
                drawRect(color = colorItemDraggedBackground)
            }
        }
}

private fun <T> Modifier.createReorderButtonModifier(
    itemsContainer: ItemsContainer<T>,
    index: Int,
    draggingIndex: MutableState<Int>,
    scrollState: ScrollState,
    parentViewportHeight: MutableState<Float>,
    coroutineScope: CoroutineScope,
    scrollJob: MutableState<Job?>,
    onReorder: (newItems: MutableList<T>) -> Unit,
) = this.pointerInput(index) {
    detectDragGestures(

        onDragStart = { _: Offset ->
            draggingIndex.value = index
            itemsContainer.overscrollDiff.value = 0f
            itemsContainer.totalRelativeSwapOffset = 0f
            coroutineScope.launch {
                itemsContainer.itemBiasOffsets[index]?.snapTo(0f)
            }
        },

        onDrag = { change: PointerInputChange, dragAmount: Offset ->
            change.consume()

            scrollJob.value?.cancel()
            scrollJob.value = null

            var offsetBias: Float = itemsContainer.itemBiasOffsets[index]?.targetValue ?: 0f // relative offset
            val thisHeight = itemsContainer.itemHeights[index] ?: 0f
            var position = itemsContainer.indexToPositionMap.getValue(index) // real positional index on view
            val draggedId: Int = index

            // minimize overlap by moving item when it's half-covered
            val swappedBy: Int = calculateItemsToSwap(
                index, position, offsetBias, itemsContainer.items.size, itemsContainer.itemHeights,
                itemsContainer.positionToIndexMap,
            )

            // reorder overlapped items in temporary maps
            when {
                swappedBy < 0 -> { // moving up
                    var draggedPxDelta = 0f
                    for(swapStep in 1..-swappedBy) {
                        val swappedPosition = position - swapStep // position of item being swapped
                        val newPosition = swappedPosition + 1
                        val swappedId = itemsContainer.positionToIndexMap.getValue(swappedPosition)
                        draggedPxDelta += itemsContainer.itemHeights[swappedId] ?: 0f
                        val currentStablePosition = itemsContainer.itemStablePositions[swappedId]?.targetValue ?: 0f
                        coroutineScope.launch {
                            itemsContainer.itemStablePositions[swappedId]?.animateTo(currentStablePosition + thisHeight)
                        }
                        itemsContainer.indexToPositionMap[swappedId] = newPosition
                        itemsContainer.positionToIndexMap[newPosition] = swappedId
                    }

                    position += swappedBy
                    itemsContainer.indexToPositionMap[draggedId] = position
                    itemsContainer.positionToIndexMap[position] = draggedId
                    val newStablePosition = (itemsContainer.itemStablePositions[draggedId]?.targetValue ?: 0f) - draggedPxDelta
                    offsetBias += draggedPxDelta
                    coroutineScope.launch {
                        itemsContainer.itemStablePositions[draggedId]?.snapTo(newStablePosition)
                    }
                }

                swappedBy > 0 -> { // moving down
                    var draggedPxDelta = 0f
                    for(swapStep in 1..swappedBy) {
                        val swappedPosition = position + swapStep // position of item being swapped
                        val newPosition = swappedPosition - 1
                        val swappedId = itemsContainer.positionToIndexMap.getValue(swappedPosition)
                        draggedPxDelta += itemsContainer.itemHeights[swappedId] ?: 0f
                        val currentStablePosition = itemsContainer.itemStablePositions[swappedId]?.targetValue ?: 0f
                        coroutineScope.launch {
                            itemsContainer.itemStablePositions[swappedId]?.animateTo(currentStablePosition - thisHeight)
                        }
                        itemsContainer.indexToPositionMap[swappedId] = newPosition
                        itemsContainer.positionToIndexMap[newPosition] = swappedId
                    }

                    position += swappedBy
                    itemsContainer.indexToPositionMap[draggedId] = position
                    itemsContainer.positionToIndexMap[position] = draggedId
                    val newStablePosition = (itemsContainer.itemStablePositions[draggedId]?.targetValue ?: 0f) + draggedPxDelta
                    offsetBias -= draggedPxDelta
                    coroutineScope.launch {
                        itemsContainer.itemStablePositions[draggedId]?.snapTo(newStablePosition)
                    }
                }
            }

            itemsContainer.totalRelativeSwapOffset += dragAmount.y
            coroutineScope.launch {
                itemsContainer.itemBiasOffsets[draggedId]?.snapTo(offsetBias + dragAmount.y)
            }

            // overscroll
            var priorVisibleHeight = thisHeight / 2 - scrollState.value
            for (i in 0 until position) {
                val itemId = itemsContainer.positionToIndexMap.getValue(i)
                priorVisibleHeight += itemsContainer.itemHeights[itemId] ?: 0f
            }
            val beyondVisibleHeight = parentViewportHeight.value - priorVisibleHeight
            val borderArea = parentViewportHeight.value * 0.18f
            val overscrolledTop = priorVisibleHeight + offsetBias - borderArea
            val overscrolledBottom = -beyondVisibleHeight + offsetBias + borderArea
            val movedABit: Boolean = (itemsContainer.totalRelativeSwapOffset + itemsContainer.overscrollDiff.value).absoluteValue > thisHeight
            val overscrolledY: Float = when {
                (itemsContainer.totalRelativeSwapOffset < 0 || movedABit) && overscrolledTop < 0 && scrollState.canScrollBackward -> {
                    overscrolledTop
                }
                (itemsContainer.totalRelativeSwapOffset > 0 || movedABit) && overscrolledBottom > 0 && scrollState.canScrollForward -> {
                    overscrolledBottom
                }
                else -> 0f
            }

            if (overscrolledY != 0f) {
                val scrollBy = overscrolledY * 0.07f
                scrollJob.value = coroutineScope.launch {
                    while ((scrollState.canScrollForward && scrollBy > 0) || (scrollState.canScrollBackward && scrollBy < 0)) {
                        yield()
                        val scrollDelta = scrollState.scrollBy(scrollBy)
                        itemsContainer.overscrollDiff.value += scrollDelta
                        itemsContainer.itemBiasOffsets[draggedId]?.snapTo((itemsContainer.itemBiasOffsets[draggedId]?.targetValue ?: 0f) + scrollDelta)
                        delay(20)
                    }
                }
            }
        },

        onDragEnd = {
            persistSwappedItems(itemsContainer, onReorder)
            draggingIndex.value = -1
            scrollJob.value?.cancel()
            scrollJob.value = null
            coroutineScope.launch {
                itemsContainer.itemBiasOffsets[index]?.animateTo(0f)
            }
        },

        onDragCancel = {
            persistSwappedItems(itemsContainer, onReorder)
            draggingIndex.value = -1
            scrollJob.value?.cancel()
            scrollJob.value = null
            coroutineScope.launch {
                itemsContainer.itemBiasOffsets[index]?.animateTo(0f)
            }
        },
    )
}

private fun calculateItemsToSwap(
    itemIndex: Int, // ID
    position: Int, // real item position
    offsetY: Float, // relativate Offset
    itemsCount: Int,
    itemHeights: Map<Int, Float>,
    positionToIndexMap: MutableMap<Int, Int>, // real displayed index to item index (ID) on list
): Int {
    // Return swapped positions and corresponding pixels
    val thisItemHeight: Float = itemHeights[itemIndex] ?: return 0
    var swappedBy = 0
    var overlapY: Float = abs(offsetY)
    when {
        offsetY < 0 -> { // moving up
            while (true) {
                val currentPosition: Int = position + swappedBy
                if (currentPosition <= 0)
                    return swappedBy
                val nextItemId = positionToIndexMap.getValue(currentPosition - 1)
                val nextItemHeight = itemHeights[nextItemId] ?: thisItemHeight // guess the height is the same
                if (overlapY <= nextItemHeight / 2)
                    return swappedBy
                swappedBy -= 1
                overlapY -= nextItemHeight
            }
        }
        offsetY > 0 -> { // moving down
            while (true) {
                val currentPosition: Int = position + swappedBy
                if (currentPosition >= itemsCount - 1)
                    return swappedBy
                val nextItemId = positionToIndexMap.getValue(currentPosition + 1)
                val nextItemHeight = itemHeights[nextItemId] ?: thisItemHeight
                if (overlapY <= nextItemHeight / 2)
                    return swappedBy
                swappedBy += 1
                overlapY -= nextItemHeight
            }
        }
        else -> return 0
    }
}

private fun <T> persistSwappedItems(
    itemsContainer: ItemsContainer<T>,
    onReorder: (newItems: MutableList<T>) -> Unit,
) {
    val changesMade = itemsContainer.items.indices.any { index: Int ->
        val position = itemsContainer.indexToPositionMap.getValue(index)
        position != index
    }
    if (!changesMade) return

    val indicesNewOrder = itemsContainer.items.indices.map { position: Int ->
        itemsContainer.positionToIndexMap.getValue(position)
    }
    if (indicesNewOrder.distinct().size != itemsContainer.items.size)
        throw RuntimeException("new indices don't contain the same original indices")

    val newItems: MutableList<T> = indicesNewOrder.map { index: Int ->
        itemsContainer.items[index]
    }.toMutableList()

    onReorder(newItems)
}
