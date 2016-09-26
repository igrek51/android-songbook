package igrek.songbook.logic.crdfile;

import android.graphics.Paint;

import igrek.songbook.R;
import igrek.songbook.events.transpose.TransposeEvent;
import igrek.songbook.events.transpose.TransposeResetEvent;
import igrek.songbook.graphics.canvas.CanvasGraphics;
import igrek.songbook.graphics.infobar.InfoBarClickAction;
import igrek.songbook.logic.autoscroll.Autoscroll;
import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.controller.dispatcher.IEvent;
import igrek.songbook.logic.controller.dispatcher.IEventObserver;
import igrek.songbook.logic.controller.services.IService;
import igrek.songbook.logic.music.transposer.ChordsTransposer;
import igrek.songbook.resources.UserInfoService;

public class ChordsManager implements IService, IEventObserver {

    //TODO klasa do przechowywania danych tymczasowej konfiguracji przeglądarki akordów, zmiana nazwy
    //TODO dodanie obsługi przez eventy, usunięcie z pola App, pełny service

    private int transposed = 0;

    private CRDParser crdParser;

    private CRDModel crdModel;

    private int screenW = 0;

    private Paint paint = null;

    private float fontsize = 23.0f;

    private String originalFileContent = null;


    public ChordsManager() {
        crdParser = new CRDParser();
        AppController.registerEventObserver(TransposeEvent.class, this);
        AppController.registerEventObserver(TransposeResetEvent.class, this);
    }

    public void reset() {
        transposed = 0;
        Autoscroll autoscroll = AppController.getService(Autoscroll.class);
        autoscroll.reset();
    }

    public void load(String fileContent, Integer screenW, Integer screenH, Paint paint) {
        reset();
        originalFileContent = fileContent;

        if (screenW != null) {
            this.screenW = screenW;
        }
        if (paint != null) {
            this.paint = paint;
        }

        crdParser = new CRDParser();

        parseAndTranspose(originalFileContent);
    }


    public void reparse() {
        parseAndTranspose(originalFileContent);
    }

    public CRDModel getCRDModel() {
        return crdModel;
    }

    public float getFontsize() {
        return fontsize;
    }

    public void setFontsize(float fontsize) {
        if (fontsize < 1) fontsize = 1;
        this.fontsize = fontsize;
        Autoscroll autoscroll = AppController.getService(Autoscroll.class);
        autoscroll.setFontsize(fontsize);
    }

    private void parseAndTranspose(String originalFileContent) {

        ChordsTransposer chordsTransposer = AppController.getService(ChordsTransposer.class);

        String transposedContent = chordsTransposer.transposeContent(originalFileContent, transposed);

        crdModel = crdParser.parseFileContent(transposedContent, screenW, fontsize, paint);
    }

    public void transpose(int t) {
        transposed += t;
        if (transposed >= 12) transposed -= 12;
        if (transposed <= -12) transposed += 12;
        parseAndTranspose(originalFileContent);
    }

    public int getTransposed() {
        return transposed;
    }

    public String getTransposedString() {
        return (transposed > 0 ? "+" : "") + transposed;
    }

    @Override
    public void onEvent(IEvent event) {

        if (event instanceof TransposeEvent) {

            int t = ((TransposeEvent) event).getT();

            UserInfoService userInfo = AppController.getService(UserInfoService.class);

            transpose(t);

            CanvasGraphics canvas = AppController.getService(CanvasGraphics.class);
            canvas.setCRDModel(getCRDModel());

            String info = userInfo.resString(R.string.transposition) + ": " + getTransposedString();

            if (getTransposed() != 0) { //włączono niezerową transpozycję

                userInfo.showActionInfo(info, null, userInfo.resString(R.string.transposition_reset), new InfoBarClickAction() {
                    @Override
                    public void onClick() {
                        AppController.sendEvent(new TransposeResetEvent());
                    }
                });

            } else {
                userInfo.showActionInfo(info, null, userInfo.resString(R.string.action_info_ok), null);
            }

        } else if (event instanceof TransposeResetEvent) {

            AppController.sendEvent(new TransposeEvent(-getTransposed()));

        }
    }
}
