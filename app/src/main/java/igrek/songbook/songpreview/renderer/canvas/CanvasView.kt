package igrek.songbook.songpreview.renderer.canvas

import android.content.Context
import android.graphics.*
import android.graphics.Shader.TileMode
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi


class CanvasView(
    context: Context,
    val onInit: () -> Unit,
    val onRepaint: () -> Unit,
    val onSizeChanged: () -> Unit,
) : View(context) {
    constructor(context: Context) : this(context, {}, {}, {})

    var w: Int = 0
    var h: Int = 0

    var paint: Paint = Paint()
    private var canvas: Canvas? = null
    private var initialized: Boolean = false
    private val lock = Any()

    private val isInitialized: Boolean
        @Synchronized get() = initialized

    init {
        viewTreeObserver.addOnGlobalLayoutListener {
            w = width
            h = height
        }
    }

    fun reset() {
        paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        canvas = null
        initialized = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.w = width
        this.h = height
        if (isInitialized)
            onSizeChanged()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.canvas = canvas
        if (w == 0 && h == 0) {
            w = width
            h = height
        }
        if (!initialized) {
            synchronized(lock) {
                if (!initialized) {
                    onInit()
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

    fun drawText(text: String, cx: Float, cy: Float, align: Align) {
        when (align) {
            Align.LEFT -> { // left only
                paint.textAlign = Paint.Align.LEFT
                canvas?.drawText(text, cx, cy, paint)
                return
            }
            Align.RIGHT -> { // right only
                paint.textAlign = Paint.Align.RIGHT
                canvas?.drawText(text, cx, cy, paint)
                return
            }
            else -> {}
        }
        when {
            align.isFlagSet(Align.LEFT) -> paint.textAlign = Paint.Align.LEFT
            align.isFlagSet(Align.HCENTER) -> paint.textAlign = Paint.Align.CENTER
            else -> // right
                paint.textAlign = Paint.Align.RIGHT
        }
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        var yPos = cy - (paint.descent() + paint.ascent()) / 2
        if (align.isFlagSet(Align.TOP)) {
            yPos += (textBounds.height() / 2).toFloat()
        } else if (align.isFlagSet(Align.BOTTOM)) {
            yPos -= (textBounds.height() / 2).toFloat()
        }
        canvas?.drawText(text, cx, yPos, paint)
    }

    fun setFontSize(textsize: Float) {
        paint.textSize = textsize
    }

    fun setFontTypeface(typeface: Typeface?) {
        paint.typeface = typeface
    }

    fun setColor(color: Int) {
        var color1 = color
        // if alpha channel is not set - set it to max (opaque)
        if (color1 and -0x1000000 == 0)
            color1 = color1 or -0x1000000
        paint.color = color1
    }

    fun setColor(rgb: Int, alpha: Int) {
        paint.color = rgb or (alpha shl 24)
    }

    fun clearScreen() {
        paint.style = Paint.Style.FILL
        canvas?.drawPaint(paint)
    }

    fun fillRect(left: Float, top: Float, right: Float, bottom: Float) {
        paint.style = Paint.Style.FILL
        canvas?.drawRect(left, top, right, bottom, paint)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun fillRectGradientH(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        color1: Int,
        color2: Int
    ) {
        val shader: Shader = LinearGradient(left, top, right, top, color1, color2, TileMode.CLAMP)
        val paint = Paint()
        paint.shader = shader
        canvas?.drawRect(RectF(left, top, right, bottom), paint)
    }
}