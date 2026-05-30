package com.example.inspixmobile.presentation.component

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

@Immutable
sealed class MasonryItemSpan {
    data object Single : MasonryItemSpan()
    data object FullLine : MasonryItemSpan()
}

@Immutable
sealed class MasonryItemEstimate {
    data class Fixed(val height: Dp) : MasonryItemEstimate()
    data class AspectRatio(val ratio: Float) : MasonryItemEstimate()
}

// Keys are required to keep item state stable for paging + shared transitions.
interface VerticalMasonryGridScope {
    fun item(
        key: Any,
        contentType: Any? = null,
        span: MasonryItemSpan = MasonryItemSpan.Single,
        estimatedHeight: Dp? = null,
        aspectRatio: Float? = null,
        content: @Composable () -> Unit
    )

    fun items(
        count: Int,
        key: (index: Int) -> Any,
        contentType: (index: Int) -> Any? = { null },
        span: (index: Int) -> MasonryItemSpan = { MasonryItemSpan.Single },
        estimatedHeight: (index: Int) -> Dp? = { null },
        aspectRatio: (index: Int) -> Float? = { null },
        itemContent: @Composable (index: Int) -> Unit
    )

    fun <T> items(
        items: List<T>,
        key: (item: T) -> Any,
        contentType: (item: T) -> Any? = { null },
        span: (item: T) -> MasonryItemSpan = { MasonryItemSpan.Single },
        estimatedHeight: (item: T) -> Dp? = { null },
        aspectRatio: (item: T) -> Float? = { null },
        itemContent: @Composable (item: T) -> Unit
    )
}

@Stable
class VerticalMasonryGridState internal constructor(
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0
) {
    internal val columnStates = mutableStateListOf<LazyListState>()
    internal val columnItems = mutableStateListOf<MutableList<MasonryColumnItem>>()
    internal val fullSpanItems = mutableStateListOf<MasonryFullSpanPlacement>()
    internal val layoutCache = MasonryLayoutCache()
    internal val measuredFullSpanHeightsPx = mutableStateMapOf<Any, Int>()
    internal var layoutSeed by mutableIntStateOf(0)

    internal var scrollOffsetPx by mutableStateOf(0f)
        private set

    var firstVisibleItemIndex by mutableIntStateOf(initialFirstVisibleItemIndex)
        internal set
    var firstVisibleItemScrollOffset by mutableIntStateOf(initialFirstVisibleItemScrollOffset)
        internal set

    private var scrollableState: ScrollableState? = null
    private var pendingScroll: PendingScroll? =
        if (initialFirstVisibleItemIndex != 0 || initialFirstVisibleItemScrollOffset != 0) {
            PendingScroll(
                index = initialFirstVisibleItemIndex,
                scrollOffset = initialFirstVisibleItemScrollOffset,
                animate = false
            )
        } else {
            null
        }

    internal fun ensureColumnCount(columns: Int) {
        if (columnStates.size == columns) return
        columnStates.clear()
        columnItems.clear()
        repeat(columns) {
            columnStates.add(LazyListState())
            columnItems.add(mutableStateListOf())
        }
        fullSpanItems.clear()
        measuredFullSpanHeightsPx.clear()
        layoutSeed++
        layoutCache.reset(columns)
    }

    internal fun attachScrollableState(state: ScrollableState) {
        scrollableState = state
    }

    internal fun dispatchRawDelta(delta: Float): Float {
        val leader = columnStates.firstOrNull() ?: return 0f
        val consumed = leader.dispatchRawDelta(delta)
        if (consumed != 0f) {
            // Keep columns pixel-synced by forwarding the same delta to all lists.
            for (index in 1 until columnStates.size) {
                columnStates[index].dispatchRawDelta(consumed)
            }
            scrollOffsetPx += consumed
        }
        return consumed
    }

    internal fun updateMeasuredFullSpanHeight(key: Any, heightPx: Int) {
        val current = measuredFullSpanHeightsPx[key]
        if (current == null || abs(current - heightPx) > 1) {
            measuredFullSpanHeightsPx[key] = heightPx
            layoutSeed++
        }
    }

    internal suspend fun applyPendingScrollIfAny() {
        val pending = pendingScroll ?: return
        pendingScroll = null
        performScrollTo(
            index = pending.index,
            scrollOffset = pending.scrollOffset,
            animate = pending.animate
        )
    }

    suspend fun scrollToItem(index: Int, scrollOffset: Int = 0) {
        performScrollTo(index, scrollOffset, animate = false)
    }

    suspend fun animateScrollToItem(index: Int, scrollOffset: Int = 0) {
        performScrollTo(index, scrollOffset, animate = true)
    }

    private suspend fun performScrollTo(
        index: Int,
        scrollOffset: Int,
        animate: Boolean
    ) {
        val targetOffset = layoutCache.itemTopOffset(index) ?: run {
            pendingScroll = PendingScroll(index, scrollOffset, animate)
            return
        }
        val delta = (targetOffset + scrollOffset) - scrollOffsetPx
        val scrollState = scrollableState ?: run {
            pendingScroll = PendingScroll(index, scrollOffset, animate)
            return
        }
        if (animate) {
            scrollState.animateScrollBy(delta)
        } else {
            scrollState.scrollBy(delta)
        }
    }

    private data class PendingScroll(
        val index: Int,
        val scrollOffset: Int,
        val animate: Boolean
    )

    companion object {
        val Saver: Saver<VerticalMasonryGridState, List<Int>> = Saver(
            save = { listOf(it.firstVisibleItemIndex, it.firstVisibleItemScrollOffset) },
            restore = { restored ->
                VerticalMasonryGridState(
                    initialFirstVisibleItemIndex = restored[0],
                    initialFirstVisibleItemScrollOffset = restored[1]
                )
            }
        )
    }
}

