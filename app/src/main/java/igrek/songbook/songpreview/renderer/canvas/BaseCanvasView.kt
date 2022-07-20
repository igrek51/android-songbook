package igrek.songbook.songpreview.renderer.canvas

import android.content.Context
import android.graphics.*
import android.graphics.LinearGradient
import android.graphics.Shader.TileMode
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi


abstract class BaseCanvasView(context: Context) : View(context) {

    var w: Int = 0
        protected set
    var h: Int = 0
        protected set

    var paint: Paint? = null
        protected set
    private var canvas: Canvas? = null
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

    fun drawText(text: String, cx: Float, cy: Float, align: Int) {
        when (align) {
            Align.LEFT -> { // left only
                paint?.textAlign = Paint.Align.LEFT
                canvas?.drawText(text, cx, cy, paint!!)
                return
            }
            Align.RIGHT -> { // right only
                paint?.textAlign = Paint.Align.RIGHT
                canvas?.drawText(text, cx, cy, paint!!)
                return
            }
        }
        when {
            isFlagSet(align, Align.LEFT) -> paint?.textAlign = Paint.Align.LEFT
            isFlagSet(align, Align.HCENTER) -> paint?.textAlign = Paint.Align.CENTER
            else -> // right
                paint?.textAlign = Paint.Align.RIGHT
        }
        val textBounds = Rect()
        paint?.getTextBounds(text, 0, text.length, textBounds)
        var yPos = cy - (paint!!.descent() + paint!!.ascent()) / 2
        if (isFlagSet(align, Align.TOP)) {
            yPos += (textBounds.height() / 2).toFloat()
        } else if (isFlagSet(align, Align.BOTTOM)) {
            yPos -= (textBounds.height() / 2).toFloat()
        }
        canvas?.drawText(text, cx, yPos, paint!!)
    }

    fun setFontSize(textsize: Float) {
        paint?.textSize = textsize
    }

    fun setFontTypeface(typeface: Typeface?) {
        paint?.typeface = typeface
    }

    fun setColor(color: Int) {
        var color1 = color
        // if alpha channel is not set - set it to max (opaque)
        if (color1 and -0x1000000 == 0)
            color1 = color1 or -0x1000000
        paint?.color = color1
    }

    fun setColor(color: Long) {
        var color1: Long = color
        // if alpha channel is not set - set it to max (opaque)
        if (color1 and -0x1000000 == 0L)
            color1 = color1 or -0x1000000
        paint?.color = color1.toInt()
    }

    fun setColor(rgb: Int, alpha: Int) {
        paint?.color = rgb or (alpha shl 24)
    }

    fun clearScreen() {
        paint?.style = Paint.Style.FILL
        canvas?.drawPaint(paint!!)
    }

    fun fillRect(left: Float, top: Float, right: Float, bottom: Float) {
        paint?.style = Paint.Style.FILL
        canvas?.drawRect(left, top, right, bottom, paint!!)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun fillRectGradientH(left: Float, top: Float, right: Float, bottom: Float, color1: Int, color2: Int) {
        val shader: Shader = LinearGradient(left, top, right, top, color1, color2, TileMode.CLAMP)
        val paint = Paint()
        paint.shader = shader
        canvas?.drawRect(RectF(left, top, right, bottom), paint)
    }

    companion object {
        fun isFlagSet(tested: Int, flag: Int): Boolean {
            return tested and flag == flag
        }
    }
}