package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class BusyBoxInstaller {

    private static final String BUSYBOX_BIN = "busybox";

    public static boolean isInstalled(Context context) {
        File busyboxScript = new File(new File(context.getFilesDir(), "bin"), BUSYBOX_BIN);
        return busyboxScript.exists();
    }

    public interface InstallationCallback {
        void onSuccess();
        void onError(String error);
    }

    public static void setup(Context context) {
        // Not used
    }

    public static void install(final Context context, final InstallationCallback callback) {
        new Thread(() -> {
            File binDir = new File(context.getFilesDir(), "bin");
            if (!binDir.exists()) binDir.mkdir();
            File busyboxScript = new File(binDir, BUSYBOX_BIN);

            try {
                // 1. Find the native library
                File nativeLib = new File(context.getApplicationInfo().nativeLibraryDir, "libbusybox.so");
                if (!nativeLib.exists()) throw new Exception("Native library not found.");

                // 2. Create a wrapper script named 'busybox'
                // This allows 'busybox [command]' to work correctly by invoking the .so binary
                String scriptContent = "#!/system/bin/sh\n" +
                        "exec " + nativeLib.getAbsolutePath() + " \"$@\"\n";
                
                FileOutputStream os = new FileOutputStream(busyboxScript);
                os.write(scriptContent.getBytes());
                os.flush();
                os.close();

                busyboxScript.setExecutable(true, false);
                
                // 3. Create symlinks for all other commands to point to our wrapper script
                createSymlinks(busyboxScript.getAbsolutePath(), binDir.getAbsolutePath(), nativeLib.getAbsolutePath());

                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (busyboxScript.exists()) busyboxScript.delete();
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

    private static void createSymlinks(String scriptPath, String binDir, String binaryPath) {
        try {
            // Ask the binary to install symlinks to the binDir
            // We use the binary path to do the install, but it will create links to itself
            // So we'll do a second pass to make symlinks point to our wrapper script
            Process process = Runtime.getRuntime().exec(new String[]{"/system/bin/sh", "-c", binaryPath + " --install -s " + binDir});
            process.waitFor();
        } catch (Exception e) {
            Tuils.log(e);
        }
    }
}