@Composable
fun rememberVerticalMasonryGridState(): VerticalMasonryGridState {
    return rememberSaveable(saver = VerticalMasonryGridState.Saver) {
        VerticalMasonryGridState()
    }
}

@Composable
fun VerticalMasonryGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    state: VerticalMasonryGridState = rememberVerticalMasonryGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalItemSpacing: Dp = 0.dp,
    horizontalItemSpacing: Dp = 0.dp,
    userScrollEnabled: Boolean = true,
    content: VerticalMasonryGridScope.() -> Unit
) {
    require(columns > 0) { "VerticalMasonryGrid requires at least 1 column." }

    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val resolvedHorizontalSpacing = if (columns > 1) horizontalItemSpacing else 0.dp

    val scrollableState = rememberScrollableState { delta ->
        state.dispatchRawDelta(delta)
    }
    val containerModifier = if (userScrollEnabled) {
        // Match LazyColumn drag direction.
        modifier.scrollable(
            state = scrollableState,
            orientation = Orientation.Vertical,
            reverseDirection = true
        )
    } else {
        modifier
    }

    BoxWithConstraints(modifier = containerModifier) {
        val startPadding = contentPadding.calculateLeftPadding(layoutDirection)
        val endPadding = contentPadding.calculateRightPadding(layoutDirection)
        val topPadding = contentPadding.calculateTopPadding()

        val availableWidth =
            maxWidth - startPadding - endPadding - (resolvedHorizontalSpacing * (columns - 1))
        val columnWidth = if (columns > 0) availableWidth / columns else 0.dp
        val fullWidth = maxWidth - startPadding - endPadding

        val columnWidthPx = with(density) { columnWidth.roundToPx() }
        val fullWidthPx = with(density) { fullWidth.roundToPx() }
        val verticalSpacingPx = with(density) { verticalItemSpacing.roundToPx() }
        val topPaddingPx = with(density) { topPadding.roundToPx() }

        val scope = remember { VerticalMasonryGridScopeImpl() }
        scope.reset()
        scope.content()
        val itemProvider = MasonryItemProvider(scope.intervals)

        state.ensureColumnCount(columns)
        // Layout is precomputed with estimated heights so we can assign items without measuring.
        // This keeps append cheap and avoids re-creating the whole column model on paging updates.
        state.layoutCache.updateLayout(
            columns = columns,
            columnWidthPx = columnWidthPx,
            fullWidthPx = fullWidthPx,
            verticalSpacingPx = verticalSpacingPx,
            density = density.density,
            layoutSeed = state.layoutSeed,
            measuredFullSpanHeightsPx = state.measuredFullSpanHeightsPx,
            provider = itemProvider,
            columnItems = state.columnItems,
            fullSpanItems = state.fullSpanItems
        )

        LaunchedEffect(scrollableState) {
            state.attachScrollableState(scrollableState)
        }

        LaunchedEffect(state) {
            snapshotFlow {
                computeFirstVisibleItem(
                    columnStates = state.columnStates,
                    columnItems = state.columnItems
                )
            }.collect { (index, offset) ->
                state.firstVisibleItemIndex = index
                state.firstVisibleItemScrollOffset = offset
            }
        }

        LaunchedEffect(state) {
            snapshotFlow { state.layoutCache.layoutVersion }.collect {
                state.applyPendingScrollIfAny()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            MasonryColumns(
                columns = columns,
                columnItems = state.columnItems,
                columnStates = state.columnStates,
                provider = itemProvider,
                contentPadding = contentPadding,
                horizontalItemSpacing = resolvedHorizontalSpacing,
                verticalItemSpacing = verticalItemSpacing
            )

            FullSpanOverlay(
                placements = state.fullSpanItems,
                provider = itemProvider,
                contentPadding = contentPadding,
                layoutDirection = layoutDirection,
                state = state,
                topPaddingPx = topPaddingPx
            )
        }
    }
}

@Composable
private fun MasonryColumns(
    columns: Int,
    columnItems: List<MutableList<MasonryColumnItem>>,
    columnStates: List<LazyListState>,
    provider: MasonryItemProvider,
    contentPadding: PaddingValues,
    horizontalItemSpacing: Dp,
    verticalItemSpacing: Dp
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(horizontalItemSpacing)
    ) {
        repeat(columns) { columnIndex ->
            val columnPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
                start = if (columnIndex == 0) {
                    contentPadding.calculateLeftPadding(layoutDirection)
                } else {
                    0.dp
                },
                end = if (columnIndex == columns - 1) {
                    contentPadding.calculateRightPadding(layoutDirection)
                } else {
                    0.dp
                }
            )

            LazyColumn(
                state = columnStates[columnIndex],
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = columnPadding,
                verticalArrangement = Arrangement.spacedBy(verticalItemSpacing),
                userScrollEnabled = false
            ) {
                items(
                    items = columnItems[columnIndex],
                    key = { it.key },
                    contentType = { item ->
                        if (item.isSpacer) {
                            MasonryContentType.Spacer
                        } else {
                            provider.getContentType(item.index)
                        }
                    }
                ) { item ->
                    if (item.isSpacer) {
                        Spacer(modifier = Modifier.height(with(density) { item.heightPx.toDp() }))
                    } else {
                        provider.Item(item.index)
                    }
                }
            }
        }
    }
}

