package de.mingbo.easydump;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by shaomingbo on 14-9-25.
 */
public class Utils {

    public static void toast(final Activity activity, final String msg) {
        if (activity == null || activity.isFinishing())
            return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void d(String msg) {
        if (BuildConfig.DEBUG)
            Log.d(Configuration.TAG, msg);
    }


    public static boolean isOpen() {
        ActivityManager manager = (ActivityManager) App.get().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo info : services) {
            if (info.service.getClassName().equals(PopWindowService.class.getName()))
                return true;
        }
        return false;
    }

    public static boolean firstTime() {
        boolean flag = App.get().getSharedPreferences().getBoolean("first", true);
        App.get().getSharedPreferences().edit().putBoolean("first", false).apply();
        return flag;
    }

    public static boolean isMIUI() {
        return Build.MANUFACTURER.toLowerCase().contains("xiaomi");
    }

    public static void appDetail() {
        Intent action = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        action.setData(Uri.fromParts("package", App.get().getPackageName(), null));
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.get().startActivity(action);
    }


}
