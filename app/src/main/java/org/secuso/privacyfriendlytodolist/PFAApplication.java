package org.secuso.privacyfriendlytodolist;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import org.secuso.privacyfriendlybackup.api.pfa.BackupManager;
import org.secuso.privacyfriendlytodolist.backup.BackupCreator;
import org.secuso.privacyfriendlytodolist.backup.BackupRestorer;

public class PFAApplication extends Application implements Configuration.Provider {

    @Override
    public void onCreate() {
        super.onCreate();
        BackupManager.setBackupCreator(new BackupCreator());
        BackupManager.setBackupRestorer(new BackupRestorer());
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build();
    }
}
