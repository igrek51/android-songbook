package igrek.songbook.playlist.list

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.view.View

import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import igrek.songbook.info.logger.LoggerFactory

class TreeListReorder(private val listView: PlaylistListView) {

    private val logger = LoggerFactory.logger

    private var startTouchY: Float = 0.toFloat()
    private var lastTouchY: Float = 0.toFloat()
    private var scrollStart: Int? = 0

    private var draggedItemPos: Int? = null
    private var draggedItemView: View? = null
    private var draggedItemViewTop: Int? = null

    private var hoverBitmap: BitmapDrawable? = null
    private var hoverBitmapAnimation: BitmapDrawable? = null
    var hoverBitmapBounds: Rect? = null
        private set

    private val itemsReplaceCover = 0.65f

    private val hoverBorderThickness = 5
    private val hoverBorderColor = -0x334f4f50

    val isDragging: Boolean
        get() = draggedItemPos != null

    private fun updateHoverBitmap() {
        if (draggedItemViewTop != null && draggedItemPos != null) {
            val dy = lastTouchY - startTouchY
            hoverBitmapBounds!!.offsetTo(0, draggedItemViewTop!! + dy.toInt())
            hoverBitmap!!.bounds = hoverBitmapBounds!!
        }
    }

    private fun getAndAddHoverView(v: View): BitmapDrawable {
        val top = v.top
        val left = v.left

        val b = getBitmapWithBorder(v)
        val drawable = BitmapDrawable(listView.resources, b)

        hoverBitmapBounds = Rect(left, top, left + v.width, top + v.height)
        drawable.bounds = hoverBitmapBounds!!

        return drawable
    }

    private fun getBitmapWithBorder(v: View): Bitmap {
        val bitmap = getBitmapFromView(v)
        val can = Canvas(bitmap)

        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = hoverBorderThickness.toFloat()
        paint.color = hoverBorderColor

        can.drawBitmap(bitmap, 0f, 0f, null)
        can.drawRect(rect, paint)

        return bitmap
    }

    private fun getBitmapFromView(v: View): Bitmap {
        val bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        v.draw(canvas)
        return bitmap
    }


    private fun itemDraggingStarted(position: Int, itemView: View) {
        draggedItemPos = position
        draggedItemView = itemView
        draggedItemViewTop = itemView.top
        scrollStart = listView.scrollHandler?.scrollOffset

        hoverBitmap = getAndAddHoverView(draggedItemView!!)
        draggedItemView!!.visibility = INVISIBLE

        listView.invalidate()
    }

    fun setLastTouchY(lastTouchY: Float) {
        this.lastTouchY = lastTouchY
    }

    fun handleItemDragging() {

        val dyTotal = lastTouchY - startTouchY + ((listView.scrollHandler?.scrollOffset ?: 0) - (scrollStart ?: 0))

        if (draggedItemViewTop == null) {
            logger.error("draggedItemViewTop = null")
            return
        }
        if (draggedItemPos == null) {
            logger.error("draggedItemPos = null")
            return
        }

        var step = 0
        var deltaH = 0

        if (dyTotal > 0) { // move up
            while (true) {
                if (draggedItemPos!! + step + 1 >= listView.items?.size ?: 0)
                    break
                val downHeight = listView.getItemHeight(draggedItemPos!! + step + 1)
                if (downHeight == 0)
                    break
                if (dyTotal - deltaH > downHeight * itemsReplaceCover) {
                    step++
                    deltaH += downHeight
                } else {
                    break
                }
            }
        } else if (dyTotal < 0) { // move down
            while (true) {
                if (draggedItemPos!! + step - 1 < 0)
                    break
                val upHeight = listView.getItemHeight(draggedItemPos!! + step - 1)
                if (upHeight == 0)
                    break
                if (-dyTotal + deltaH > upHeight * itemsReplaceCover) {
                    step--
                    deltaH -= upHeight
                } else {
                    break
                }
            }
        }

        if (step != 0) {
            val targetPosition = draggedItemPos!! + step

            listView.itemMoved(draggedItemPos!!, step)

            if (step > 0) {
                val draggedItemHeight = listView.getItemHeight(draggedItemPos!!)
                for (i in draggedItemPos!! until targetPosition) {
                    val nextHeight = listView.getItemHeight(i + 1)
                    listView.putItemHeight(i, nextHeight)
                }
                listView.putItemHeight(targetPosition, draggedItemHeight)
            } else if (step < 0) {
                val draggedItemHeight = listView.getItemHeight(draggedItemPos!!)
                for (i in draggedItemPos!! downTo targetPosition + 1) {
                    val nextHeight = listView.getItemHeight(i - 1)
                    listView.putItemHeight(i, nextHeight)
                }
                listView.putItemHeight(targetPosition, draggedItemHeight)
            }

            startTouchY += deltaH.toFloat()
            draggedItemViewTop = draggedItemViewTop!! + deltaH

            draggedItemPos = targetPosition
            if (draggedItemView != null) {
                draggedItemView!!.visibility = VISIBLE
            }
            draggedItemView = listView.getItemView(draggedItemPos!!)
            if (draggedItemView != null) {
                draggedItemView!!.visibility = INVISIBLE
            }
        }

        listView.scrollHandler?.handleScrolling()

        listView.invalidate()
    }