@Composable
private fun FullSpanOverlay(
    placements: List<MasonryFullSpanPlacement>,
    provider: MasonryItemProvider,
    contentPadding: PaddingValues,
    layoutDirection: LayoutDirection,
    state: VerticalMasonryGridState,
    topPaddingPx: Int
) {
    if (placements.isEmpty()) return

    val density = LocalDensity.current
    val startPadding = contentPadding.calculateLeftPadding(layoutDirection)
    val endPadding = contentPadding.calculateRightPadding(layoutDirection)
    val offsetY = (topPaddingPx - state.scrollOffsetPx).roundToInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(unbounded = true)
            .padding(start = startPadding, end = endPadding)
            .offset { IntOffset(0, offsetY) }
    ) {
        // Full-span items are rendered in an overlay to keep the per-column LazyColumns simple
        // and avoid a custom LazyLayout. Spacer gaps align them with the masonry columns.
        var lastBottomPx = 0
        placements.forEach { placement ->
            val gapPx = placement.topOffsetPx - lastBottomPx
            if (gapPx > 0) {
                Spacer(modifier = Modifier.height(with(density) { gapPx.toDp() }))
            }
            key(placement.key) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged {
                            state.updateMeasuredFullSpanHeight(
                                placement.key,
                                it.height
                            )
                        }
                ) {
                    provider.Item(placement.index)
                }
            }
            lastBottomPx = placement.topOffsetPx + placement.estimatedHeightPx
        }
    }
}

