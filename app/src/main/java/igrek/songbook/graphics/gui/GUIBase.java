package igrek.songbook.graphics.gui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import igrek.songbook.logic.touchcontroller.ITouchController;

public class GUIBase implements View.OnTouchListener {

    protected AppCompatActivity activity;
    protected GUIListener guiListener;
    protected ITouchController touchController = null;

    protected InputMethodManager imm;

    public GUIBase(AppCompatActivity activity, GUIListener guiListener) {
        this.activity = activity;
        this.guiListener = guiListener;
        imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        init();
    }

    public void setTouchController(ITouchController touchController) {
        this.touchController = touchController;
    }

    public GUIListener getGuiListener() {
        return guiListener;
    }

    protected void init() {

    }


    public void hideSoftKeyboard(View window) {
        if (imm != null) {
            imm.hideSoftInputFromWindow(window.getWindowToken(), 0);
        }
    }

    public void showSoftKeyboard(View window) {
        if (imm != null) {
            imm.showSoftInput(window, 0);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(touchController != null) {
            float touchX = event.getX();
            float touchY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return touchController.onTouchDown(touchX, touchY);
                case MotionEvent.ACTION_MOVE:
                    return touchController.onTouchMove(touchX, touchY);
                case MotionEvent.ACTION_UP:
                    return touchController.onTouchUp(touchX, touchY);
            }
        }
        return false;
    }

    protected void setFullscreen(boolean full) {
        int fullscreen_flag = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        if (full) {
            activity.getWindow().setFlags(fullscreen_flag, fullscreen_flag);
        } else {
            activity.getWindow().clearFlags(fullscreen_flag);
        }
    }
}
