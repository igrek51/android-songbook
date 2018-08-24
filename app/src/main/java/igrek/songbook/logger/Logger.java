package igrek.songbook.logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;

import com.google.common.base.Joiner;

public class Logger {
	
	protected Logger() {
	}
	
	public void error(String message) {
		log(message, LogLevel.ERROR, "[ERROR] ");
	}
	
	public void error(Throwable ex) {
		log(ex.getMessage(), LogLevel.ERROR, "[EXCEPTION - " + ex.getClass().getName() + "] ");
		printExceptionStackTrace(ex);
	}
	
	public void fatal(final Activity activity, String e) {
		log(e, LogLevel.FATAL, "[FATAL ERROR] ");
		if (activity == null) {
			error("FATAL ERROR: No activity");
			return;
		}
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
		dlgAlert.setMessage(e);
		dlgAlert.setTitle("Critical error");
		dlgAlert.setPositiveButton("Close app", (dialog, which) -> activity.finish());
		dlgAlert.setCancelable(false);
		dlgAlert.create().show();
	}
	
	public void fatal(final Activity activity, Throwable ex) {
		String e = ex.getClass().getName() + " - " + ex.getMessage();
		printExceptionStackTrace(ex);
		fatal(activity, e);
	}
	
	public void warn(String message) {
		log(message, LogLevel.WARN, "[warn] ");
	}
	
	public void info(String message) {
		log(message, LogLevel.INFO, "");
	}
	
	public void debug(String message) {
		log(message, LogLevel.DEBUG, "[debug] ");
	}
	
	public void debug(Object... objs) {
		String message = Joiner.on(", ").join(objs);
		log(message, LogLevel.DEBUG, "[debug] ", 6);
	}
	
	public void debug(Object obj) {
		log(obj.toString(), LogLevel.DEBUG, "[debug] ", 6);
	}
	
	/**
	 * Super debug
	 */
	public void dupa() {
		log("dupa", LogLevel.DEBUG, "[debug] ");
	}
	
	public void trace(String message) {
		log(message, LogLevel.TRACE, "[trace] ");
	}
	
	public void trace() {
		log("Quick Trace: " + System.currentTimeMillis(), LogLevel.DEBUG, "[trace] ");
	}
	
	private void log(String message, LogLevel level, String logPrefix) {
		log(message, level, logPrefix, 5);
	}
	
	protected void log(String message, LogLevel level, String logPrefix, int stackTraceIndex) {
		if (level.moreOrEqualImportant(LoggerFactory.CONSOLE_LEVEL)) {
			
			String consoleMessage;
			if (level.lessOrEqualImportant(LoggerFactory.SHOW_TRACE_DETAILS_LEVEL)) {
				// depends on nested methods count
				StackTraceElement ste = Thread.currentThread().getStackTrace()[stackTraceIndex];
				
				String methodName = ste.getMethodName();
				String fileName = ste.getFileName();
				int lineNumber = ste.getLineNumber();
				
				consoleMessage = logPrefix + "(" + fileName + ":" + lineNumber + "): " + message;
			} else {
				consoleMessage = logPrefix + message;
			}
			
			if (level.moreOrEqualImportant(LogLevel.ERROR)) {
				printError(consoleMessage);
			} else if (level.moreOrEqualImportant(LogLevel.WARN)) {
				printWarn(consoleMessage);
			} else if (level.moreOrEqualImportant(LogLevel.INFO)) {
				printInfo(consoleMessage);
			} else {
				printDebug(consoleMessage);
			}
		}
	}
	
	private void printExceptionStackTrace(Throwable ex) {
		if (LoggerFactory.SHOW_EXCEPTIONS_TRACE) {
			printError(Log.getStackTraceString(ex));
		}
	}
	
	protected void printDebug(String msg) {
		Log.d(LoggerFactory.LOG_TAG, msg);
	}
	
	protected void printInfo(String msg) {
		Log.i(LoggerFactory.LOG_TAG, msg);
	}
	
	protected void printWarn(String msg) {
		Log.w(LoggerFactory.LOG_TAG, msg);
	}
	
	protected void printError(String msg) {
		Log.e(LoggerFactory.LOG_TAG, msg);
	}
	
}
