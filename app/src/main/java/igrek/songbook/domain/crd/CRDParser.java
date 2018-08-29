package igrek.songbook.domain.crd;

import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;

public class CRDParser {
	
	private boolean bracket;
	
	private Paint paint;
	
	public CRDParser() {
	}
	
	public synchronized CRDModel parseFileContent(String content, float screenW, float fontsize, Paint paint) {
		this.paint = paint;
		setNormalFont();
		paint.setTextSize(fontsize);
		
		content = content.replace("\r", "");
		content = content.replace("\t", " ");
		CRDModel model = new CRDModel();
		setBracket(false);
		String[] lines1 = content.split("\n");
		for (String line1 : lines1) {
			model.addLines(parseLine(line1, screenW, fontsize));
		}
		
		// store line numbers
		int y = 0;
		for (CRDLine line : model.getLines()) {
			line.setY(y);
			y++;
		}
		
		return model;
	}
	
	private List<CRDLine> parseLine(String line, float screenW, float fontsize) {
		
		List<CRDChar> chars = str2chars(line);
		
		List<List<CRDChar>> lines2 = wrapLine(chars, screenW);
		
		List<CRDLine> lines = new ArrayList<>();
		for (List<CRDChar> subline : lines2) {
			lines.add(chars2line(subline, fontsize));
		}
		
		return lines;
	}
	
	private List<CRDChar> str2chars(String line) {
		List<CRDChar> chars = new ArrayList<>();
		for (int i = 0; i < line.length(); i++) {
			String c = line.substring(i, i + 1);
			
			float charWidth;
			CRDTextType type;
			
			if (c.equals("[")) {
				setBracket(true);
				charWidth = 0;
				type = CRDTextType.BRACKET;
			} else if (c.equals("]")) {
				setBracket(false);
				charWidth = 0;
				type = CRDTextType.BRACKET;
			} else {
				float[] fw = new float[1];
				paint.getTextWidths(c, fw);
				charWidth = fw[0];
				if (bracket) {
					type = CRDTextType.CHORDS;
				} else {
					type = CRDTextType.REGULAR_TEXT;
				}
			}
			
			chars.add(new CRDChar(c, charWidth, type));
		}
		return chars;
	}
	
	
	private float textWidth(List<CRDChar> chars) {
		float sum = 0;
		for (CRDChar achar : chars) {
			sum += achar.width;
		}
		return sum;
	}
	
	private int maxScreenStringLength(List<CRDChar> chars, float screenW) {
		int l = chars.size();
		while (textWidth(chars.subList(0, l)) > screenW && l > 1) {
			l--;
		}
		return l;
	}
	
	
	private List<List<CRDChar>> wrapLine(List<CRDChar> chars, float screenW) {
		List<List<CRDChar>> lines = new ArrayList<>();
		if (textWidth(chars) <= screenW) {
			lines.add(chars);
		} else {
			int maxLength = maxScreenStringLength(chars, screenW);
			List<CRDChar> before = chars.subList(0, maxLength);
			ArrayList<CRDChar> newBefore = new ArrayList<>(before);
			newBefore.add(new CRDChar("\u21B5", 0, CRDTextType.LINEWRAPPER));
			List<CRDChar> after = chars.subList(maxLength, chars.size());
			lines.add(newBefore);
			lines.addAll(wrapLine(after, screenW));
		}
		return lines;
	}
	
	private synchronized CRDLine chars2line(List<CRDChar> chars, float fontsize) {
		// aggregate groups of the same type
		CRDLine line = new CRDLine();
		
		CRDTextType lastType = null;
		StringBuilder buffer = new StringBuilder();
		float startX = 0;
		float x = 0;
		for (int i = 0; i < chars.size(); i++) {
			CRDChar crdChar = chars.get(i);
			if (lastType == null)
				lastType = crdChar.type;
			
			if (crdChar.type != lastType) {
				// complete the previous fragment
				if (buffer.length() > 0) {
					
					if (lastType.isDisplayable()) {
						CRDFragment fragment = new CRDFragment(startX / fontsize, buffer.toString(), lastType);
						line.addFragment(fragment);
					}
					
					startX = x;
					buffer = new StringBuilder();
				}
				
				lastType = crdChar.type;
			}
			
			if (crdChar.type.isDisplayable()) {
				buffer.append(crdChar.c);
				x += crdChar.width;
			}
		}
		
		// complete the last fragment
		if (buffer.length() > 0) {
			if (lastType.isDisplayable()) {
				CRDFragment fragment = new CRDFragment(startX / fontsize, buffer.toString(), lastType);
				line.addFragment(fragment);
			}
		}
		
		return line;
	}
	
	
	private void setBracket(boolean bracket) {
		this.bracket = bracket;
		// change typeface due to text width calculation
		if (bracket) {
			setBoldFont();
		} else {
			setNormalFont();
		}
	}
	
	private void setNormalFont() {
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
	}
	
	private void setBoldFont() {
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
	}
}