    fun itemDraggingStopped() {
        if (draggedItemPos != null && draggedItemViewTop != null) {
            draggedItemPos = null

            hoverBitmapAnimation = hoverBitmap
            val scrollOffset = listView.scrollHandler?.scrollOffset ?: 0
            hoverBitmapBounds!!.offsetTo(0, draggedItemViewTop!! - (scrollOffset - scrollStart!!))
            val hoverBitmapBoundsCopy = Rect(hoverBitmapBounds)
            val draggedItemViewCopy = draggedItemView

            draggedItemView = null
            hoverBitmap = null
            draggedItemViewTop = null

            val hoverViewAnimator = ObjectAnimator.ofObject(hoverBitmapAnimation, "bounds", rectBoundsEvaluator, hoverBitmapBoundsCopy)
            hoverViewAnimator.addUpdateListener { listView.invalidate() }
            hoverViewAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    listView.isEnabled = false
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (draggedItemViewCopy != null) {
                        draggedItemViewCopy.visibility = VISIBLE
                    }
                    hoverBitmapAnimation = null
                    listView.isEnabled = true
                    listView.invalidate()
                }
            })
            hoverViewAnimator.start()
        } else {
            draggedItemPos = null
            draggedItemView = null
            draggedItemViewTop = null
        }
    }

    fun onItemMoveButtonPressed(position: Int, itemView: View, touchY: Float) {
        startTouchY = itemView.top + touchY
        lastTouchY = startTouchY
        itemDraggingStarted(position, itemView)
    }

    fun onItemMoveClicked(position: Int) {
        if (position + 1 >= (listView.items?.size ?: 0))
            return
        listView.itemMoved(position, 1)
        listView.invalidate()
    }

    fun onItemMoveButtonReleased() {
        itemDraggingStopped()
    }

    fun setDraggedItemView() {
        val draggedItemViewOld = draggedItemView
        draggedItemView = listView.getItemView(draggedItemPos!!)
        if (draggedItemView != null) {
            draggedItemView!!.visibility = INVISIBLE
        }
        if (draggedItemViewOld !== draggedItemView && draggedItemViewOld != null) {
            draggedItemViewOld.visibility = VISIBLE
        }
    }

    fun dispatchDraw(canvas: Canvas) {
        if (hoverBitmap != null) {
            updateHoverBitmap()
            hoverBitmap!!.draw(canvas)
        }
        if (hoverBitmapAnimation != null) {
            hoverBitmapAnimation!!.draw(canvas)
        }
    }

    companion object {

        private val rectBoundsEvaluator = object : TypeEvaluator<Rect> {
            override fun evaluate(fraction: Float, startValue: Rect, endValue: Rect): Rect {
                return Rect(interpolate(startValue.left, endValue.left, fraction), interpolate(startValue.top, endValue.top, fraction), interpolate(startValue.right, endValue.right, fraction), interpolate(startValue.bottom, endValue.bottom, fraction))
            }

            fun interpolate(start: Int, end: Int, fraction: Float): Int {
                return (start + fraction * (end - start)).toInt()
            }
        }
    }
}
