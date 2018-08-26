package igrek.songbook.domain.crdfile;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

public class CRDModel {
	
	private List<CRDLine> lines;
	
	public CRDModel() {
		lines = new ArrayList<>();
	}
	
	public void addLines(List<CRDLine> lines) {
		this.lines.addAll(lines);
	}
	
	public void addLine(CRDLine line) {
		lines.add(line);
	}
	
	public List<CRDLine> getLines() {
		return lines;
	}
	
	@Override
	public String toString() {
		return Joiner.on("\n").join(lines);
	}
}
