package igrek.songbook.logic.crdfile;

import android.graphics.Paint;

import igrek.songbook.logic.music.transposer.ChordsTransposer;
import igrek.songbook.settings.Config;

public class ChordsManager {

    private String fileContent;

    private ChordsTransposer chordsTransposer;

    private int transposed = 0;

    private CRDParser crdParser;

    private CRDModel crdModel;

    private int screenW;

    private Paint paint;


    public ChordsManager(String fileContent, int screenW, int screenH, Paint paint) {
        this.fileContent = fileContent;
        this.screenW = screenW;
        this.paint = paint;

        chordsTransposer = new ChordsTransposer();
        crdParser = new CRDParser();

        parseAndTranspose();
    }

    public CRDModel getCRDModel() {
        return crdModel;
    }

    public void parseAndTranspose(){

        String transposedContent = chordsTransposer.transposeContent(fileContent, transposed);

        crdModel = crdParser.parseFileContent(transposedContent, screenW, Config.Fonts.lineheight, paint);
    }

    public void transpose(int t){
        transposed += t;
        parseAndTranspose();
    }
}
