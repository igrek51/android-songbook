package igrek.todotree.gui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import igrek.todotree.logic.touchcontroller.ITouchController;

public class GUIBase implements View.OnTouchListener {

    protected AppCompatActivity activity;
    protected GUIListener guiListener;
    protected ITouchController touchController = null;

    protected InputMethodManager imm;

    protected RelativeLayout mainContent;

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

    public RelativeLayout getMainContent() {
        return mainContent;
    }

    public View setMainContentLayout(int layoutResource) {
        mainContent.removeAllViews();
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(layoutResource, null);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainContent.addView(layout);
        return layout;
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

}
