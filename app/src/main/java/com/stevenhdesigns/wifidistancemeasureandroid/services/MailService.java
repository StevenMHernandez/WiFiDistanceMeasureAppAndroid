package com.stevenhdesigns.wifidistancemeasureandroid.services;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MailService {
    public void setup(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // pass
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
            }
        }
    }

    public void sendMail(Activity activity, ArrayList<String> dataList) {
        StringBuilder x = new StringBuilder();
        for (String s : dataList) {
            x.append(s).append("\n");
        }
        File fileToSend = createFileWithContent(activity, x.toString());

        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_SUBJECT, "WiFiDistanceMeasureApp Experiments");
        email.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fileToSend.getAbsoluteFile()));
        email.setType("message/rfc822");

        activity.startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }

    private File createFileWithContent(Activity activity, String content) {
        File file = null;

        try {
            String deviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
            String uniqueName = String.valueOf(System.currentTimeMillis());
            String filename = deviceId + "." + uniqueName + ".csv";
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            Toast.makeText(activity.getBaseContext(), "Unable create temp file.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return file;
    }
}
