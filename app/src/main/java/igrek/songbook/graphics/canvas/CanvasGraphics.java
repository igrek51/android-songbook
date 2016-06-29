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

    private float scroll = 0;
    private float startScroll = 0;

    private boolean bracket = false;

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
    public void init(){
        setFontSize(Config.Fonts.fontsize);
        lines = splitLines(fileContent);
    }

    @Override
    public void onRepaint() {

        drawBackground();

        drawFileContent();
    }

    private void drawFileContent(){
        setFontSize(Config.Fonts.fontsize);

        setColor(0xffffff);

        int index = 0;
        bracket = false;
        for(String line : lines){
            drawLine(line, index, Config.Fonts.lineheight, scroll);
            index++;
        }
    }

    private void drawBackground() {
        setColor(Config.Colors.background);
        clearScreen();
    }

    private void drawLine(String line, int index, float lineheight, float scroll){
        float y = index * lineheight - scroll;
        if(y > h) return;
        if(y + lineheight < 0) return;
        final float yOffset = lineheight;
        float x = 0;
        for(int i=0; i<line.length(); i++){

            String s = line.substring(i, i+1);

            if(s.equals("[")){
                bracket = true;
                setColor(0x404040);
            }else if(s.equals("]")){
                bracket = false;
                setColor(0x404040);
            }else {
                if (bracket) {
                    setColor(0xf00000);
                } else {
                    setColor(0xffffff);
                }
            }

            drawTextUnaligned(s, x, y + yOffset);

            x += getTextWidth(s);
        }
    }

    //TODO: przenieść do CRDParser
    //TODO: uwzględnić usuwane znaki [] i różną szerokość pogrubionych znaków
    //TODO: rozbić tekst na fragmenty o różnych czcionkach
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
            int maxLength = getMaxScreenStringLength(line1);
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
    private int getMaxScreenStringLength(String str){
        int l = str.length();
        while(getTextWidth(str.substring(0, l)) > w && l > 1){
            l--;
        }
        return l;
    }

    @Override
    protected void onTouchDown(float touchX, float touchY) {
        super.onTouchDown(touchX, touchY);
        startScroll = scroll;
    }

    @Override
    protected void onTouchMove(float touchX, float touchY) {
        scroll = startScroll + startTouchY - touchY;
        if(scroll < 0) scroll = 0;
        repaint();
    }
}