private fun computeFirstVisibleItem(
    columnStates: List<LazyListState>,
    columnItems: List<List<MasonryColumnItem>>
): Pair<Int, Int> {
    var minIndex = Int.MAX_VALUE
    var minOffset = 0
    columnStates.forEachIndexed { columnIndex, listState ->
        val itemsInfo = listState.layoutInfo.visibleItemsInfo
        for (itemInfo in itemsInfo) {
            val columnItem = columnItems[columnIndex].getOrNull(itemInfo.index) ?: continue
            val globalIndex = if (columnItem.isSpacer) {
                columnItem.placeholderIndex ?: continue
            } else {
                columnItem.index
            }
            val effectiveOffset = if (columnItem.isSpacer) {
                itemInfo.offset + columnItem.contentOffsetPx
            } else {
                itemInfo.offset
            }
            if (globalIndex < minIndex) {
                minIndex = globalIndex
                minOffset = (-effectiveOffset).coerceAtLeast(0)
            }
            break
        }
    }
    return if (minIndex == Int.MAX_VALUE) 0 to 0 else minIndex to minOffset
}

@Immutable
internal data class MasonryColumnItem(
    val key: Any,
    val index: Int,
    val heightPx: Int,
    val isSpacer: Boolean,
    val placeholderIndex: Int? = null,
    val contentOffsetPx: Int = 0
)

@Immutable
internal data class MasonryFullSpanPlacement(
    val key: Any,
    val index: Int,
    val topOffsetPx: Int,
    val estimatedHeightPx: Int
)

@Immutable
private enum class MasonryContentType {
    Spacer
}

private class VerticalMasonryGridScopeImpl : VerticalMasonryGridScope {
    val intervals = ArrayList<MasonryInterval>()

    fun reset() {
        intervals.clear()
    }

    override fun item(
        key: Any,
        contentType: Any?,
        span: MasonryItemSpan,
        estimatedHeight: Dp?,
        aspectRatio: Float?,
        content: @Composable () -> Unit
    ) {
        val estimate = buildEstimate(estimatedHeight, aspectRatio)
        intervals.add(
            MasonryInterval(
                count = 1,
                key = { _ -> key },
                contentType = { _ -> contentType },
                span = { _ -> span },
                estimate = { _ -> estimate },
                item = { _ -> content() }
            )
        )
    }

    override fun items(
        count: Int,
        key: (index: Int) -> Any,
        contentType: (index: Int) -> Any?,
        span: (index: Int) -> MasonryItemSpan,
        estimatedHeight: (index: Int) -> Dp?,
        aspectRatio: (index: Int) -> Float?,
        itemContent: @Composable (index: Int) -> Unit
    ) {
        if (count <= 0) return
        intervals.add(
            MasonryInterval(
                count = count,
                key = key,
                contentType = contentType,
                span = span,
                estimate = { index -> buildEstimate(estimatedHeight(index), aspectRatio(index)) },
                item = itemContent
            )
        )
    }

