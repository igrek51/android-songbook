package igrek.songbook.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
fun <T> ReorderListView(
    items: MutableList<T>,
    itemContent: @Composable (item: T, reorderButtonModifier: Modifier) -> Unit,
) {
    val draggingIndex: MutableState<Int> = remember { mutableStateOf(-1) }
    val dragTargetIndex: MutableState<Int?> = remember { mutableStateOf(null) }
    val itemHeights: MutableMap<Int, Float> = remember { mutableStateMapOf() }
    val itemAnimatedOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>> = remember { mutableStateMapOf() }
    val scrollState: ScrollState = rememberScrollState()
    val scrollDiff: MutableState<Float> = remember { mutableStateOf(0f) }
    val parentViewportHeight: MutableState<Float> = remember { mutableStateOf(0f) }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val scrollJob: MutableState<Job?> = remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .onGloballyPositioned { coordinates: LayoutCoordinates ->
                parentViewportHeight.value = coordinates.parentLayoutCoordinates?.size?.height?.toFloat() ?: 0f
            },
    ) {
        for (index in items.indices) {
            val item = items[index]

            val offsetYAnimated: Animatable<Float, AnimationVector1D> = remember { Animatable(0f) }
            itemAnimatedOffsets[index] = offsetYAnimated
            val offsetY = offsetYAnimated.value.roundToInt() + when (index) {
                draggingIndex.value -> scrollDiff.value.roundToInt()
                else -> 0
            }

            var itemModifier = Modifier
                .offset { IntOffset(0, offsetY) }
                .fillMaxWidth()
                .onGloballyPositioned { coordinates: LayoutCoordinates ->
                    itemHeights[index] = coordinates.size.height.toFloat()
                }
            if (draggingIndex.value == index) {
                itemModifier = itemModifier.background(Color.LightGray.copy(alpha = 0.2f))
            }

            val reorderButtonModifier = Modifier.createReorderButtonModifier(
                items, index, draggingIndex, dragTargetIndex, itemHeights, itemAnimatedOffsets,
                scrollState, scrollDiff, parentViewportHeight, coroutineScope, scrollJob,
                offsetYAnimated,
            )

            Box(modifier = itemModifier) {
                itemContent(item, reorderButtonModifier)
            }

            if (dragTargetIndex.value == index) {
                Divider(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
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
    scrollState: ScrollState,
    scrollDiff: MutableState<Float>,
    parentViewportHeight: MutableState<Float>,
    coroutineScope: CoroutineScope,
    scrollJob: MutableState<Job?>,
    offsetYAnimated: Animatable<Float, AnimationVector1D>,
) = this.pointerInput(index) {
    detectDragGestures(

        onDragStart = { _: Offset ->
            draggingIndex.value = index
            dragTargetIndex.value = null
            scrollDiff.value = 0f
            coroutineScope.launch {
                offsetYAnimated.snapTo(0f)
            }
        },

        onDragEnd = {
            val relativateOffset = offsetYAnimated.targetValue + scrollDiff.value
            val (swapped, movedBy) = calculateItemsToSwap(index, items.size, relativateOffset, itemHeights)
            val minIndex = Integer.min(index, index + swapped)
            val maxIndex = Integer.max(index, index + swapped)
            val endOffset = offsetYAnimated.targetValue + scrollDiff.value - movedBy
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
                }
                else -> coroutineScope.launch {
                    offsetYAnimated.animateTo(0f)
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
                offsetYAnimated.animateTo(0f)
            }
        },

        onDrag = { change: PointerInputChange, dragAmount: Offset ->
            change.consume()

            scrollJob.value?.cancel()
            scrollJob.value = null

            val relativateOffset = offsetYAnimated.targetValue + scrollDiff.value
            val thisHeight = itemHeights[index] ?: 0f

            val (swapped, _) = calculateItemsToSwap(index, items.size, relativateOffset, itemHeights)
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
                offsetYAnimated.targetValue < 0 && scrollState.canScrollBackward -> {
                    val overscrolled = priorVisibleHeight + relativateOffset - borderArea
                    if (overscrolled < 0)
                        overscrolledY = overscrolled
                }
                offsetYAnimated.targetValue > 0 && scrollState.canScrollForward -> {
                    val overscrolled = -beyondVisibleHeight + relativateOffset + borderArea
                    if (overscrolled > 0)
                        overscrolledY = overscrolled
                }
            }

            coroutineScope.launch {
                offsetYAnimated.snapTo(offsetYAnimated.targetValue + dragAmount.y)
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
