package igrek.songbook.graphics.gui;

import android.graphics.Paint;

import igrek.songbook.logic.filetree.FileItem;

public interface GUIListener {

    void onToolbarBackClicked();

    void onItemClicked(int position, FileItem item);

    void onResized(int w, int h);

    void onGraphicsInitialized(int w, int h, Paint paint);

    void onTransposed(int t);

    void onFontsizeChanged(float fontsize);

    void autoscrollRemainingWaitTime(long ms);

    void onAutoscrollStartRequest();

    void onAutoscrollStarted();

    void onAutoscrollEnded();

    void onCanvasClicked();

    boolean auscrollScrollBy(float intervalStep);

    boolean canAutoScroll();

    void onCanvasScroll(float dScroll);
}
