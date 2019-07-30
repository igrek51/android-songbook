package igrek.songbook.songpreview.renderer.canvas

import android.content.Context
import android.graphics.*
import android.view.View

abstract class BaseCanvasView(context: Context) : View(context) {

    var w = 0
        protected set
    var h = 0
        protected set

    var paint: Paint? = null
        protected set
    protected var canvas: Canvas? = null
    private var initialized: Boolean = false

    val isInitialized: Boolean
        @Synchronized get() = initialized

    init {

        viewTreeObserver.addOnGlobalLayoutListener {
            w = width
            h = height
        }
    }

    open fun reset() {
        paint = Paint()
        paint?.isAntiAlias = true
        paint?.isFilterBitmap = true
        //paint.setDither(true);

        canvas = null
        initialized = false
    }

    abstract fun onRepaint()

    abstract fun init()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.w = width
        this.h = height
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.canvas = canvas
        if (w == 0 && h == 0) {
            w = width
            h = height
        }
        if (!initialized) {
            synchronized(initialized) {
                if (!initialized) {
                    init()
                    initialized = true
                }
            }
        }
        onRepaint()
    }

    @Synchronized
    fun repaint() {
        invalidate()
    }

    fun drawTextUnaligned(s: String, x: Float, y: Float) {
        paint?.textAlign = Paint.Align.LEFT
        canvas!!.drawText(s, x, y, paint)
    }

    fun getTextWidths(s: String): FloatArray {
        val f = FloatArray(s.length)
        paint?.getTextWidths(s, f)
        return f
    }

    @JvmOverloads
    fun drawText(text: String, cx: Float, cy: Float, align: Int = 0) {
        var align = align
        // short case
        if (align == Align.LEFT) { // left only
            paint?.textAlign = Paint.Align.LEFT
            canvas!!.drawText(text, cx, cy, paint)
            return
        } else if (align == Align.RIGHT) { // right only
            paint?.textAlign = Paint.Align.RIGHT
            canvas!!.drawText(text, cx, cy, paint)
            return
        }
        // default values
        if (align and 0x0f == 0)
            align = align or Align.LEFT
        if (align and 0xf0 == 0)
            align = align or Align.TOP
        if (isFlagSet(align, Align.LEFT)) {
            paint?.textAlign = Paint.Align.LEFT
        } else if (isFlagSet(align, Align.HCENTER)) {
            paint?.textAlign = Paint.Align.CENTER
        } else { // right
            paint?.textAlign = Paint.Align.RIGHT
        }
        val textBounds = Rect()
        paint?.getTextBounds(text, 0, text.length, textBounds)
        var y_pos = cy - (paint!!.descent() + paint!!.ascent()) / 2
        if (isFlagSet(align, Align.TOP)) {
            y_pos += (textBounds.height() / 2).toFloat()
        } else if (isFlagSet(align, Align.BOTTOM)) {
            y_pos -= (textBounds.height() / 2).toFloat()
        }
        canvas!!.drawText(text, cx, y_pos, paint)
    }

    @JvmOverloads
    fun drawTextMultiline(text: String, cx: Float, cy: Float, lineheight: Float, align: Int = 0) {
        var text = text
        var align = align
        // calculate lines count
        var lines = 1
        for (i in 0 until text.length) {
            if (text[i] == '\n')
                lines++
        }
        // default values
        if (align and 0x0f == 0)
            align = align or Align.LEFT
        if (align and 0xf0 == 0)
            align = align or Align.TOP
        // offest in Y axis
        var offset_y: Float
        if (isFlagSet(align, Align.TOP)) {
            offset_y = cy
        } else if (isFlagSet(align, Align.VCENTER)) {
            offset_y = cy - (lines - 1) * lineheight / 2
        } else { // bottom
            offset_y = cy - (lines - 1) * lineheight
        }
        // for each line
        for (i in 0 until lines) {
            // find \n character
            var indexn = text.indexOf("\n")
            if (indexn == -1)
                indexn = text.length // there is no \n anymore
            // cut a row
            val row_text = text.substring(0, indexn)
            if (indexn < text.length) {
                text = text.substring(indexn + 1) // remove cut row and \n character
            }
            // draw 1 row
            drawText(row_text, cx, offset_y, align)
            offset_y += lineheight
        }
    }

    fun getTextWidth(text: String): Float {
        val widths = getTextWidths(text)
        var sum = 0f
        for (w in widths) {
            sum += w
        }
        return sum
    }

    fun setFontSize(textsize: Float) {
        paint?.textSize = textsize
    }

    fun setFontTypeface(typeface: Typeface?) {
        paint?.typeface = typeface
    }

    fun setColor(color: String) {
        var color = color
        if (color.length > 0 && color[0] != '#') {
            color = "#$color"
        }
        paint?.color = Color.parseColor(color)
    }

    fun setColor(color: Int) {
        var color = color
        // if alpha channel is not set - set it to max (opaque)
        if (color and -0x1000000 == 0)
            color = color or -0x1000000
        paint?.color = color
    }

    fun setColor(rgb: Int, alpha: Int) {
        paint?.color = rgb or (alpha shl 24)
    }

    fun clearScreen() {
        paint?.style = Paint.Style.FILL
        canvas!!.drawPaint(paint)
    }

    fun clearScreen(color: String) {
        setColor(color)
        clearScreen()
    }

    fun drawLine(startx: Float, starty: Float, stopx: Float, stopy: Float) {
        canvas!!.drawLine(startx, starty, stopx, stopy, paint)
    }

    fun fillCircle(cx: Float, cy: Float, radius: Float) {
        paint?.style = Paint.Style.FILL
        canvas!!.drawCircle(cx, cy, radius, paint)
    }

    fun outlineCircle(cx: Float, cy: Float, radius: Float, thickness: Float) {
        paint?.style = Paint.Style.STROKE
        paint?.strokeWidth = thickness
        canvas!!.drawCircle(cx, cy, radius, paint)
        paint?.strokeWidth = 0f
    }

    fun fillRect(left: Float, top: Float, right: Float, bottom: Float) {
        paint?.style = Paint.Style.FILL
        canvas!!.drawRect(left, top, right, bottom, paint)
    }

    fun fillRectWH(left: Float, top: Float, width: Float, height: Float) {
        paint?.style = Paint.Style.FILL
        canvas!!.drawRect(left, top, left + width, top + height, paint)
    }

    fun fillRoundRect(left: Float, top: Float, right: Float, bottom: Float, radius: Float) {
        paint?.style = Paint.Style.FILL
        val rectf = RectF(left, top, right, bottom)
        canvas!!.drawRoundRect(rectf, radius, radius, paint)
    }

    fun fillRoundRectWH(left: Float, top: Float, width: Float, height: Float, radius: Float) {
        fillRoundRect(left, top, left + width, top + height, radius)
    }

    fun outlineRectWH(left: Float, top: Float, width: Float, height: Float, thickness: Float) {
        paint?.style = Paint.Style.STROKE
        paint?.strokeWidth = thickness
        canvas!!.drawRect(left, top, left + width, top + height, paint)
        paint?.strokeWidth = 0f
    }

    companion object {

        fun isFlagSet(tested: Int, flag: Int): Boolean {
            return tested and flag == flag
        }
    }
}