    override fun <T> items(
        items: List<T>,
        key: (item: T) -> Any,
        contentType: (item: T) -> Any?,
        span: (item: T) -> MasonryItemSpan,
        estimatedHeight: (item: T) -> Dp?,
        aspectRatio: (item: T) -> Float?,
        itemContent: @Composable (item: T) -> Unit
    ) {
        if (items.isEmpty()) return
        intervals.add(
            MasonryInterval(
                count = items.size,
                key = { index -> key(items[index]) },
                contentType = { index -> contentType(items[index]) },
                span = { index -> span(items[index]) },
                estimate = { index ->
                    buildEstimate(
                        estimatedHeight(items[index]),
                        aspectRatio(items[index])
                    )
                },
                item = { index -> itemContent(items[index]) }
            )
        )
    }

    private fun buildEstimate(
        estimatedHeight: Dp?,
        aspectRatio: Float?
    ): MasonryItemEstimate? {
        return when {
            estimatedHeight != null -> MasonryItemEstimate.Fixed(estimatedHeight)
            aspectRatio != null && aspectRatio > 0f -> MasonryItemEstimate.AspectRatio(aspectRatio)
            else -> null
        }
    }
}

@Immutable
internal data class MasonryInterval(
    val count: Int,
    val key: (index: Int) -> Any,
    val contentType: (index: Int) -> Any?,
    val span: (index: Int) -> MasonryItemSpan,
    val estimate: (index: Int) -> MasonryItemEstimate?,
    val item: @Composable (index: Int) -> Unit
)

internal class MasonryItemProvider(
    private val intervals: List<MasonryInterval>
) {
    private val intervalStarts: IntArray

    val itemCount: Int

    init {
        var current = 0
        intervalStarts = IntArray(intervals.size)
        intervals.forEachIndexed { index, interval ->
            intervalStarts[index] = current
            current += interval.count
        }
        itemCount = current
    }

    fun getKey(globalIndex: Int): Any = withInterval(globalIndex) { interval, localIndex ->
        interval.key(localIndex)
    }

    fun getContentType(globalIndex: Int): Any? = withInterval(globalIndex) { interval, localIndex ->
        interval.contentType(localIndex)
    }

    fun getSpan(globalIndex: Int): MasonryItemSpan =
        withInterval(globalIndex) { interval, localIndex ->
            interval.span(localIndex)
        }

    fun getEstimate(globalIndex: Int): MasonryItemEstimate? =
        withInterval(globalIndex) { interval, localIndex ->
            interval.estimate(localIndex)
        }

    @Composable
    fun Item(globalIndex: Int) {
        withInterval(globalIndex) { interval, localIndex ->
            interval.item(localIndex)
        }
    }

    fun forEachItem(fromIndex: Int, block: (globalIndex: Int) -> Unit) {
        var currentIndex = 0
        intervals.forEach { interval ->
            val endIndex = currentIndex + interval.count
            if (endIndex <= fromIndex) {
                currentIndex = endIndex
                return@forEach
            }
            val localStart = (fromIndex - currentIndex).coerceAtLeast(0)
            for (localIndex in localStart until interval.count) {
                block(currentIndex + localIndex)
            }
            currentIndex = endIndex
        }
    }

    private inline fun <T> withInterval(
        globalIndex: Int,
        block: (interval: MasonryInterval, localIndex: Int) -> T
    ): T {
        var left = 0
        var right = intervalStarts.lastIndex
        while (left <= right) {
            val mid = (left + right) ushr 1
            val start = intervalStarts[mid]
            val end = if (mid == intervalStarts.lastIndex) itemCount else intervalStarts[mid + 1]
            when {
                globalIndex < start -> right = mid - 1
                globalIndex >= end -> left = mid + 1
                else -> {
                    val interval = intervals[mid]
                    return block(interval, globalIndex - start)
                }
            }
        }
        error("Index $globalIndex is out of bounds for itemCount=$itemCount")
    }
}

