package android.example.com.squawker.fcm;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;

public class SquawkerFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "SquawkerFirebaseMS";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d(TAG, "onNewToken: token:"+s);
    }
}
