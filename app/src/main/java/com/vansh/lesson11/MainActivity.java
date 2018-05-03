package com.vansh.lesson11;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String DOWNLOAD_URL = "https://upload.wikimedia.org/wikipedia/en/a/aa/Bart_Simpson_200px.png";
    ProgressBar progressBar;
    TextView tv;
    Button btnNotification;
    NotificationCompat.Builder notification;
    private static final int uniqueNotificationID = 112233;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tv = (TextView) findViewById(R.id.textView2);

        progressBar.setVisibility(View.INVISIBLE);
        Button button = (Button) findViewById(R.id.btn_download);

        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                tv.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                if(isPermissionGranted())
                    new DownloadFile().execute(DOWNLOAD_URL);
                else
                    requestPermission();
            }
        });


        btnNotification = (Button) findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnNotification) {

                    notification.setSmallIcon(R.drawable.logo);

                    notification.setTicker("Re:Driver - The quest have been updated!");   //For use with Android versions before Lollipop/5.0
                    notification.setContentTitle("Quest Updated!");
                    notification.setContentText("'Websec, Protector of Sites' has been slain! Collect your rewards!");
                    notification.setWhen(System.currentTimeMillis());                     //Time which shows in the notification
                    notification.setDefaults(Notification.DEFAULT_ALL);                   //Default sound, vibrate pattern, etc.
                    notification.setPriority(NotificationCompat.PRIORITY_HIGH);           //PRIORITY_HIGH pops up a message, vibrates, sound, etc. LOW doesn't pop up a message

                    Intent intent = new Intent(MainActivity.this, MainActivity.class);

                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    notification.setContentIntent(pendingIntent);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                    notificationManager.notify(uniqueNotificationID, notification.build());
                }
            }
        });


        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

    }



    private boolean isPermissionGranted()
    {
      return   ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
    }

    private class DownloadFile extends AsyncTask<String, Integer, String> {
        ProgressDialog mProgressDialog;
        private final int MAX_INT =100;
        private final String FILE_NAME ="lesson_11_task.jpg";
        String filepath = Environment.getExternalStorageDirectory().getPath();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setMax(MAX_INT);
        }

        @Override
        protected String doInBackground(String... Url) {
            try {
                URL url = new URL(Url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream());

                OutputStream output = new FileOutputStream(filepath + "/" + FILE_NAME);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // Publish the progress
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                // Close connection
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                // Error Log
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressBar.setProgress(progress[0]);

            Log.e("Progress",progress[0].toString());
            if(progress[0]==MAX_INT)
            {
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView=inflater.inflate(R.layout.alert_layout, null);
                ImageView imageView = (ImageView)dialogView.findViewById(R.id.imageView);
                File file = new File(filepath + "/" + FILE_NAME);
                Glide.with(MainActivity.this).load(file).into(imageView);
                progressBar.setVisibility(View.INVISIBLE);
                tv.setVisibility(View.VISIBLE);
                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Download Complete!")
                        .setMessage("Image Downloaded at:" + filepath + "/" + FILE_NAME)
                        .setView(dialogView).create();
                alertDialog.show();
                noti();
                notification = new NotificationCompat.Builder(getApplicationContext());
                notification.setAutoCancel(true);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==2 && isPermissionGranted())
        {
            new DownloadFile().execute(DOWNLOAD_URL);
        }
        else
        {

            boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (! showRationale) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Permission Not Granted")
                        .setMessage("Grant Permissions from settings")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }
            else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Permission Not Granted")
                        .setMessage("Grant Permission to continue")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermission();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                Toast.makeText(getApplicationContext(), "Closing App", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        }
    }

    void noti(){
        notification.setSmallIcon(R.drawable.logo);

        notification.setTicker("Task Completed");   //For use with Android versions before Lollipop/5.0
        notification.setContentTitle("Image Downloaded!");
        notification.setContentText("Udacity Scholarship Program is amazing...");
        notification.setWhen(System.currentTimeMillis());                     //Time which shows in the notification
        notification.setDefaults(Notification.DEFAULT_ALL);                   //Default sound, vibrate pattern, etc.
        notification.setPriority(NotificationCompat.PRIORITY_HIGH);           //PRIORITY_HIGH pops up a message, vibrates, sound, etc. LOW doesn't pop up a message

        Intent intent = new Intent(MainActivity.this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(uniqueNotificationID, notification.build());
    }
}
