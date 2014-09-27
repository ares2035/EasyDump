package de.mingbo.easydump;

/**
 * Created by shaomingbo on 14-9-26.
 */
public interface IEvent {

    void onInstallSuccess();

    void onInstallFailed();

    void onNotInstalled();

    void onInstallComplete();

    void onDumpStopped();

    void onStopException();

    void onDumpBegan();

    void onDumpException();

    void onNoPermission();

    void onAlreadyRun();

    void onRunningIllegal();


}
