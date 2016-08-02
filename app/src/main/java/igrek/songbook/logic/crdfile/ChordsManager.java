package igrek.songbook.logic.crdfile;

import android.graphics.Paint;

import igrek.songbook.logic.music.transposer.ChordsTransposer;

public class ChordsManager {

    private String fileContent = null;

    private ChordsTransposer chordsTransposer;

    private int transposed = 0;

    private CRDParser crdParser;

    private CRDModel crdModel;

    private int screenW = 0;

    private Paint paint = null;

    private float fontsize = 21.0f;


    public ChordsManager() {

        chordsTransposer = new ChordsTransposer();
        crdParser = new CRDParser();
    }

    public void load(String fileContent, Integer screenW, Integer screenH, Paint paint){
        this.fileContent = fileContent;
        if(screenW != null) {
            this.screenW = screenW;
        }
        if(paint != null) {
            this.paint = paint;
        }

        chordsTransposer = new ChordsTransposer();
        crdParser = new CRDParser();

        parseAndTranspose();
    }

    public CRDModel getCRDModel() {
        return crdModel;
    }

    public float getFontsize() {
        return fontsize;
    }

    public void setFontsize(float fontsize) {
        if(fontsize < 1) fontsize = 1;
        this.fontsize = fontsize;
    }

    public void parseAndTranspose(){

        String transposedContent = chordsTransposer.transposeContent(fileContent, transposed);

        crdModel = crdParser.parseFileContent(transposedContent, screenW, fontsize, paint);
    }

    public void transpose(int t){
        transposed += t;
        parseAndTranspose();
    }
}
