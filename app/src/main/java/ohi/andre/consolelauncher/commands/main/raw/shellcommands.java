package ohi.andre.consolelauncher.commands.main.raw;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.tuils.Tuils;

/**
 * Created by francescoandreuzzi on 19/04/16.
 */
public class shellcommands implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        Collection<String> cmds = getOSCommands(pack.context);
        List<String> commands = new ArrayList<>(cmds);

        Collections.sort(commands, Tuils::alphabeticCompare);

        Tuils.addPrefix(commands, Tuils.DOUBLE_SPACE);
        Tuils.addSeparator(commands, Tuils.SPACE);
        Tuils.insertHeaders(commands, true);

        return Tuils.toPlanString(commands, Tuils.EMPTYSTRING);
    }

    private final String[] path = {
            "/system/bin",
            "/system/xbin"
    };

    private Set<String> getOSCommands(android.content.Context context) {
        Set<String> commands = new HashSet<>();

        for (String s : path) {
            String[] f = new File(s).list();
            if(f != null) {
                commands.addAll(Arrays.asList(f));
            }
        }

        File internalBin = new File(context.getFilesDir(), "bin");
        File busybox = new File(internalBin, "busybox");
        
        if (busybox.exists()) {
            try {
                Process process = Runtime.getRuntime().exec(new String[]{busybox.getAbsolutePath(), "--list"});
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    commands.add(line.trim());
                }
                process.waitFor();
            } catch (Exception e) {
                // Fallback to just the file list if execution fails
                String[] f = internalBin.list();
                if(f != null) {
                    commands.addAll(Arrays.asList(f));
                }
            }
        }

        return commands;
    }

    @Override
    public int[] argType() {
        return new int[0];
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public int helpRes() {
        return R.string.help_shellcommands;
    }

    @Override
    public String onArgNotFound(ExecutePack info, int index) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack info, int nArgs) {
        return null;
    }
}
