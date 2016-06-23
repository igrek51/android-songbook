package igrek.todotree.system.output;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import igrek.todotree.settings.Config;

public class Output {

    private static List<String> echos;
    private static long lastEcho;
    private static int errors = 0;

    public Output() {
        reset();
    }

    public static void reset() {
        echos = new ArrayList<>();
        lastEcho = 0;
        errors = 0;
    }

    //  LOG
    public static void log(String l) {
        Log.i(Config.Output.logTag, errorPrefix() + l);
    }

    public static void logError(String l) {
        Log.e(Config.Output.logTag, errorPrefix() + l);
    }

    public static void logvar(String l, int i) {
        log(l + " = " + i);
    }

    public static void logvar(String l, float f) {
        log(l + " = " + f);
    }

    //  ERRORS, EXCEPTIONS
    public static void error(String e) {
        errors++;
        logError("[ERROR] " + e);
    }

    public static void error(Throwable ex) {
        errors++;
        logError("[EXCEPTION - " + ex.getClass().getName() + "] " + ex.getMessage());
        if (Config.Output.show_exceptions_trace) {
            printStackTrace(ex);
        }
    }

    public static void errorUncaught(Throwable ex) {
        errors++;
        logError("[UNCAUGHT EXCEPTION - " + ex.getClass().getName() + "] " + ex.getMessage());
        if (Config.Output.show_exceptions_trace) {
            printStackTrace(ex);
        }
    }

    public static void printStackTrace(Throwable ex) {
        logError(Log.getStackTraceString(ex));
    }

    public static void errorThrow(String e) throws Exception {
        throw new Exception(e);
    }

    public static void errorCritical(final Activity activity, String e) {
        errors++;
        logError("[CRITICAL ERROR] " + e);
        if (activity == null) {
            error("errorCritical: Brak activity");
            return;
        }
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
        dlgAlert.setMessage(e);
        dlgAlert.setTitle("Błąd krytyczny");
        dlgAlert.setPositiveButton("Zamknij aplikację", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }

    public static void errorCritical(final Activity activity, Throwable ex) {
        String e = ex.getClass().getName() + " - " + ex.getMessage();
        if (Config.Output.show_exceptions_trace) {
            printStackTrace(ex);
        }
        errorCritical(activity, e);
    }

    private static String errorPrefix() {
        return (errors > 0) ? ("[E:" + errors + "] ") : "";
    }

    //  INFO, ECHO - widoczne dla użytkownika

    public static void info(String i) {
        echo(i);
        log("[info] " + i);
    }

    private static void echo(String e) {
        echos.add(e);
        lastEcho = System.currentTimeMillis();
    }

    public static void echoClearAfterDelay() {
        if (!echos.isEmpty()) {
            if (System.currentTimeMillis() > lastEcho + Config.Output.echo_showtime) {
                echoClear1Line();
                lastEcho += Config.Output.echo_showtime;
            }
        }
    }

    public static void echoClear1Line() {
        if (!echos.isEmpty()) {
            echos.remove(0);
        }
    }

    public static void echoWait(int waitms) {
        lastEcho = System.currentTimeMillis() + waitms;
    }

    public static List<String> getEchos() {
        return echos;
    }

    public static String getEchosMultiline() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < echos.size(); i++) {
            builder.append(echos.get(i));
            if (i < echos.size() - 1) builder.append('\n');
        }
        return builder.toString();
    }

    public static String getEchosMultilineReversed() {
        StringBuilder builder = new StringBuilder();
        for (int i = echos.size() - 1; i >= 0; i--) {
            builder.append(echos.get(i));
            if (i > 0) builder.append('\n');
        }
        return builder.toString();
    }
}
