package igrek.songbook.system.filesystem;

import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

public class CharsetDetector {
	
	private final byte[] cp1250PLBytes = {
			(byte) 0xB9, (byte) 0xBF, (byte) 0x9C, (byte) 0x9F, (byte) 0xEA, (byte) 0xE6,
			(byte) 0xF1, (byte) 0xF3, (byte) 0xB3, (byte) 0xA5, (byte) 0xAF, (byte) 0x8C,
			(byte) 0x8F, (byte) 0xCA, (byte) 0xC6, (byte) 0xD1, (byte) 0xD3, (byte) 0xA3
	};
	private final byte[] utf8PLPrefixBytes = {(byte) 0xC3, (byte) 0xC4, (byte) 0xC5};
	private final String CHARSET_UTF8 = "UTF-8";
	private final String CHARSET_CP1250 = "Cp1250";
	private Logger logger = LoggerFactory.INSTANCE.getLogger();
	
	public CharsetDetector() {
	}
	
	public String detect(byte[] bytes) {
		// if file contains special characters from utf8
		if (containsBytes(bytes, utf8PLPrefixBytes)) {
			logger.info("Encoding detected: " + CHARSET_UTF8);
			return CHARSET_UTF8;
		}
		
		// if file contains polish letters from CP1250
		if (containsBytes(bytes, cp1250PLBytes)) {
			logger.info("Encoding detected: " + CHARSET_CP1250);
			return CHARSET_CP1250;
		}
		
		logger.info("Default encoding: " + CHARSET_UTF8);
		return CHARSET_UTF8;
	}
	
	public byte[] repair(byte[] in, String charset) {
		if (charset.equals(CHARSET_UTF8)) {
			for (int i = 0; i < in.length; i++) {
				//krzaki zamiast apostrofu
				if (in[i] == (byte) 0x92) {
					in[i] = (byte) '\'';
				}
			}
		}
		return in;
	}
	
	private String byte2hex(byte b) {
		return String.format("%02X ", b);
	}
	
	private boolean containsByte(byte[] bytes, byte b) {
		for (byte b1 : bytes) {
			if (b1 == b)
				return true;
		}
		return false;
	}
	
	private boolean containsBytes(byte[] bytes1, byte[] bytes2) {
		for (byte b1 : bytes1) {
			if (containsByte(bytes2, b1)) {
				return true;
			}
		}
		return false;
	}
}
