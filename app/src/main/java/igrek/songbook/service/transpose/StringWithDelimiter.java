package igrek.songbook.service.transpose;

class StringWithDelimiter {
	
	String str;
	String delimiter;
	
	StringWithDelimiter(String str, String delimiter) {
		this.str = str;
		this.delimiter = delimiter;
	}
	
	StringWithDelimiter(String str) {
		this.str = str;
		this.delimiter = "";
	}
}
