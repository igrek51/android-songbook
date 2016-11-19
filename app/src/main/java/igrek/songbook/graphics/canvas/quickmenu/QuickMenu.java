package igrek.songbook.graphics.canvas.quickmenu;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import igrek.songbook.R;
import igrek.songbook.events.ShowQuickMenuEvent;
import igrek.songbook.events.autoscroll.AutoscrollStartUIEvent;
import igrek.songbook.events.autoscroll.AutoscrollStopUIEvent;
import igrek.songbook.events.transpose.TransposeEvent;
import igrek.songbook.events.transpose.TransposeResetEvent;
import igrek.songbook.events.transpose.TransposedEvent;
import igrek.songbook.graphics.canvas.CanvasGraphics;
import igrek.songbook.logic.autoscroll.Autoscroll;
import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.controller.dispatcher.IEvent;
import igrek.songbook.logic.controller.dispatcher.IEventObserver;
import igrek.songbook.logic.controller.services.IService;
import igrek.songbook.logic.crdfile.ChordsManager;
import igrek.songbook.resources.UserInfoService;

//TODO jedna instacja, ponowne wykorzystanie klasy, bez czyszczenia event observerów
//TODO paski regulacji czasu odliczania i tempa autoscrolla
//TODO 2 buttony: autoscroll wait, autoscroll now

public class QuickMenu implements IService, IEventObserver {

    private CanvasGraphics canvas;
    private ChordsManager chordsManager;
    private UserInfoService infoService;

    private boolean visible = false;

    private View quickMenuView;

    private TextView tvTransposition;
    private Button btnTransposeM5;
    private Button btnTransposeM1;
    private Button btnTranspose0;
    private Button btnTransposeP1;
    private Button btnTransposeP5;
    private Button btnAutoscrollToggle;

    public QuickMenu(CanvasGraphics canvas) {
        this.canvas = canvas;

        chordsManager = AppController.getService(ChordsManager.class);
        infoService = AppController.getService(UserInfoService.class);

        AppController.registerService(this);

        AppController.clearEventObservers(ShowQuickMenuEvent.class);
        AppController.registerEventObserver(ShowQuickMenuEvent.class, this);
        AppController.clearEventObservers(TransposedEvent.class);
        AppController.registerEventObserver(TransposedEvent.class, this);
    }

    public void setQuickMenuView(View quickMenuView) {
        this.quickMenuView = quickMenuView;

        tvTransposition = (TextView) quickMenuView.findViewById(R.id.tvTransposition);

        btnTransposeM5 = (Button) quickMenuView.findViewById(R.id.btnTransposeM5);
        btnTransposeM5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppController.sendEvent(new TransposeEvent(-5));
            }
        });

        btnTransposeM1 = (Button) quickMenuView.findViewById(R.id.btnTransposeM1);
        btnTransposeM1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppController.sendEvent(new TransposeEvent(-1));
            }
        });

        btnTranspose0 = (Button) quickMenuView.findViewById(R.id.btnTranspose0);
        btnTranspose0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppController.sendEvent(new TransposeResetEvent());
            }
        });

        btnTransposeP1 = (Button) quickMenuView.findViewById(R.id.btnTransposeP1);
        btnTransposeP1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppController.sendEvent(new TransposeEvent(+1));
            }
        });

        btnTransposeP5 = (Button) quickMenuView.findViewById(R.id.btnTransposeP5);
        btnTransposeP5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppController.sendEvent(new TransposeEvent(+5));
            }
        });

        btnAutoscrollToggle = (Button) quickMenuView.findViewById(R.id.btnAutoscrollToggle);
        btnAutoscrollToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Autoscroll autoscroll = AppController.getService(Autoscroll.class);
                if (autoscroll.isRunning()) {
                    AppController.sendEvent(new AutoscrollStopUIEvent());
                } else {
                    AppController.sendEvent(new AutoscrollStartUIEvent());
                }

                setVisible(false);
            }
        });

        updateTranspositionText();
    }

    private void updateTranspositionText() {
        String tvTranspositionText = infoService.resString(R.string.transposition) + ": " + chordsManager.getTransposedString();

        tvTransposition.setText(tvTranspositionText);
    }

    public void draw() {

        if (visible) {

            //dimmed background
            float w = canvas.getW();
            float h = canvas.getH();

            canvas.setColor(0x000000, 130);
            canvas.fillRect(0, 0, w, h);

        }
    }

    public boolean onScreenClicked(float x, float y) {

        setVisible(false);

        return true;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;

        if (visible) {
            quickMenuView.setVisibility(View.VISIBLE);
            updateTranspositionText();
        } else {
            quickMenuView.setVisibility(View.GONE);
        }

        canvas.repaint();
    }

    @Override
    public void onEvent(IEvent event) {
        if (event instanceof ShowQuickMenuEvent) {
            setVisible(((ShowQuickMenuEvent) event).isShow());
        } else if (event instanceof TransposedEvent) {
            if (visible) {
                updateTranspositionText();
            }
        }
    }
}
