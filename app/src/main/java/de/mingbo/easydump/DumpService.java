package de.mingbo.easydump;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

public class DumpService extends Service {

    private static final int CODE = 1987;
    public static final String EXTRA_KEY = "dump";
    public static final String ACTION_DUMP_BEGIN = "begin";
    public static final String ACTION_DUMP_STOP = "stop";


    public DumpService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String msg = intent.getStringExtra(EXTRA_KEY);
        if (!TextUtils.isEmpty(msg)) {
            if (msg.equals(ACTION_DUMP_BEGIN))
                dumpBegin();
            else if (msg.equals(ACTION_DUMP_STOP))
                dumpStop();
        }

        return Service.START_FLAG_REDELIVERY;
    }

    void dumpBegin() {
        notifyUser();
        App.get().execute(new Runnable() {
            @Override
            public void run() {
                boolean isRunning = Shell.isRunning();
                if (isRunning && App.get().started) {
                    App.get().sendBroadcast(new Intent(Configuration.ALREADY_RUNNING));
                    return;
                }

                if (isRunning && !App.get().started) {
                    App.get().sendBroadcast(new Intent(Configuration.RUNNING_ILLEGAL));
                    return;
                }

                Shell.dumpBegin();
            }
        });
    }

    void dumpStop() {
        stopForeground(true);
        App.get().execute(new Runnable() {
            @Override
            public void run() {
                Shell.dumpStop();
            }
        });
    }

    private void notifyUser() {
        Intent action = new Intent(this, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        action.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(this, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification notification = builder.setAutoCancel(false)
                .setContentIntent(intent)
                .setContentText(getString(R.string.dump_service_running))
                .setContentTitle(getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        startForeground(CODE, notification);
    }

}
