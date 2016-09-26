package igrek.songbook.graphics.canvas.quickmenu;

import java.util.ArrayList;
import java.util.List;

import igrek.songbook.R;
import igrek.songbook.events.ShowQuickMenuEvent;
import igrek.songbook.events.autoscroll.AutoscrollStartUIEvent;
import igrek.songbook.events.autoscroll.AutoscrollStopUIEvent;
import igrek.songbook.events.transpose.TransposeEvent;
import igrek.songbook.events.transpose.TransposeResetEvent;
import igrek.songbook.graphics.canvas.CanvasGraphics;
import igrek.songbook.graphics.canvas.enums.Align;
import igrek.songbook.graphics.canvas.enums.Font;
import igrek.songbook.logic.autoscroll.Autoscroll;
import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.controller.dispatcher.IEvent;
import igrek.songbook.logic.controller.dispatcher.IEventObserver;
import igrek.songbook.logic.controller.services.IService;
import igrek.songbook.logic.crdfile.ChordsManager;
import igrek.songbook.resources.LangStringService;

//TODO jedna instacja, ponowne wykorzystanie klasy

public class QuickMenu implements IService, IEventObserver {

    private CanvasGraphics canvas;
    private ChordsManager chordsManager;
    private LangStringService langStrings;

    private boolean visible = false;

    private final float MENU_TRANSPOSE_BUTTON_H = 0.2f;
    private final float MENU_AUTOSCROLL_BUTTON_H = 0.2f;

    private List<QuickMenuButton> buttons;

    public QuickMenu(CanvasGraphics canvas) {
        this.canvas = canvas;

        chordsManager = AppController.getService(ChordsManager.class);
        langStrings = AppController.getService(LangStringService.class);

        initButtons();

        AppController.registerService(this);

        AppController.clearEventObservers(ShowQuickMenuEvent.class);
        AppController.registerEventObserver(ShowQuickMenuEvent.class, this);
    }

    private void initButtons() {
        buttons = new ArrayList<>();

        //TODO button extends QuickMenuButton z nadpisaną metodą getText(), dynamiczny tekst na podstawie stanu autoscrolla
        //TODO 2 buttony: autoscroll wait, autoscroll now
        //TODO autoscroll text to string xml
        buttons.add(new QuickMenuButton(langStrings.resString(R.string.autoscroll), 0, 1 - MENU_AUTOSCROLL_BUTTON_H, 1, MENU_AUTOSCROLL_BUTTON_H, new ButtonClickedAction() {
            @Override
            public void onClicked() {

                Autoscroll autoscroll = AppController.getService(Autoscroll.class);
                if (autoscroll.isRunning()) {
                    AppController.sendEvent(new AutoscrollStopUIEvent());
                } else {
                    AppController.sendEvent(new AutoscrollStartUIEvent());
                }

                setVisible(false);
            }
        }));

        buttons.add(new QuickMenuButton("-5", 0, 1 - MENU_AUTOSCROLL_BUTTON_H - MENU_TRANSPOSE_BUTTON_H, 0.2f, MENU_TRANSPOSE_BUTTON_H, new ButtonClickedAction() {
            @Override
            public void onClicked() {
                AppController.sendEvent(new TransposeEvent(-5));
            }
        }));
        buttons.add(new QuickMenuButton("-1", 0.2f, 1 - MENU_AUTOSCROLL_BUTTON_H - MENU_TRANSPOSE_BUTTON_H, 0.2f, MENU_TRANSPOSE_BUTTON_H, new ButtonClickedAction() {
            @Override
            public void onClicked() {
                AppController.sendEvent(new TransposeEvent(-1));
            }
        }));
        buttons.add(new QuickMenuButton("0", 0.4f, 1 - MENU_AUTOSCROLL_BUTTON_H - MENU_TRANSPOSE_BUTTON_H, 0.2f, MENU_TRANSPOSE_BUTTON_H, new ButtonClickedAction() {
            @Override
            public void onClicked() {
                AppController.sendEvent(new TransposeResetEvent());
            }
        }));
        buttons.add(new QuickMenuButton("+1", 0.6f, 1 - MENU_AUTOSCROLL_BUTTON_H - MENU_TRANSPOSE_BUTTON_H, 0.2f, MENU_TRANSPOSE_BUTTON_H, new ButtonClickedAction() {
            @Override
            public void onClicked() {
                AppController.sendEvent(new TransposeEvent(+1));
            }
        }));
        buttons.add(new QuickMenuButton("+5", 0.8f, 1 - MENU_AUTOSCROLL_BUTTON_H - MENU_TRANSPOSE_BUTTON_H, 0.2f, MENU_TRANSPOSE_BUTTON_H, new ButtonClickedAction() {
            @Override
            public void onClicked() {
                AppController.sendEvent(new TransposeEvent(+5));
            }
        }));

    }

    public void draw() {

        if (visible) {

            //dimmed background
            float w = canvas.getW();
            float h = canvas.getH();

            canvas.setColor(0x000000, 110);
            canvas.fillRect(0, 0, w, h);

            //info o aktualnej transpozycji
            canvas.setColor(0xffffff);
            canvas.setFont(Font.FONT_BOLD);
            //TODO tłumaczenia pl lub przenieść do strings.xml
            canvas.drawText(langStrings.resString(R.string.transposition) + ": " + chordsManager.getTransposedString(), 0.5f * w, (1 - MENU_AUTOSCROLL_BUTTON_H - MENU_TRANSPOSE_BUTTON_H) * h, Align.BOTTOM_HCENTER);

            for (QuickMenuButton button : buttons) {
                button.draw(canvas);
            }

        }
    }

    public boolean onScreenClicked(float x, float y) {

        for (QuickMenuButton button : buttons) {
            if (button.click(x / canvas.getW(), y / canvas.getH())) {
                return true;
            }
        }

        setVisible(false);

        return true;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        canvas.repaint();
    }

    @Override
    public void onEvent(IEvent event) {
        if (event instanceof ShowQuickMenuEvent) {
            setVisible(((ShowQuickMenuEvent) event).isShow());
        }
    }
}
