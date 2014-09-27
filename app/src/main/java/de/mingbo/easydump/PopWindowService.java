package de.mingbo.easydump;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import butterknife.ButterKnife;

public class PopWindowService extends Service implements IEvent {
    public PopWindowService() {
    }

    WindowManager manager;
    View root;
    Button start, stop, resume, hide;
    MessageReceiver receiver = new MessageReceiver();
    Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        setup();
        linkCallbacks();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Configuration.INSTALL_COMPLETE);
        filter.addAction(Configuration.INSTALL_SUCCESS);
        filter.addAction(Configuration.INSTALL_FAILED);
        filter.addAction(Configuration.NOT_INSTALLED);

        filter.addAction(Configuration.NO_PERMISSION);

        filter.addAction(Configuration.DUMP_EXCEPTION);
        filter.addAction(Configuration.DUMP_STARTED);
        filter.addAction(Configuration.DUMP_STOPPED);
        filter.addAction(Configuration.DUMP_STOP_EXCEPTION);

        filter.addAction(Configuration.WINDOW_ADDED);
        filter.addAction(Configuration.WINDOW_REMOVED);

        filter.addAction(Configuration.MESSAGE_UPDATE);
        filter.addAction(Configuration.TITLE_UPDATE);

        filter.addAction(Configuration.ALREADY_RUNNING);
        filter.addAction(Configuration.RUNNING_ILLEGAL);

        registerReceiver(receiver, filter);

        handler = new Handler();
    }

    private void linkCallbacks() {
        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });

        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent action = new Intent(PopWindowService.this, MainActivity.class);
                action.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                action.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(action);
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setEnabled(false);
                App.get().start();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop.setEnabled(false);
                App.get().stop();
            }
        });
    }

    private void setup() {
        manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        root = inflater.inflate(R.layout.popup_window, null);
        start = ButterKnife.findById(root, R.id.start);
        stop = ButterKnife.findById(root, R.id.stop);
        resume = ButterKnife.findById(root, R.id.resume);
        hide = ButterKnife.findById(root, R.id.hide);
    }

    private WindowManager.LayoutParams getParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        return params;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void checkStarted() {
        App.get().execute(new Runnable() {
            @Override
            public void run() {
                final boolean isRunning = Shell.isRunning();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        start.setEnabled(!isRunning);
                        stop.setEnabled(isRunning);
                    }
                }, 0);
            }
        });
    }

    private void remove() {
        manager.removeView(root);
        sendBroadcast(new Intent(Configuration.WINDOW_REMOVED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        remove();
        unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager.addView(root, getParams());
        sendBroadcast(new Intent(Configuration.WINDOW_ADDED));
        checkStarted();
        return Service.START_STICKY;
    }


    @Override
    public void onInstallSuccess() {
        start.setEnabled(Configuration.installed());
        stop.setEnabled(false);
    }

    @Override
    public void onInstallFailed() {
        start.setEnabled(false);
        stop.setEnabled(false);
    }

    @Override
    public void onNotInstalled() {
        start.setEnabled(false);
        stop.setEnabled(false);

        Intent action = new Intent(this, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        action.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        action.putExtra(MainActivity.EXTRA_NOT_INSTALLED, true);
        startActivity(action);
    }

    @Override
    public void onInstallComplete() {
        start.setEnabled(Configuration.installed());
        stop.setEnabled(false);
    }

    @Override
    public void onDumpStopped() {
        start.setEnabled(true);
        stop.setEnabled(false);
    }

    @Override
    public void onStopException() {
        stop.setEnabled(false);
        start.setEnabled(true);
    }

    @Override
    public void onDumpBegan() {
        start.setEnabled(false);
        stop.setEnabled(true);
    }

    @Override
    public void onDumpException() {
        stop.setEnabled(false);
        start.setEnabled(true);
    }

    @Override
    public void onNoPermission() {
        start.setEnabled(false);
        stop.setEnabled(false);
    }

    @Override
    public void onRunningIllegal() {
        start.setEnabled(false);
        stop.setEnabled(true);
    }

    @Override
    public void onAlreadyRun() {
        start.setEnabled(false);
        stop.setEnabled(true);
    }

}
