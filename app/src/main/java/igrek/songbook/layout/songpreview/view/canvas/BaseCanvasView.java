package igrek.songbook.layout.songpreview.view.canvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

public abstract class BaseCanvasView extends View {
	
	protected int w = 0;
	protected int h = 0;
	
	protected Paint paint;
	protected Canvas canvas = null;
	private boolean initialized = false;
	
	public BaseCanvasView(Context context) {
		super(context);
		
		getViewTreeObserver().addOnGlobalLayoutListener(() -> {
			w = getWidth();
			h = getHeight();
		});
	}
	
	public static boolean isFlagSet(int tested, int flag) {
		return (tested & flag) == flag;
	}
	
	public void reset() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		//paint.setDither(true);
		
		canvas = null;
		initialized = false;
	}
	
	public int getW() {
		return w;
	}
	
	public int getH() {
		return h;
	}
	
	/**
	 * repaint screen method to override
	 */
	public void onRepaint() {
	}
	
	public void init() {
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.w = getWidth();
		this.h = getHeight();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		this.canvas = canvas;
		if (w == 0 && h == 0) {
			w = getWidth();
			h = getHeight();
		}
		if (!initialized) {
			initialized = true;
			init();
		}
		onRepaint();
	}
	
	public synchronized void repaint() {
		invalidate();
	}
	
	
	public void drawTextUnaligned(String s, float x, float y) {
		paint.setTextAlign(Paint.Align.LEFT);
		canvas.drawText(s, x, y, paint);
	}
	
	public float[] getTextWidths(String s) {
		float[] f = new float[s.length()];
		paint.getTextWidths(s, f);
		return f;
	}
	
	public void drawText(String text, float cx, float cy, int align) {
		// short case
		if (align == Align.LEFT) { // left only
			paint.setTextAlign(Paint.Align.LEFT);
			canvas.drawText(text, cx, cy, paint);
			return;
		} else if (align == Align.RIGHT) { // right only
			paint.setTextAlign(Paint.Align.RIGHT);
			canvas.drawText(text, cx, cy, paint);
			return;
		}
		// default values
		if ((align & 0x0f) == 0)
			align |= Align.LEFT;
		if ((align & 0xf0) == 0)
			align |= Align.TOP;
		if (isFlagSet(align, Align.LEFT)) {
			paint.setTextAlign(Paint.Align.LEFT);
		} else if (isFlagSet(align, Align.HCENTER)) {
			paint.setTextAlign(Paint.Align.CENTER);
		} else { // right
			paint.setTextAlign(Paint.Align.RIGHT);
		}
		Rect textBounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), textBounds);
		float y_pos = cy - (paint.descent() + paint.ascent()) / 2;
		if (isFlagSet(align, Align.TOP)) {
			y_pos += textBounds.height() / 2;
		} else if (isFlagSet(align, Align.BOTTOM)) {
			y_pos -= textBounds.height() / 2;
		}
		canvas.drawText(text, cx, y_pos, paint);
	}
	
	public void drawText(String text, float cx, float cy) {
		drawText(text, cx, cy, 0);
	}
	
	public void drawTextMultiline(String text, float cx, float cy, float lineheight, int align) {
		// calculate lines count
		int lines = 1;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\n')
				lines++;
		}
		// default values
		if ((align & 0x0f) == 0)
			align |= Align.LEFT;
		if ((align & 0xf0) == 0)
			align |= Align.TOP;
		// offest in Y axis
		float offset_y;
		if (isFlagSet(align, Align.TOP)) {
			offset_y = cy;
		} else if (isFlagSet(align, Align.VCENTER)) {
			offset_y = cy - (lines - 1) * lineheight / 2;
		} else { // bottom
			offset_y = cy - (lines - 1) * lineheight;
		}
		// for each line
		for (int i = 0; i < lines; i++) {
			// find \n character
			int indexn = text.indexOf("\n");
			if (indexn == -1)
				indexn = text.length(); // there is no \n anymore
			// cut a row
			String row_text = text.substring(0, indexn);
			if (indexn < text.length()) {
				text = text.substring(indexn + 1); // remove cut row and \n character
			}
			// draw 1 row
			drawText(row_text, cx, offset_y, align);
			offset_y += lineheight;
		}
	}
	
	public void drawTextMultiline(String text, float cx, float cy, float lineheight) {
		drawTextMultiline(text, cx, cy, lineheight, 0);
	}
	
	public float getTextWidth(String text) {
		float[] widths = getTextWidths(text);
		float sum = 0;
		for (float w : widths) {
			sum += w;
		}
		return sum;
	}
	
	public void setFontSize(float textsize) {
		paint.setTextSize(textsize);
	}
	
	public void setFont(int fontface) {
		// default values
		if ((fontface & 0x0f) == 0)
			fontface |= Font.FONT_DEFAULT;
		if ((fontface & 0xf0) == 0)
			fontface |= Font.FONT_NORMAL;
		Typeface family;
		if (isFlagSet(fontface, Font.FONT_MONOSPACE)) {
			family = Typeface.MONOSPACE;
		} else {
			family = Typeface.DEFAULT;
		}
		int style;
		if (isFlagSet(fontface, Font.FONT_BOLD)) {
			style = Typeface.BOLD;
		} else {
			style = Typeface.NORMAL;
		}
		paint.setTypeface(Typeface.create(family, style));
	}
	
	public void setFont() {
		setFont(Font.FONT_DEFAULT | Font.FONT_NORMAL); // reset font to default
	}
	
	public void setColor(String color) {
		if (color.length() > 0 && color.charAt(0) != '#') {
			color = "#" + color;
		}
		paint.setColor(Color.parseColor(color));
	}
	
	public void setColor(int color) {
		// if alpha channel is not set - set it to max (opaque)
		if ((color & 0xff000000) == 0)
			color |= 0xff000000;
		paint.setColor(color);
	}
	
	public void setColor(int rgb, int alpha) {
		paint.setColor(rgb | (alpha << 24));
	}
	
	public void clearScreen() {
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPaint(paint);
	}
	
	public void clearScreen(String color) {
		setColor(color);
		clearScreen();
	}
	
	public void drawLine(float startx, float starty, float stopx, float stopy) {
		canvas.drawLine(startx, starty, stopx, stopy, paint);
	}
	
	public void fillCircle(float cx, float cy, float radius) {
		paint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(cx, cy, radius, paint);
	}
	
	public void outlineCircle(float cx, float cy, float radius, float thickness) {
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(thickness);
		canvas.drawCircle(cx, cy, radius, paint);
		paint.setStrokeWidth(0);
	}
	
	public void fillRect(float left, float top, float right, float bottom) {
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(left, top, right, bottom, paint);
	}
	
	public void fillRectWH(float left, float top, float width, float height) {
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(left, top, left + width, top + height, paint);
	}
	
	public void fillRoundRect(float left, float top, float right, float bottom, float radius) {
		paint.setStyle(Paint.Style.FILL);
		RectF rectf = new RectF(left, top, right, bottom);
		canvas.drawRoundRect(rectf, radius, radius, paint);
	}
	
	public void fillRoundRectWH(float left, float top, float width, float height, float radius) {
		fillRoundRect(left, top, left + width, top + height, radius);
	}
	
	public void outlineRectWH(float left, float top, float width, float height, float thickness) {
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(thickness);
		canvas.drawRect(left, top, left + width, top + height, paint);
		paint.setStrokeWidth(0);
	}
}