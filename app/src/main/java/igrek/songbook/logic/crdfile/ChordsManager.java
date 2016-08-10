package igrek.songbook.logic.crdfile;

import android.graphics.Paint;

import igrek.songbook.graphics.gui.GUIListener;
import igrek.songbook.logic.autoscroll.Autoscroll;
import igrek.songbook.logic.music.transposer.ChordsTransposer;

public class ChordsManager {

    private ChordsTransposer chordsTransposer;

    private int transposed = 0;

    private CRDParser crdParser;

    private CRDModel crdModel;

    private int screenW = 0;

    private Paint paint = null;

    private float fontsize = 21.0f;

    private String originalFileContent = null;

    //TODO przenieść obsługę autoscrolla do innej klasy lub zmienić sposób komunikacji
    private Autoscroll autoscroll;


    public ChordsManager(GUIListener guiListener) {
        chordsTransposer = new ChordsTransposer();
        crdParser = new CRDParser();
        autoscroll = new Autoscroll(guiListener, fontsize);
    }

    public void reset() {
        transposed = 0;
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

        chordsTransposer = new ChordsTransposer();
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
        autoscroll.setFontsize(fontsize);
    }

    private void parseAndTranspose(String originalFileContent) {

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

    public void autoscrollStart(float scroll) {
        autoscroll.start(scroll);
    }

    public void autoscrollStop() {
        autoscroll.stop();
    }

    public Autoscroll getAutoscroll() {
        return autoscroll;
    }
}
