package igrek.songbook.domain.crdfile;

import java.util.ArrayList;
import java.util.List;

public class CRDLine {

    private int y;

    private List<CRDFragment> fragments = new ArrayList<>();

    public CRDLine() {
    }

    public void addFragment(CRDFragment fragment){
        fragments.add(fragment);
    }

    public List<CRDFragment> getFragments() {
        return fragments;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getY() {
        return y;
    }
}
