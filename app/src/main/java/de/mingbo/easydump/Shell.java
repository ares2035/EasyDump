package de.mingbo.easydump;

import android.content.Intent;
import android.text.TextUtils;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mingbo.utils.ShellUtils;

import static de.mingbo.utils.ShellUtils.ShellResult;

/**
 * Created by shaomingbo on 14-9-25.
 */
public class Shell {


    public static boolean isRunning() {
        if (!Configuration.installed() || !ShellUtils.su()) {
            return false;
        }

        try {
            ShellResult result = ShellUtils.sudo("ps | "
                    + Configuration.TOOL_PATH + " grep tcpdump | "
                    + Configuration.TOOL_PATH + " wc -l");
            if (result.result != 0)
                return false;
            Pattern digits = Pattern.compile(".*(\\d+).*", Pattern.DOTALL);
            Matcher m = digits.matcher(result.message);
            if (m.matches()) {
                return Integer.parseInt(m.group(1)) > 0;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static void dumpStop() {
        if (!Configuration.installed()) {
            App.get().sendBroadcast(new Intent(Configuration.NOT_INSTALLED));
            return;
        }

        if (!ShellUtils.su()) {
            App.get().sendBroadcast(new Intent(Configuration.NO_PERMISSION));
            return;
        }

        try {
            ShellResult shell = ShellUtils.sudo(Configuration.TOOL_PATH + " killall " + Configuration.TCP_DUMP_BIN);
            if (shell.result == 0) {
                App.get().sendBroadcast(new Intent(Configuration.DUMP_STOPPED));
                return;
            }

        } catch (Exception e) {
            Utils.d(e.getMessage());
        }

        App.get().sendBroadcast(new Intent(Configuration.DUMP_STOP_EXCEPTION));
    }

    public static void dumpBegin() {
        if (!Configuration.installed()) {
            App.get().sendBroadcast(new Intent(Configuration.NOT_INSTALLED));
            return;
        }

        if (!ShellUtils.su()) {
            App.get().sendBroadcast(new Intent(Configuration.NO_PERMISSION));
            return;
        }

        Process process = null;

        try {
            process = Runtime.getRuntime().exec(ShellUtils.suShell);
            final InputStream is = process.getErrorStream();
            DataOutputStream shell = new DataOutputStream(process.getOutputStream());
            addMonitor(is);
            shell.writeBytes(Configuration.dumpParameter() + ShellUtils.EOL);
            shell.flush();

            App.get().sendBroadcast(new Intent(Configuration.DUMP_STARTED));
            process.waitFor();
        } catch (Exception e) {
            Intent action = new Intent(Configuration.TITLE_UPDATE);
            action.putExtra("title", e.getMessage());
            App.get().sendBroadcast(action);
            App.get().sendBroadcast(new Intent(Configuration.DUMP_EXCEPTION));
        } finally {
            if (process != null)
                process.destroy();
        }
    }

    public static void install() {
        File dir = Configuration.appDir();
        if (!dir.exists() && !dir.mkdirs()) {
            App.get().sendBroadcast(new Intent(Configuration.INSTALL_FAILED));
            return;
        }

        InputStream binStream = null, toolStream = null;
        OutputStream binOS = null, toolOS = null;
        try {
            binStream = App.get().getAssets().open(Configuration.TCP_DUMP_BIN);
            binOS = new FileOutputStream(Configuration.BIN_PATH);
            ByteStreams.copy(binStream, binOS);
            ShellUtils.ShellResult binResult = ShellUtils.sudo("chmod 755 " + Configuration.BIN_PATH);

            if (binResult.result != 0) {
                File bin = new File(Configuration.BIN_PATH);
                bin.delete();
                App.get().sendBroadcast(new Intent(Configuration.INSTALL_FAILED));
                return;
            }

            toolStream = App.get().getAssets().open(Configuration.TOOL_BIN);
            toolOS = new FileOutputStream(Configuration.TOOL_PATH);
            ByteStreams.copy(toolStream, toolOS);

            ShellUtils.ShellResult toolResult = ShellUtils.sudo("chmod 755 " + Configuration.TOOL_PATH);

            if (toolResult.result == 0)
                App.get().sendBroadcast(new Intent(Configuration.INSTALL_SUCCESS));
            else {
                File tool = new File(Configuration.TOOL_PATH);
                tool.delete();
                App.get().sendBroadcast(new Intent(Configuration.INSTALL_FAILED));
            }
        } catch (Exception e) {
            App.get().sendBroadcast(new Intent(Configuration.INSTALL_FAILED));
        } finally {
            Closeables.closeQuietly(binStream);
            if (binOS != null)
                try {
                    binOS.close();
                } catch (Exception e) {
                }
            if (toolOS != null)
                try {
                    toolOS.close();
                } catch (Exception e) {
                }
            App.get().sendBroadcast(new Intent(Configuration.INSTALL_COMPLETE));
        }
    }

    private static Monitor addMonitor(InputStream is) {
        return new Monitor(is);
    }

    static class Monitor extends Thread {

        InputStream is;
        StringBuilder title = new StringBuilder();

        public Monitor(InputStream is) {
            this.is = is;
            start();
        }


        void sendTitle() {
            String message = title.toString();
            if (TextUtils.isEmpty(message))
                return;
            Intent action = new Intent(Configuration.TITLE_UPDATE);
            action.putExtra("title", message);
            App.get().sendBroadcast(action);
            App.get().created = false;
        }

        @Override
        public void run() {
            super.run();

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Intent action = new Intent(Configuration.TITLE_UPDATE);
                action.putExtra("title", e.getMessage());
                App.get().sendBroadcast(action);

                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;


            Pattern message = Pattern.compile("\\s*got\\s*\\d+\\s*", Pattern.CASE_INSENSITIVE);


            try {
                while (true) {
                    line = reader.readLine();
                    if (TextUtils.isEmpty(line))
                        continue;

                    if (message.matcher(line).matches()) {
                        Intent action = new Intent(Configuration.MESSAGE_UPDATE);
                        action.putExtra("msg", line);
                        App.get().sendBroadcast(action);
                        /**
                         * 当界面被系统销毁之后，恢复title 的显示
                         */
                        if (App.get().created) {
                            sendTitle();
                        }
                    } else {
                        title.append("\n").append(line);
                        sendTitle();
                    }
                }
            } catch (IOException e) {
                title.append("\n").append("execute error: " + e.getMessage());
                Intent action = new Intent(Configuration.TITLE_UPDATE);
                action.putExtra("title", title.toString());
                App.get().sendBroadcast(action);
            }
        }
    }

}
