package com.stevenhdesigns.wifidistancemeasureandroid.services;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileDataCollectorService extends BaseDataCollectorService {
    private String LOG_TAG = "FileDataCollectorService";
    private FileOutputStream localBackup = null;

    public void setup(Context context) {
        // Setup a local file just in case the phone never returns to the internet!
        // (or at least just in case the data never actually gets sent)
        try {
            File test = new File(context.getExternalFilesDir(null), "backup" + System.currentTimeMillis() + ".csv");
            localBackup = new FileOutputStream(test, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.w(LOG_TAG, "FileOutputStream exception: - " + e.toString());
        }

    }

    public void handle(String csi) {
        try {
            if (localBackup != null) {
                localBackup.write(csi.getBytes());
                localBackup.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
