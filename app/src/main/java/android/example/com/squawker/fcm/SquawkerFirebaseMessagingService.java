package android.example.com.squawker.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SquawkerFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "SquawkerFirebaseMS";

    private final int NOTIFICATION_MAX_CHARACTERS=30;
    private static final String JSON_KEY_AUTHOR = SquawkContract.COLUMN_AUTHOR;
    private static final String JSON_KEY_AUTHOR_KEY = SquawkContract.COLUMN_AUTHOR_KEY;
    private static final String JSON_KEY_MESSAGE = SquawkContract.COLUMN_MESSAGE;
    private static final String JSON_KEY_DATE = SquawkContract.COLUMN_DATE;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String,String> data = remoteMessage.getData();

        if (data.size()>0){
            Log.d(TAG, "onMessageReceived: " + remoteMessage.getData());
            sendNotification(data);
            insertSquawk(data);
            dumpTable(SquawkProvider.SquawkMessages.CONTENT_URI);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d(TAG, "onNewToken: token:"+s);
    }

    private void insertSquawk(final Map<String, String> data) {

        // Database operations should not be done on the main thread
        AsyncTask<Void, Void, Void> insertSquawkTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues newMessage = new ContentValues();
                newMessage.put(SquawkContract.COLUMN_AUTHOR, data.get(JSON_KEY_AUTHOR));
                newMessage.put(SquawkContract.COLUMN_MESSAGE, data.get(JSON_KEY_MESSAGE).trim());
                newMessage.put(SquawkContract.COLUMN_DATE, data.get(JSON_KEY_DATE));
                newMessage.put(SquawkContract.COLUMN_AUTHOR_KEY, data.get(JSON_KEY_AUTHOR_KEY));
                getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, newMessage);

                //BvS log the full database table
                Cursor cursor = getContentResolver().query(SquawkProvider.SquawkMessages.CONTENT_URI,null,null,null,null);
                while (cursor.moveToNext()){
                    List<String> row = new LinkedList<>();
                    for (int i=0;i<cursor.getColumnCount();i++){
                        row.add(cursor.getColumnName(i)+"=" + cursor.getString(i));
                    }
                    Log.d(TAG, android.text.TextUtils.join(",", row));
                }

                return null;
            }

        };

        insertSquawkTask.execute();
    }

    void dumpTable(Uri uri){
        //BvS log the full database table
        //Ex. URI SquawkProvider.SquawkMessages.CONTENT_URI
        Cursor cursor = getContentResolver().query(uri,null,null,null,null);
        while (cursor.moveToNext()){
            List<String> row = new LinkedList<>();
            for (int i=0;i<cursor.getColumnCount();i++){
                row.add(cursor.getColumnName(i)+"=" + cursor.getString(i));
            }
            Log.d("dumpTable", android.text.TextUtils.join(",", row));
        }
    }





    //BvS Based on version from https://github.com/firebase/quickstart-android/blob/d886d348a681f41f02e78d720cb74fb8c162e339/messaging/app/src/main/java/com/google/firebase/quickstart/fcm/java/MyFirebaseMessagingService.java#L58-L101
    // in which now a Notification channel is used (not in the org version)
    // Adaptations:
    // - In the sample a String as argument, in the exercise a Data Map
    // - add to string resource <string name="default_notification_channel_id">fcm_default_channel</string>

    private void sendNotification(Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String author = data.get(JSON_KEY_AUTHOR);
        String message = data.get(JSON_KEY_MESSAGE);

        // If the message is longer than the max number of characters we want in our
        // notification, truncate it and add the unicode character for ellipsis
        if (message.length() > NOTIFICATION_MAX_CHARACTERS) {
            message = message.substring(0, NOTIFICATION_MAX_CHARACTERS) + "\u2026";
        }

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_duck)
                        //.setSmallIcon(R.drawable.ic_stat_ic_notification)
                        //.setContentTitle(getString(R.string.fcm_message))
                        .setContentTitle(String.format(getString(R.string.notification_message), author))
                        //.setContentText(messageBody)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
