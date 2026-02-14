package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class BusyBoxInstaller {

    private static final String BUSYBOX_BIN = "busybox";

    public static boolean isInstalled(Context context) {
        File busyboxFile = new File(new File(context.getFilesDir(), "bin"), BUSYBOX_BIN);
        return busyboxFile.exists() && busyboxFile.canExecute();
    }

    public interface InstallationCallback {
        void onSuccess();
        void onError(String error);
    }

    public static void setup(Context context) {
        // Not used for now
    }

    public static void install(final Context context, final InstallationCallback callback) {
        new Thread(() -> {
            File binDir = new File(context.getFilesDir(), "bin");
            if (!binDir.exists()) binDir.mkdir();
            File outputFile = new File(binDir, BUSYBOX_BIN);

            try {
                // Modern Android workaround: Use the file bundled in native library dir
                // Android allows execution from this path.
                File nativeLib = new File(context.getApplicationInfo().nativeLibraryDir, "libbusybox.so");
                
                if (!nativeLib.exists()) throw new Exception("Native library not found.");

                InputStream is = new FileInputStream(nativeLib);
                FileOutputStream os = new FileOutputStream(outputFile);
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
                os.close();
                is.close();

                outputFile.setExecutable(true, false);
                
                // Final fix for modern Android: try to run it via sh if direct exec fails
                createSymlinks(outputFile.getAbsolutePath(), binDir.getAbsolutePath());

                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void uninstall(Context context) {
        File binDir = new File(context.getFilesDir(), "bin");
        if (binDir.exists()) {
            File[] files = binDir.listFiles();
            if (files != null) {
                for (File f : files) f.delete();
            }
        }
    }

    private static void createSymlinks(String busyboxPath, String binDir) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/system/bin/sh", "-c", busyboxPath + " --install -s " + binDir});
            process.waitFor();
        } catch (Exception e) {
            Tuils.log(e);
        }
    }
}
