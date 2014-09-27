package de.mingbo.easydump;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shaomingbo on 14-9-25.
 */
public class App extends Application {

    static App application;

    ExecutorService executors;

    public volatile boolean started;
    public volatile boolean created;

    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        executors = Executors.newCachedThreadPool();
        started = false;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static App get() {
        return application;
    }

    public void execute(Runnable run) {
        executors.execute(run);
    }

    public void start() {
        Intent service = new Intent(this, DumpService.class);
        service.putExtra(DumpService.EXTRA_KEY, DumpService.ACTION_DUMP_BEGIN);
        startService(service);
    }

    public void stop() {
        Intent service = new Intent(this, DumpService.class);
        service.putExtra(DumpService.EXTRA_KEY, DumpService.ACTION_DUMP_STOP);
        startService(service);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }


}
