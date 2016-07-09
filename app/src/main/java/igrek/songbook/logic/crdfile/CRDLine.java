package igrek.songbook.logic.crdfile;

import java.util.ArrayList;
import java.util.List;

public class CRDLine {

    private float y;

    private List<CRDFragment> fragments = new ArrayList<>();

    public CRDLine(float y) {
        this.y = y;
    }

    public CRDLine() {
    }

    public void addFragment(CRDFragment fragment){
        fragments.add(fragment);
    }

    public List<CRDFragment> getFragments() {
        return fragments;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getY() {
        return y;
    }
}
