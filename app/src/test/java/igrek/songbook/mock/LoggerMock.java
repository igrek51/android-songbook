package igrek.songbook.mock;


import android.app.Activity;

import igrek.songbook.info.logger.LogLevel;
import igrek.songbook.info.logger.Logger;

public class LoggerMock extends Logger {
	
	@Override
	public void fatal(Activity activity, String e) {
		log(e, LogLevel.FATAL, "[FATAL ERROR] ");
	}
	
	@Override
	protected void printInfo(String msg) {
		System.out.println(msg);
	}
	
	@Override
	protected void printError(String msg) {
		System.err.println(msg);
	}
	
	@Override
	protected void printDebug(String msg) {
		System.out.println(msg);
	}
	
	@Override
	protected void printWarn(String msg) {
		System.out.println(msg);
	}
	
}
