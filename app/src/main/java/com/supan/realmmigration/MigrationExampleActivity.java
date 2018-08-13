package com.supan.realmmigration;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import com.supan.realmmigration.model.Migration;
import com.supan.realmmigration.model.Person;

/*
 ** This example demonstrates how you can migrate your data through different updates
 ** of your models.
 */
public class MigrationExampleActivity extends AppCompatActivity {

    public static final String TAG = MigrationExampleActivity.class.getSimpleName();

    private LinearLayout rootLayout = null;
    private Realm realm;

    private File EXPORT_REALM_PATH = new File(Environment.getExternalStorageDirectory(), "Alorsathi");
    private String EXPORT_REALM_FILE_NAME = "backup.realm";

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_migration_example);

        rootLayout = findViewById(R.id.container);
        rootLayout.removeAllViews();

        // 3 versions of the databases for testing. Normally you would only have one.
        copyBundledRealmFile(this.getResources().openRawResource(R.raw.default0), "default0.realm");
        //copyBundledRealmFile(this.getResources().openRawResource(R.raw.default1), "default1.realm");
        //copyBundledRealmFile(this.getResources().openRawResource(R.raw.default2), "default2.realm");

        // When you create a RealmConfiguration you can specify the version of the schema.
        // If the schema does not have that version a RealmMigrationNeededException will be thrown.
        RealmConfiguration config0 = new RealmConfiguration.Builder()
                .name("default0.realm")
                .schemaVersion(3)
                .build();

        // You can then manually call Realm.migrateRealm().
        try {
            Realm.migrateRealm(config0, new Migration());
        } catch (FileNotFoundException ignored) {
            // If the Realm file doesn't exist, just ignore.
        }
        realm = Realm.getInstance(config0);
        showStatus("Default0");
        showStatus(realm);
        //realm.close();

        // Or you can add the migration code to the configuration. This will run the migration code without throwing
        // a RealmMigrationNeededException.
        RealmConfiguration config1 = new RealmConfiguration.Builder()
                .name("default1.realm")
                .schemaVersion(3)
                .migration(new Migration())
                .build();

        realm = Realm.getInstance(config1); // Automatically run migration if needed
        showStatus("Default1");
        showStatus(realm);
        // realm.close();

        // or you can set .deleteRealmIfMigrationNeeded() if you don't want to bother with migrations.
        // WARNING: This will delete all data in the Realm though.
        RealmConfiguration config2 = new RealmConfiguration.Builder()
                .name("default2.realm")
                .schemaVersion(3)
                .deleteRealmIfMigrationNeeded()
                .build();

        realm = Realm.getInstance(config2);
        showStatus("Default2");
        showStatus(realm);
        realm.close();

        //checkStoragePermissions();
       /* if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission is granted");
            //File write logic here
            backup();
        }*/


        findViewById(R.id.backup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Backup clicked");
                Toast.makeText(MigrationExampleActivity.this, "Clicked", Toast.LENGTH_SHORT).show();

                if (isStoragePermissionGranted()) {
                    Log.i(TAG, "Granted and go to back up execute");
                    backup();
                } else {
                    Log.i(TAG, "Not Granted and nothing execute");
                }
            }
        });


    }

    private String copyBundledRealmFile(InputStream inputStream, String outFileName) {
        try {
            File file = new File(this.getFilesDir(), outFileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String realmString(Realm realm) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Person person : realm.where(Person.class).findAll()) {
            stringBuilder.append(person.toString()).append("\n");
        }

        return (stringBuilder.length() == 0) ? "<data was deleted>" : stringBuilder.toString();
    }

    private void showStatus(Realm realm) {
        showStatus(realmString(realm));
    }

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.addView(tv);
    }

    private void backup() {
        // First check if we have storage permissions

        File exportRealmFile = null;
        // final Realm realm = Realm.getDefaultInstance();

        try {
            EXPORT_REALM_PATH.mkdirs();

            // create a backup file
            exportRealmFile = new File(EXPORT_REALM_PATH, EXPORT_REALM_FILE_NAME);

            // if backup file already exists, delete it
            exportRealmFile.delete();

            // copy current realm to backup file
            realm.writeCopyTo(exportRealmFile);

        } catch (io.realm.internal.IOException e) {
            e.printStackTrace();
        }

        String msg = "File exported to Path: " + EXPORT_REALM_PATH + "/" + EXPORT_REALM_FILE_NAME;
        Log.i(TAG, "Backup  Path : " + exportRealmFile.getAbsolutePath());
        Log.i(TAG, "Backup  message : " + msg);

        realm.close();
    }

    private void checkStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }
}
