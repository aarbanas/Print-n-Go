package com.example.arbi.printngo;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Luka on 11.6.2017..
 */

public class NotificationService extends IntentService {

    private static final String TAG = "NotificationService";
    public String response = "";
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    HttpURLConnection conn;
    URL url = null;


    public NotificationService() {
        super("NotificationService");
    }

    @Override
    public void onHandleIntent(Intent intent) {

        while (response!="Gotovo") {


            SharedPreferences pref_print = this.getSharedPreferences("Login", 0);
            String user_id = pref_print.getString("id", null);


            try {

                // URL address where php file is
                url = new URL("http://207.154.235.97/files/checkPrintStatus.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // Log.e(TAG,e.toString());
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("user_id", user_id);
                String query = builder.build().getEncodedQuery();
                Log.i(TAG, user_id);
                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                //Log.e(TAG,e1.toString());
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    Log.i(TAG, "HTTP OK");
                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    Log.i(TAG, result.toString());
                    response = result.toString();


                } else {

                    System.out.print("Neuspjeh!");
                }

            } catch (IOException e) {
                e.printStackTrace();
                // Log.e(TAG,e.toString());
            } finally {
                conn.disconnect();
            }


            if (response.equals("Gotovo")) {

                 break;
            }

            SystemClock.sleep(7000);

        }

        Intent intentNotifikacija = new Intent();
        Notification noti = new Notification.Builder(this)
                .setTicker("Ticker title")
                .setContentTitle("Ispis je završen!")
                .setContentText("Cijena ispisa je 30kn u kopirnici Scripta Rijeka")
                .setSmallIcon(R.drawable.notifikacija_check)
                .setContentIntent(PendingIntent.getActivity(this, 0, intentNotifikacija, 0)).getNotification();
        noti.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0, noti);

        this.stopSelf();
    }

}