internal class MasonryLayoutCache {
    private var assignedCount = 0
    private var cachedColumns = 0
    private var cachedColumnWidthPx = 0
    private var cachedFullWidthPx = 0
    private var cachedVerticalSpacingPx = 0
    private var cachedDensity = 1f
    private var cachedLayoutSeed = 0

    private var sampleKeyFirst: Any? = null
    private var sampleKeyMid: Any? = null
    private var sampleKeyLast: Any? = null

    private var itemTopOffsets = IntArray(0)

    private var columnHeights = IntArray(0)

    var layoutVersion by mutableIntStateOf(0)
        private set

    fun reset(columns: Int) {
        assignedCount = 0
        cachedColumns = columns
        cachedColumnWidthPx = 0
        cachedFullWidthPx = 0
        cachedVerticalSpacingPx = 0
        cachedLayoutSeed = 0
        sampleKeyFirst = null
        sampleKeyMid = null
        sampleKeyLast = null
        itemTopOffsets = IntArray(0)
        columnHeights = IntArray(columns)
        layoutVersion++
    }

    fun updateLayout(
        columns: Int,
        columnWidthPx: Int,
        fullWidthPx: Int,
        verticalSpacingPx: Int,
        density: Float,
        layoutSeed: Int,
        measuredFullSpanHeightsPx: Map<Any, Int>,
        provider: MasonryItemProvider,
        columnItems: List<MutableList<MasonryColumnItem>>,
        fullSpanItems: MutableList<MasonryFullSpanPlacement>
    ) {
        val needsReset = columns != cachedColumns ||
                columnWidthPx != cachedColumnWidthPx ||
                fullWidthPx != cachedFullWidthPx ||
                verticalSpacingPx != cachedVerticalSpacingPx ||
                density != cachedDensity ||
                layoutSeed != cachedLayoutSeed ||
                !isPrefixStable(provider)

        if (needsReset) {
            columnItems.forEach { it.clear() }
            fullSpanItems.clear()
            columnHeights = IntArray(columns)
            assignedCount = 0
            cachedColumns = columns
            cachedColumnWidthPx = columnWidthPx
            cachedFullWidthPx = fullWidthPx
            cachedVerticalSpacingPx = verticalSpacingPx
            cachedDensity = density
            cachedLayoutSeed = layoutSeed
            layoutVersion++
        }

        if (provider.itemCount <= assignedCount) {
            if (needsReset) updateSamples(provider)
            return
        }

        ensureCapacity(provider.itemCount)

        // Incremental append: only place new items to avoid re-creating lists on paging append.
        provider.forEachItem(assignedCount) { globalIndex ->
            val span = provider.getSpan(globalIndex)
            val estimate = provider.getEstimate(globalIndex)
            val key = provider.getKey(globalIndex)

            if (span is MasonryItemSpan.FullLine) {
                val measuredHeightPx = measuredFullSpanHeightsPx[key]
                val estimatedHeightPx = measuredHeightPx ?: estimateHeightPx(
                    estimate = estimate,
                    widthPx = fullWidthPx,
                    density = density
                )

                val currentMaxHeight = columnHeights.maxOrNull() ?: 0
                val hasAnyItem = currentMaxHeight > 0
                val requiredSpacing = if (hasAnyItem) verticalSpacingPx else 0
                val fullSpanTop = currentMaxHeight + requiredSpacing

                repeat(columns) { columnIndex ->
                    val hasItemsInColumn = columnItems[columnIndex].isNotEmpty()
                    val spacingAlreadyApplied = if (hasItemsInColumn) verticalSpacingPx else 0
                    val spacingAdjustment = requiredSpacing - spacingAlreadyApplied
                    val alignmentDelta =
                        currentMaxHeight - columnHeights[columnIndex] + spacingAdjustment
                    val spacerHeight = alignmentDelta + estimatedHeightPx
                    val spacerItem = MasonryColumnItem(
                        key = "masonry-full-$globalIndex-$columnIndex",
                        index = -1,
                        heightPx = spacerHeight,
                        isSpacer = true,
                        placeholderIndex = globalIndex,
                        contentOffsetPx = alignmentDelta
                    )
                    columnItems[columnIndex].add(spacerItem)
                    columnHeights[columnIndex] = fullSpanTop + estimatedHeightPx
                }

                fullSpanItems.add(
                    MasonryFullSpanPlacement(
                        key = key,
                        index = globalIndex,
                        topOffsetPx = fullSpanTop,
                        estimatedHeightPx = estimatedHeightPx
                    )
                )
                itemTopOffsets[globalIndex] = fullSpanTop
            } else {
                val columnIndex = indexOfMin(columnHeights)
                val spacing = if (columnItems[columnIndex].isNotEmpty()) {
                    verticalSpacingPx
                } else {
                    0
                }
                val topOffset = columnHeights[columnIndex] + spacing
                val estimatedHeightPx = estimateHeightPx(
                    estimate = estimate,
                    widthPx = columnWidthPx,
                    density = density
                )
                columnItems[columnIndex].add(
                    MasonryColumnItem(
                        key = key,
                        index = globalIndex,
                        heightPx = estimatedHeightPx,
                        isSpacer = false
                    )
                )
                columnHeights[columnIndex] = topOffset + estimatedHeightPx
                itemTopOffsets[globalIndex] = topOffset
            }
        }

        assignedCount = provider.itemCount
        updateSamples(provider)
        layoutVersion++
    }

