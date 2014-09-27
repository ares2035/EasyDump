package de.mingbo.easydump;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by shaomingbo on 14-9-25.
 */
public class MessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Configuration.INSTALL_COMPLETE.equals(action)) {
            onInstallComplete(context);
        } else if (Configuration.NO_PERMISSION.equals(action)) {
            onNoPermission(context);
        } else if (Configuration.DUMP_STARTED.equals(action)) {
            onDumpBegan(context);
        } else if (Configuration.DUMP_EXCEPTION.equals(action)) {
            onDumpException(context);
        } else if (Configuration.DUMP_STOPPED.equals(action)) {
            onDumpStopped(context);
        } else if (Configuration.DUMP_STOP_EXCEPTION.equals(action)) {
            onStopException(context);
        } else if (Configuration.NOT_INSTALLED.equals(action)) {
            onNotInstalled(context);
        } else if (Configuration.WINDOW_ADDED.equals(action) || Configuration.WINDOW_REMOVED.equals(action)) {
            onWindowChanged(context);
        } else if (Configuration.INSTALL_SUCCESS.equals(action)) {
            onInstallSuccess(context);
        } else if (Configuration.INSTALL_FAILED.equals(action)) {
            onInstallFailed(context);
        } else if (Configuration.MESSAGE_UPDATE.equals(action)) {
            onMessageUpdate(context, intent);
        } else if (Configuration.TITLE_UPDATE.equals(action)) {
            onTitleUpdate(context, intent);
        } else if (Configuration.ALREADY_RUNNING.equals(action)) {
            onAlreadyRunning(context);
        } else if (Configuration.RUNNING_ILLEGAL.equals(action)) {
            onRunningIllegal(context);
        }
    }

    private void onAlreadyRunning(Context context) {
        if (context instanceof IEvent) {
            ((IEvent) context).onAlreadyRun();
        }
    }

    private void onRunningIllegal(Context context) {
        if (context instanceof IEvent) {
            ((IEvent) context).onRunningIllegal();
        }
    }

    private void onTitleUpdate(Context context, Intent intent) {
        if (context instanceof MainActivity) {
            String msg = intent.getStringExtra("title");
            ((MainActivity) context).onTitleUpdate(msg);
        }
    }

    private void onMessageUpdate(Context context, Intent intent) {
        if (context instanceof MainActivity) {
            String msg = intent.getStringExtra("msg");
            ((MainActivity) context).onMessageUpdate(msg);
        }
    }

    private void onInstallSuccess(Context context) {
        if (context instanceof IEvent) {
            ((IEvent) context).onInstallSuccess();
        }
    }

    private void onInstallFailed(Context context) {
        if (context instanceof IEvent) {
            ((IEvent) context).onInstallFailed();
        }
    }

    private void onWindowChanged(Context context) {
        if (context instanceof MainActivity) {
            ((MainActivity) context).checkWindow();
        }
    }


    private void onNotInstalled(Context context) {
        if (context instanceof IEvent) {
            ((IEvent) context).onNotInstalled();
        }
    }

    private void onDumpStopped(Context context) {
        if (context instanceof IEvent) {
            ((IEvent) context).onDumpStopped();
        }
        App.get().started = false;
    }

    private void onStopException(Context context) {
        if (context instanceof IEvent) {
            ((IEvent) context).onStopException();
        }
    }


    private void onDumpBegan(Context context) {
        if (context instanceof IEvent) {
            ((IEvent) context).onDumpBegan();
        }
        App.get().started = true;
    }

    private void onDumpException(Context context) {
        if (context instanceof IEvent) {
            ((IEvent) context).onDumpException();
        }
        App.get().started = false;
    }

    private void onNoPermission(Context context) {
        if (context instanceof IEvent) {
            ((IEvent) context).onNoPermission();
        }
    }

    private void onInstallComplete(Context context) {
        if (context instanceof IEvent) {
            ((IEvent) context).onInstallComplete();
        }
    }
}
