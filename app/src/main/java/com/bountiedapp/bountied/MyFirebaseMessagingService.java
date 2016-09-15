package com.bountiedapp.bountied;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;


import com.bountiedapp.bountied.ui.BountiesFound;
import com.bountiedapp.bountied.ui.HomeActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*******************************************************************
 * MyFirebaseMessagingService allows the program to automatically
 * receive broadcast messages from Google's cloud messaging
 * service.  Based on the messages, we can activate notifications.
 *******************************************************************/

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // static strings used for intents to an activity
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_FOUNDARRAY = "EXTRA_FOUNDARRAY";

    private static final String TAG = "MyFirebaseMsgService";

    // used to keep the notifications from overwriting any
    // that came before, but from this same app
    private static int m_unique_number_id = 0;

    // if a message is received from Firebase then do the following
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // get the part of the message labeled "type"
        String type = remoteMessage.getData().get("type");

        // if a user accepted a bounty this user placed then do the following
        if (type.equals("accept")) {

            // get the lat and lng that were sent in the message
            String lat = remoteMessage.getData().get("lat");
            String lng = remoteMessage.getData().get("lng");

            // Calling method to generate notification
            sendNotificationAndMap(remoteMessage.getData().get("msg"), lat, lng);
        }
        // if a user placed a possible find for this user to look at, do the following
        else if (type.equals("found")) {

            // need to get all finds for a users bounty,
            // but need to get them in jsonarray format from a string
            JSONObject jsonObject;
            JSONArray jsonArray;
            ArrayList<String> finds = new ArrayList<String>();

            // convert the string to a JSON object
            // then a JSON object to a JSON Array
            try {
                jsonObject = new JSONObject(remoteMessage.getData().get("found"));
                jsonArray = jsonObject.getJSONArray("finds");
                for(int i = 0; i < jsonArray.length(); i++) {
                    finds.add('"' + jsonArray.get(i).toString() + '"');
                }
                // Calling method to generate notification
                sendFoundNotification(remoteMessage.getData().get("msg"), finds);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }
        // if a user choose the find this user placed on his/her bounty
        else if (type.equals("reward")) {
            // Calling method to generate notification
            sendNotification(remoteMessage.getData().get("msg"));
        }
        // if a user declined the find this user placed on his/her bounty
        else if (type.equals("decline")) {
            // Calling method to generate notification
            sendNotification(remoteMessage.getData().get("msg"));
        }
        // if a user deleted a bounty that this user placed a find on, notify them
        else if (type.equals("delete")) {
            // Calling method to generate notification
            sendNotification(remoteMessage.getData().get("msg"));
        }
    }

    // send the user a notification,
    // and if they click it open Google maps and map the found address
    private void sendNotificationAndMap(String messageBody, String lat, String lng) {

        // eventually need to check if google maps is installed and if it is not
        // take the user to a screen which displays their coordinates/address

        // create a map query for google maps
        String mapQuery = "geo:0,0?q=" + lat + "," + lng + "(" + "Your bounty was found at this location." + ")";

        Uri gmmIntentUri = Uri.parse(mapQuery);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");


        mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), mapIntent,
                PendingIntent.FLAG_ONE_SHOT);

        // set all notification stuff
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

        // increase this to keep notifications from erase eachother
        m_unique_number_id++;
        notificationManager.notify(m_unique_number_id, notificationBuilder.build());
    }


    // send the user a notification,
    // and if they click it open up the home activity of Bountied
    private void sendNotification(String messageBody) {

        // send user to the Home activity if they tap the notification
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_ONE_SHOT);

        // set all notification stuff
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

        // increase this to keep notifications from erase eachother
        m_unique_number_id++;
        notificationManager.notify(m_unique_number_id, notificationBuilder.build());

    }


    // send the user a notification,
    // and if they click it open up the bounties found activity of Bountied
    private void sendFoundNotification(String messageBody, ArrayList finds) {

        // send user to the BountiesFound activity to see the new finds on their placed bounty
        Intent intent = new Intent(this, BountiesFound.class);
        Bundle extras = new Bundle();

        // put the finds array in the bundle to pass via intent
        extras.putCharSequenceArrayList(EXTRA_FOUNDARRAY, finds);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(BUNDLE_EXTRAS, extras);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_ONE_SHOT);

        // set everything to do with notification
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

        // increase this to keep notifications from erase eachother
        m_unique_number_id++;
        notificationManager.notify(m_unique_number_id, notificationBuilder.build());

    }
}