    fun itemTopOffset(index: Int): Int? {
        if (index < 0 || index >= assignedCount) return null
        return itemTopOffsets[index]
    }

    private fun ensureCapacity(itemCount: Int) {
        if (itemTopOffsets.size >= itemCount) return
        val newSize = maxOf(itemTopOffsets.size * 2, itemCount)
        itemTopOffsets = itemTopOffsets.copyOf(newSize)
    }

    private fun estimateHeightPx(
        estimate: MasonryItemEstimate?,
        widthPx: Int,
        density: Float
    ): Int {
        return when (estimate) {
            is MasonryItemEstimate.Fixed -> (estimate.height.value * density).roundToInt()
            is MasonryItemEstimate.AspectRatio -> {
                if (estimate.ratio <= 0f) widthPx else (widthPx / estimate.ratio).roundToInt()
            }

            null -> widthPx
        }.coerceAtLeast(1)
    }

    private fun isPrefixStable(provider: MasonryItemProvider): Boolean {
        if (assignedCount == 0) return true
        if (provider.itemCount < assignedCount) return false
        // Sentinel key checks keep append O(1). We accept stale layout on mid-list changes
        // to avoid rebuilding the whole grid during paging updates.
        val first = provider.getKey(0)
        val last = provider.getKey(assignedCount - 1)
        if (first != sampleKeyFirst || last != sampleKeyLast) return false
        if (assignedCount >= 3) {
            val midIndex = assignedCount / 2
            val mid = provider.getKey(midIndex)
            if (mid != sampleKeyMid) return false
        }
        return true
    }

    private fun updateSamples(provider: MasonryItemProvider) {
        if (provider.itemCount == 0) {
            sampleKeyFirst = null
            sampleKeyMid = null
            sampleKeyLast = null
            return
        }
        sampleKeyFirst = provider.getKey(0)
        sampleKeyLast = provider.getKey(provider.itemCount - 1)
        sampleKeyMid = if (provider.itemCount >= 3) {
            provider.getKey(provider.itemCount / 2)
        } else {
            sampleKeyFirst
        }
    }

    private fun indexOfMin(values: IntArray): Int {
        var minIndex = 0
        var minValue = values[0]
        for (i in 1 until values.size) {
            val value = values[i]
            if (value < minValue) {
                minValue = value
                minIndex = i
            }
        }
        return minIndex
    }

}
