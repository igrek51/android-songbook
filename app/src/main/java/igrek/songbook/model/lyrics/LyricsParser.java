package igrek.songbook.model.lyrics;

import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LyricsParser {
	
	private boolean bracket;
	private Paint paint;
	private final Set<String> wordSplitters = new HashSet<>(Arrays.asList(" ", ".", ",", "-", ":", ";", "/"));
	
	public LyricsParser() {
	}
	
	public synchronized LyricsModel parseFileContent(String content, float screenW, float fontsize, Paint paint) {
		this.paint = paint;
		setNormalFont();
		paint.setTextSize(fontsize);
		
		content = content.replace("\r", "");
		content = content.replace("\t", " ");
		LyricsModel model = new LyricsModel();
		setBracket(false);
		String[] lines1 = content.split("\n");
		for (String line1 : lines1) {
			model.addLines(parseLine(line1, screenW, fontsize));
		}
		
		// store line numbers
		int y = 0;
		for (LyricsLine line : model.getLines()) {
			line.setY(y);
			y++;
		}
		
		return model;
	}
	
	private List<LyricsLine> parseLine(String line, float screenW, float fontsize) {
		
		List<LyricsChar> chars = str2chars(line.trim());
		
		List<List<LyricsChar>> lines2 = wrapLine(chars, screenW);
		
		List<LyricsLine> lines = new ArrayList<>();
		for (List<LyricsChar> subline : lines2) {
			lines.add(chars2line(subline, fontsize));
		}
		
		return lines;
	}
	
	private List<LyricsChar> str2chars(String line) {
		List<LyricsChar> chars = new ArrayList<>();
		for (int i = 0; i < line.length(); i++) {
			String c = Character.toString(line.charAt(i));
			
			float charWidth;
			LyricsTextType type;
			
			if (c.equals("[")) {
				setBracket(true);
				charWidth = 0;
				type = LyricsTextType.BRACKET;
			} else if (c.equals("]")) {
				setBracket(false);
				charWidth = 0;
				type = LyricsTextType.BRACKET;
			} else {
				float[] fw = new float[1];
				paint.getTextWidths(c, fw);
				charWidth = fw[0];
				if (bracket) {
					type = LyricsTextType.CHORDS;
				} else {
					type = LyricsTextType.REGULAR_TEXT;
				}
			}
			
			chars.add(new LyricsChar(c, charWidth, type));
		}
		return chars;
	}
	
	
	private float textWidth(List<LyricsChar> chars) {
		float sum = 0;
		for (LyricsChar achar : chars) {
			sum += achar.width;
		}
		return sum;
	}
	
	private int maxScreenStringLength(List<LyricsChar> chars, float screenW) {
		int l = chars.size();
		while (textWidth(chars.subList(0, l)) > screenW && l > 1) {
			l--;
		}
		// do not wrap in the middle of the word, try to step back until word splitter found
		int lastWordSplitter = findLastWordSplitter(chars, l);
		if (lastWordSplitter == -1) {
			// it's one long word only - no way to split
			return l;
		} else if (lastWordSplitter == l - 1) {
			// last char is already a word splitter
			return l;
		} else {
			// split after last word splitter
			return lastWordSplitter + 1;
		}
	}
	
	private int findLastWordSplitter(List<LyricsChar> chars, int toIndex) {
		while (--toIndex > 1) {
			// is word splitter
			if (wordSplitters.contains(chars.get(toIndex).c))
				return toIndex;
		}
		return -1;
	}
	
	private List<List<LyricsChar>> wrapLine(List<LyricsChar> chars, float screenW) {
		List<List<LyricsChar>> lines = new ArrayList<>();
		if (textWidth(chars) <= screenW) {
			lines.add(chars);
		} else {
			int maxLength = maxScreenStringLength(chars, screenW);
			List<LyricsChar> before = chars.subList(0, maxLength);
			// copying due to subsequent modifications
			ArrayList<LyricsChar> newBefore = new ArrayList<>(before);
			// add special line wrapper
			newBefore.add(new LyricsChar("\u21B5", 0, LyricsTextType.LINEWRAPPER));
			List<LyricsChar> after = chars.subList(maxLength, chars.size());
			lines.add(newBefore);
			lines.addAll(wrapLine(after, screenW));
		}
		return lines;
	}
	
	private synchronized LyricsLine chars2line(List<LyricsChar> chars, float fontsize) {
		// aggregate groups of the same type
		LyricsLine line = new LyricsLine();
		
		LyricsTextType lastType = null;
		StringBuilder buffer = new StringBuilder();
		float startX = 0;
		float x = 0;
		for (int i = 0; i < chars.size(); i++) {
			LyricsChar lyricsChar = chars.get(i);
			if (lastType == null)
				lastType = lyricsChar.type;
			
			if (lyricsChar.type != lastType) {
				// complete the previous fragment
				if (buffer.length() > 0) {
					
					if (lastType.isDisplayable()) {
						LyricsFragment fragment = new LyricsFragment(startX / fontsize, buffer.toString(), lastType);
						line.addFragment(fragment);
					}
					
					startX = x;
					buffer = new StringBuilder();
				}
				
				lastType = lyricsChar.type;
			}
			
			if (lyricsChar.type.isDisplayable()) {
				buffer.append(lyricsChar.c);
				x += lyricsChar.width;
			}
		}
		
		// complete the last fragment
		if (buffer.length() > 0) {
			if (lastType.isDisplayable()) {
				LyricsFragment fragment = new LyricsFragment(startX / fontsize, buffer.toString(), lastType);
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
