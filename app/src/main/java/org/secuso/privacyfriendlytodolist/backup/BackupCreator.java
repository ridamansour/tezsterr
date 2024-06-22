package org.secuso.privacyfriendlytodolist.backup;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.JsonWriter;
import android.util.Log;

import androidx.sqlite.db.SupportSQLiteDatabase;

import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil;
import org.secuso.privacyfriendlybackup.api.backup.PreferenceUtil;
import org.secuso.privacyfriendlybackup.api.pfa.IBackupCreator;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;
import org.secuso.privacyfriendlytodolist.util.PinUtil;
import org.secuso.privacyfriendlytodolist.view.PinActivity;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BackupCreator implements IBackupCreator {

    @Override
    public boolean writeBackup(Context context, OutputStream outputStream) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> pinCheckTask = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (PinUtil.hasPin(context)) {
                    // check if a pin is set and validate it first
                    PinActivity.result = null;
                    Intent intent = new Intent(context, PinActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);

                    while (PinActivity.result == null) {
                        Thread.sleep(200);
                    }

                    return PinActivity.result;
                } else {
                    return true;
                }
            }
        };

        Future<Boolean> pinCheckFuture = executor.submit(pinCheckTask);
        boolean pinCheckResult;
        try {
            pinCheckResult = pinCheckFuture.get();
        } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        executor.shutdown();
        return false;
    }

        executor.shutdown();

        if (pinCheckResult) {
            return writeBackupInternal(context, outputStream);
        } else {
            return false;
        }
    }

    private boolean writeBackupInternal(Context context, OutputStream outputStream) {
        OutputStreamWriter outputStreamWriter = null;
        JsonWriter writer = null;

        try {
            outputStreamWriter = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
            writer = new JsonWriter(outputStreamWriter);
            writer.setIndent("");

            writer.beginObject();
            SupportSQLiteDatabase dataBase = DatabaseUtil.getSupportSQLiteOpenHelper(context, DatabaseHelper.DATABASE_NAME).getReadableDatabase();
            writer.name("database");
            DatabaseUtil.writeDatabase(writer, dataBase);
            dataBase.close();

            writer.name("preferences");
            android.content.SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            PreferenceUtil.writePreferences(writer, pref, new String[]{"pref_pin"});

            writer.endObject();
            writer.close();
            return true;
        } catch (Exception e) {
            Log.e("PFA BackupCreator", "Error occurred", e);
            e.printStackTrace();
            return false;
        }
    }
}
