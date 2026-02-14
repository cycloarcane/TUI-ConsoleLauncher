package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;

public class BusyBoxInstaller {

    private static final String PREFS_NAME = "busybox_prefs";
    private static final String ENABLED_KEY = "busybox_enabled";

    public static boolean isInstalled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(ENABLED_KEY, false);
    }

    public interface InstallationCallback {
        void onSuccess();
        void onError(String error);
    }

    public static void install(final Context context, final InstallationCallback callback) {
        // Installation on Android 10+ is just enabling the Aliases
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(ENABLED_KEY, true);
        editor.apply();
        if (callback != null) callback.onSuccess();
    }

    public static void uninstall(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(ENABLED_KEY, false);
        editor.apply();
    }

    public static String getBusyboxPath(Context context) {
        File nativeLib = new File(context.getApplicationInfo().nativeLibraryDir, "libbusybox.so");
        return nativeLib.exists() ? nativeLib.getAbsolutePath() : null;
    }
}
