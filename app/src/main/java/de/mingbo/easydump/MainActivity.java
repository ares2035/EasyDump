package de.mingbo.easydump;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity implements IEvent {


    public static final String EXTRA_NOT_INSTALLED = "not_installed";
    @InjectView(R.id.start)
    Button start;
    @InjectView(R.id.stop)
    Button stop;
    @InjectView(R.id.install)
    Button install;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.output)
    TextView output;
    @InjectView(R.id.window)
    Button window;


    MessageReceiver receiver = new MessageReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setups();
        linkCallbacks();
        App.get().created = true;

    }


    @Override
    protected void onResume() {
        super.onResume();
        checkInstallState();
        checkWindow();
        checkStarted();
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
    }

    private void checkStarted() {
        App.get().execute(new Runnable() {
            @Override
            public void run() {
                final boolean isRunning = Shell.isRunning();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        start.setEnabled(!isRunning);
                        stop.setEnabled(isRunning);
                    }
                });
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean flag = intent.getBooleanExtra(EXTRA_NOT_INSTALLED, false);
        if (flag)
            title.setText(R.string.title_not_installed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void linkCallbacks() {
        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                install.setEnabled(false);
                App.get().execute(new Runnable() {
                    @Override
                    public void run() {
                        Shell.install();
                    }
                });
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

        window.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(MainActivity.this, PopWindowService.class);

                if (Utils.isOpen()) {
                    stopService(service);
                } else {
                    if (Utils.firstTime()) {
                        if (Utils.isMIUI()) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.alert_dialog_title)
                                    .setMessage(R.string.info_miui_alert)
                                    .setPositiveButton(R.string.alert_dialog_ok_button, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Utils.appDetail();
                                        }
                                    })
                                    .show();
                            return;
                        }

                    }
                    home();
                    startService(service);
                }
            }
        });
    }


    private void setups() {
        ButterKnife.inject(this);
        start.setEnabled(Configuration.installed());
        stop.setEnabled(false);
    }

    private void home() {
        Intent action = new Intent(Intent.ACTION_MAIN);
        action.addCategory(Intent.CATEGORY_HOME);
        startActivity(action);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            home();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    void checkInstallState() {
        boolean installed = Configuration.installed();
        install.setEnabled(!installed);
        install.setText(installed
                ? R.string.main_activity_install_button_text_installed
                : R.string.main_activity_install_button_text_not_installed);
    }

    void checkWindow() {
        window.setText(Utils.isOpen()
                ? R.string.main_activity_window_button_unenable
                : R.string.main_activity_window_button_enable);
    }

    void onMessageUpdate(String message) {
        if (TextUtils.isEmpty(message))
            return;
        output.setText(message);
    }

    void onTitleUpdate(String message) {
        if (TextUtils.isEmpty(message))
            return;
        title.setText(message);
    }

    public void onNoPermission() {
        start.setEnabled(false);
        stop.setEnabled(false);
        checkInstallState();
    }

    public void onInstallComplete() {
        checkInstallState();
        start.setEnabled(Configuration.installed());
        stop.setEnabled(false);
    }

    public void onInstallSuccess() {
        Utils.toast(this, getString(R.string.success_install_bin_file));
        checkInstallState();
        start.setEnabled(Configuration.installed());
        stop.setEnabled(false);
        window.setEnabled(true);
    }

    public void onInstallFailed() {
        Utils.toast(this, getString(R.string.error_install_tcp_dump_failed));
        checkInstallState();
        start.setEnabled(false);
        stop.setEnabled(false);
        window.setEnabled(false);
    }

    public void onNotInstalled() {
        checkInstallState();
        start.setEnabled(false);
        stop.setEnabled(false);
        Utils.toast(this, getString(R.string.error_not_installed));
    }

    public void onDumpBegan() {
        start.setEnabled(false);
        stop.setEnabled(true);
        checkInstallState();
    }


    public void onDumpStopped() {
        start.setEnabled(true);
        stop.setEnabled(false);
        checkInstallState();
    }

    public void onDumpException() {
        title.setText(getString(R.string.error_dump_exception));
        stop.setEnabled(false);
        start.setEnabled(true);
        checkInstallState();
    }

    public void onStopException() {
        title.setText(getString(R.string.error_stop_dump_exception));
        start.setEnabled(true);
        stop.setEnabled(false);
        checkInstallState();
    }


    @Override
    public void onRunningIllegal() {
        checkInstallState();
        checkWindow();
        start.setEnabled(false);
        stop.setEnabled(true);
        Utils.toast(this, getString(R.string.running_illegal));
    }

    @Override
    public void onAlreadyRun() {
        checkInstallState();
        checkWindow();
        start.setEnabled(false);
        stop.setEnabled(true);
        Utils.toast(this, getString(R.string.already_run));
    }
}
