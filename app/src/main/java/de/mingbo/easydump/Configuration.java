package de.mingbo.easydump;

import android.os.Environment;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shaomingbo on 14-9-25.
 */
public class Configuration {

    public static final String TAG = "easydump";
    public static final String TCP_DUMP_BIN = "tcpdump";
    public static final String TOOL_BIN = "busybox";
    public static final String BIN_PATH = "/data/data/" + App.get().getPackageName() + File.separator + TCP_DUMP_BIN;
    public static final String TOOL_PATH = "/data/data/" + App.get().getPackageName() + File.separator + TOOL_BIN;
    public static final String INSTALL_COMPLETE = App.get().getPackageName() + ".install_complete";
    public static final String NOT_INSTALLED = App.get().getPackageName() + ".not_installed";
    public static final String NO_PERMISSION = App.get().getPackageName() + ".no_permission";
    public static final String DUMP_STARTED = App.get().getPackageName() + ".started";
    public static final String DUMP_EXCEPTION = App.get().getPackageName() + ".exception";
    public static final String DUMP_STOPPED = App.get().getPackageName() + ".stopped";
    public static final String DUMP_STOP_EXCEPTION = App.get().getPackageName() + ".stop_exception";
    public static final String WINDOW_ADDED = App.get().getPackageName() + ".window_added";
    public static final String WINDOW_REMOVED = App.get().getPackageName() + ".window_removed";
    public static final String INSTALL_FAILED = App.get().getPackageName() + ".install_failed";
    public static final String INSTALL_SUCCESS = App.get().getPackageName() + ".install_success";
    public static final String MESSAGE_UPDATE = App.get().getPackageName() + ".message_update";
    public static final String TITLE_UPDATE = App.get().getPackageName() + ".title_update";
    public static final String ALREADY_RUNNING = App.get().getPackageName() + ".already_running";
    public static final String RUNNING_ILLEGAL = App.get().getPackageName() + ".running_illegal";


    public static final File appDir() {
        return new File(Environment.getExternalStorageDirectory(), App.get().getString(R.string.app_name));
    }


    public static final String logPath() {
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder builder = new StringBuilder();
        builder.append(format.format(new Date())).append(".pcap");
        File output = new File(appDir(), builder.toString());
        return output.getAbsolutePath();
    }

    public static String dumpParameter() {
        StringBuilder builder = new StringBuilder();
        builder.append(Configuration.BIN_PATH)
                .append(" -vv -s 0 ")
                .append(" -w ")
                .append(Configuration.logPath());
        return builder.toString();
    }

    public static boolean installed() {
        File bin = new File(BIN_PATH);
        File tool = new File(TOOL_PATH);
        File logDir = appDir();
        return bin.exists() && logDir.exists() && tool.exists();
    }

}
