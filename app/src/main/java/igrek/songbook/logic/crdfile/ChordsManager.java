package igrek.songbook.logic.crdfile;

import android.graphics.Paint;

import igrek.songbook.logic.autoscroll.Autoscroll;
import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.controller.services.IService;
import igrek.songbook.logic.music.transposer.ChordsTransposer;

public class ChordsManager implements IService {

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
}
