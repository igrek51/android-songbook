package igrek.songbook.info.logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;

import com.google.common.base.Joiner;

public class Logger {
	
	protected Logger() {
	}
	
	private static int lastTagKey = 0;
	
	public void error(String message) {
		log(message, LogLevel.ERROR, "[ERROR] ");
	}
	
	public void error(Throwable ex) {
		log(ex.getMessage(), LogLevel.ERROR, "[EXCEPTION - " + ex.getClass().getName() + "] ");
		printExceptionStackTrace(ex);
	}
	
	public void fatal(final Activity activity, String e) {
		log(e, LogLevel.FATAL, "[FATAL] ");
		if (activity == null) {
			error("FATAL ERROR: No activity");
			return;
		}
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
		dlgAlert.setMessage(e);
		dlgAlert.setTitle("Critical error :(");
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
		log(message, LogLevel.WARN, "[warn]  ");
	}
	
	public void info(String message) {
		log(message, LogLevel.INFO, "[info]  ");
	}
	
	public void debug(String message) {
		log(message, LogLevel.DEBUG, "[debug] ");
	}
	
	public void debug(Object... objs) {
		String message = Joiner.on(", ").join(objs);
		log(message, LogLevel.DEBUG, "[debug] ");
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
	
	protected void log(String message, LogLevel level, String logPrefix) {
		if (level.moreOrEqualImportant(LoggerFactory.CONSOLE_LEVEL)) {
			
			String consoleMessage;
			if (level.lessOrEqualImportant(LoggerFactory.SHOW_TRACE_DETAILS_LEVEL)) {
				StackTraceElement externalTrace = getFirstExternalTrace(Thread.currentThread()
						.getStackTrace());
				
				String fileName = externalTrace.getFileName();
				int lineNumber = externalTrace.getLineNumber();
				
				consoleMessage = String.format("%s(%s:%d): %s", logPrefix, fileName, lineNumber, message);
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
	
	/**
	 * @param stackTraces array of stack traces
	 * @return first external stack trace (not in this class)
	 */
	private StackTraceElement getFirstExternalTrace(StackTraceElement[] stackTraces) {
		boolean loggerClassFound = false;
		// skip first stack traces: dalvik.system.VMStack, java.lang.Thread
		for (int i = 2; i < stackTraces.length; i++) {
			StackTraceElement stackTrace = stackTraces[i];
			String className = stackTrace.getClassName();
			// if it's not from this logger class
			if (className.equals(this.getClass().getName())) {
				loggerClassFound = true;
			} else if (loggerClassFound) {
				// only when there are previous Logger found
				return stackTrace;
			}
		}
		return stackTraces[0];
	}
	
	private void printExceptionStackTrace(Throwable ex) {
		if (LoggerFactory.SHOW_EXCEPTIONS_TRACE) {
			printError(Log.getStackTraceString(ex));
		}
	}
	
	protected void printDebug(String msg) {
		Log.d(tagWithKey(), msg);
	}
	
	protected void printInfo(String msg) {
		Log.i(tagWithKey(), msg);
	}
	
	protected void printWarn(String msg) {
		Log.w(tagWithKey(), msg);
	}
	
	protected void printError(String msg) {
		Log.e(tagWithKey(), msg);
	}
	
	/**
	 * force logcat to align all logs the same way
	 * by generating different tags for every next log.
	 * (to prevent log header cutting by logcat)
	 * https://github.com/orhanobut/logger/issues/173
	 */
	private String tagWithKey() {
		return LoggerFactory.LOG_TAG + incrementTagKey();
	}
	
	/**
	 * method synchronized due to multithreading
	 * @return next (incremented) tag key number
	 */
	private static synchronized int incrementTagKey() {
		lastTagKey = (lastTagKey + 1) % 10;
		return lastTagKey;
	}
}
