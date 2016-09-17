package igrek.songbook.graphics.canvas.quickmenu;

import java.util.ArrayList;
import java.util.List;

import igrek.songbook.events.ShowQuickMenuEvent;
import igrek.songbook.events.autoscroll.AutoscrollStartUIEvent;
import igrek.songbook.events.autoscroll.AutoscrollStopUIEvent;
import igrek.songbook.events.transpose.TransposeEvent;
import igrek.songbook.events.transpose.TransposeResetEvent;
import igrek.songbook.graphics.canvas.CanvasGraphics;
import igrek.songbook.graphics.canvas.enums.Font;
import igrek.songbook.logic.autoscroll.Autoscroll;
import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.controller.dispatcher.IEvent;
import igrek.songbook.logic.controller.dispatcher.IEventObserver;
import igrek.songbook.logic.controller.services.IService;

//TODO jedna instacja, ponowne wykorzystanie klasy

public class QuickMenu implements IService, IEventObserver {

    private CanvasGraphics canvas;

    private boolean visible = false;

    private final float MENU_TRANSPOSE_BUTTON_H = 0.2f;
    private final float MENU_AUTOSCROLL_BUTTON_H = 0.2f;

    private List<QuickMenuButton> buttons;

    public QuickMenu(CanvasGraphics canvas) {
        this.canvas = canvas;

        initButtons();

        AppController.registerService(this);

        AppController.clearEventObservers(ShowQuickMenuEvent.class);
        AppController.registerEventObserver(ShowQuickMenuEvent.class, this);
    }

    private void initButtons() {
        buttons = new ArrayList<>();

        //TODO button extends QuickMenuButton z nadpisaną metodą getText(), dynamiczny tekst na podstawie stanu autoscrolla
        //TODO 2 buttony: autoscroll wait, autoscroll now
        buttons.add(new QuickMenuButton("Autoscroll", 0, 1 - MENU_AUTOSCROLL_BUTTON_H, 1, MENU_AUTOSCROLL_BUTTON_H, new ButtonClickedAction() {
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

            canvas.setFont(Font.FONT_NORMAL);
            canvas.setColor(0x000000, 110);
            canvas.fillRect(0, 0, w, h);

            for (QuickMenuButton button : buttons) {
                button.draw(canvas);
            }

            //TODO informacja o aktualnej transpozycji, tytuł Transpozycja do buttonów
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
