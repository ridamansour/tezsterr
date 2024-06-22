package org.secuso.privacyfriendlytodolist.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.JsonReader;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil;
import org.secuso.privacyfriendlybackup.api.backup.FileUtil;
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BackupRestorer implements IBackupRestorer {

    @Override
    public boolean restoreBackup(Context context, InputStream restoreData) {
        try {
            InputStreamReader isReader = new InputStreamReader(restoreData);
            JsonReader reader = new JsonReader(isReader);

            // START
            reader.beginObject();
            while (reader.hasNext()) {
                String type = reader.nextName();
                switch (type) {
                    case "database":
                    readDatabase(reader, context);
                    break;
                    case "preferences":
                    readPreferences(reader, context);
                    break;
                    default:
                    throw new RuntimeException("Cannot parse type " + type);
                }
            }
            reader.endObject();
            // END

            /*
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = restoreData.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            String resultString = result.toString("UTF-8");
            Log.d("PFA BackupRestorer", resultString);
            */
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void readDatabase(JsonReader reader, Context context) throws IOException {
        reader.beginObject();
        String n1 = reader.nextName();
        if (!"version".equals(n1)) {
            throw new RuntimeException("Unknown value " + n1);
        }
        int version = reader.nextInt();
        String n2 = reader.nextName();
        if (!"content".equals(n2)) {
            throw new RuntimeException("Unknown value " + n2);
        }
        SupportSQLiteOpenHelper helper = DatabaseUtil.getSupportSQLiteOpenHelper(context, "restoreDatabase", version);
        SupportSQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        db.setVersion(version);
        DatabaseUtil.readDatabaseContent(reader, db);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        reader.endObject();

        // Copy file to correct location
        File databaseFile = context.getDatabasePath("restoreDatabase");
        FileUtil.copyFile(databaseFile, context.getDatabasePath(DatabaseHelper.DATABASE_NAME));
        databaseFile.delete();
    }

    private void readPreferences(JsonReader reader, Context context) throws IOException {
        reader.beginObject();
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "pref_pin_enabled":
                case "notify":
                case "pref_progress":
                pref.putBoolean(name, reader.nextBoolean());
                break;
                case "pref_pin":
                pref.putString(name, reader.nextString()); // TODO maybe leave this out
                break;
                case "pref_default_reminder_time":
                pref.putString(name, reader.nextString());
                break;
                default:
                throw new RuntimeException("Unknown preference " + name);
            }
        }
        pref.commit();
        reader.endObject();
    }
}
