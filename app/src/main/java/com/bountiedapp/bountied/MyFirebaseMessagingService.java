package com.bountiedapp.bountied;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;


import com.bountiedapp.bountied.ui.BountiesFound;
import com.bountiedapp.bountied.ui.HomeActivity;
import com.bountiedapp.bountied.ui.HuntsInProgress;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mprovost on 7/25/2016.
 */



public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static int unique_number_id = 0;

    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_FOUNDARRAY = "EXTRA_FOUNDARRAY";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData().get("msg"));

        String type = remoteMessage.getData().get("type");
        if (type.equals("accept")) {
            //Calling method to generate notification
            String lat = remoteMessage.getData().get("lat");
            String lng = remoteMessage.getData().get("lng");
            sendNotificationAndMap(remoteMessage.getData().get("msg"), lat, lng);
        }
        else if (type.equals("found")) {
            //Calling method to generate notification
            JSONObject jsonObject;
            JSONArray jsonArray;
            ArrayList<String> finds = new ArrayList<String>();
            try {
                jsonObject = new JSONObject(remoteMessage.getData().get("found"));
                jsonArray = jsonObject.getJSONArray("finds");
                for(int i = 0; i < jsonArray.length(); i++) {
                    finds.add('"' + jsonArray.get(i).toString() + '"');
                }
                sendFoundNotification(remoteMessage.getData().get("msg"), finds);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else if (type.equals("reward")) {
            //Calling method to generate notification
            sendNotification(remoteMessage.getData().get("msg"));
        }
        else if (type.equals("decline")) {
            //Calling method to generate notification
            sendNotification(remoteMessage.getData().get("msg"));
        }
        else if (type.equals("delete")) {
            //Calling method to generate notification
            sendNotification(remoteMessage.getData().get("msg"));
        }
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotificationAndMap(String messageBody, String lat, String lng) {
        lat = "41.081932";
        lng = "-74.175816";

        // for now try and switch to google maps and display san fransisco
        String mapQuery = "geo:0,0?q=" + lat + "," + lng + "(" + "Your bounty was found at this location." + ")";

        Uri gmmIntentUri = Uri.parse(mapQuery);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
//        if (mapIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(mapIntent);
//        }

//        Intent intent = new Intent(this, HuntsInProgress.class);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), mapIntent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Bountied Notification")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        unique_number_id++;
        notificationManager.notify(unique_number_id, notificationBuilder.build());
    }


    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {


        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Bountied Notification")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        unique_number_id++;
        notificationManager.notify(unique_number_id, notificationBuilder.build());

    }


    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendFoundNotification(String messageBody, ArrayList finds) {


        Intent intent = new Intent(this, BountiesFound.class);
        Bundle extras = new Bundle();
        System.out.println(finds);
        extras.putCharSequenceArrayList(EXTRA_FOUNDARRAY, finds);
        //intent.putExtra()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(BUNDLE_EXTRAS, extras);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Bountied Notification")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        unique_number_id++;
        notificationManager.notify(unique_number_id, notificationBuilder.build());

    }
}
