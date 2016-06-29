package igrek.songbook.graphics.canvas;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import igrek.songbook.graphics.canvas.enums.Align;
import igrek.songbook.graphics.gui.GUIListener;
import igrek.songbook.settings.Config;

public class CanvasGraphics extends BaseCanvasGraphics {


    public String fileContent = null;

    public String filename = null;

    private List<String> lines = null;
    private int scroll = 0;

    public CanvasGraphics(Context context, GUIListener guiListener) {
        super(context, guiListener);
    }

    public void setFileContent(String fileContent) {
        w = getWidth();
        h = getHeight();
        this.fileContent = fileContent;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public void initialized(){
        super.initialized();
    }

    @Override
    public void repaint() {
        drawBackground();
        if (!init) return;
        if(w == 0 && h == 0){
            w = getWidth();
            h = getHeight();
        }
        if(lines == null) {
            setFontSize(Config.Fonts.fontsize);
            lines = splitLines(fileContent);
        }
        drawFileContent();
    }

    private void drawFileContent(){
        setFontSize(Config.Fonts.fontsize);

        setColor(0xffffff);

        int index = 0;
        for(String line : lines){
            drawLine(line, index * Config.Fonts.lineheight - scroll);
            index++;
        }
    }

    private void drawBackground() {
        setColor(Config.Colors.background);
        clearScreen();
    }

    private void drawLine(String line, float y){
        drawText(line, 0, y + 1, Align.TOP_LEFT);
    }

    private List<String> splitLines(String fileContent){
        fileContent.replace("\r", "");
        List<String> lines3 = new ArrayList<>();
        String[] lines1 = fileContent.split("\n");
        for(String line1 : lines1){
            lines3.addAll(splitLine(line1));
        }
        return lines3;
    }

    private List<String> splitLine(String line1){
        List<String> lines2 = new ArrayList<>();
        if(getTextWidth(line1) <= w){
            lines2.add(line1);
        }else{
            int maxLength = getMaxStringLength(line1);
            String before = line1.substring(0, maxLength);
            lines2.add(before);
            String after = line1.substring(maxLength);
            lines2.addAll(splitLine(after));
        }
        return lines2;
    }

    /**
     * @param str
     * @return maksymalna liczba znaków tekstu (od początku) mieszcząca się w całości na ekranie
     */
    private int getMaxStringLength(String str){
        int l = str.length();
        while(getTextWidth(str.substring(0, l)) > w && l > 1){
            l--;
        }
        return l;
    }
}
