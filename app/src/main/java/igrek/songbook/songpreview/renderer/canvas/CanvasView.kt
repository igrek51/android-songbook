package igrek.songbook.songpreview.renderer.canvas

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.graphics.Typeface
import android.os.Build
import android.view.View


class CanvasView(
    context: Context,
    val onInit: () -> Unit,
    val onRepaint: () -> Unit,
    val onSizeChanged: () -> Unit,
) : View(context) {
    constructor(context: Context) : this(context, {}, {}, {})

    var w: Int = 0
    var h: Int = 0

    private var paint: Paint = Paint()
    private var strokePaint: Paint = Paint()
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
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        strokePaint = Paint()
        strokePaint.style = Paint.Style.STROKE
        strokePaint.isAntiAlias = true
        strokePaint.isFilterBitmap = true
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
            else -> paint.textAlign = Paint.Align.RIGHT
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
        canvas?.drawPaint(paint)
    }

    fun fillRect(left: Float, top: Float, right: Float, bottom: Float) {
        canvas?.drawRect(left, top, right, bottom, paint)
    }

    fun borderRect(
        color: Int,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        thickness: Float,
    ) {
        if (color and -0x1000000 == 0) // if alpha channel is not set - set it to max (opaque)
            strokePaint.color = color or -0x1000000
        else
            strokePaint.color = color
        strokePaint.strokeWidth = thickness
        canvas?.drawRect(left, top, right, bottom, strokePaint)
    }

    fun fillRectGradientH(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        color1: Int,
        color2: Int,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val shader: Shader = LinearGradient(left, top, right, top, color1, color2, TileMode.CLAMP)
            val paint = Paint()
            paint.shader = shader
            canvas?.drawRect(RectF(left, top, right, bottom), paint)
        } else {
            setColor(color2)
            fillRect(left, top, right, bottom)
        }
    }
}