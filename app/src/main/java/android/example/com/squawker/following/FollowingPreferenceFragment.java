/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package android.example.com.squawker.following;

import android.content.SharedPreferences;
import android.example.com.squawker.R;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


/**
 * Shows the list of instructors you can follow
 */
//TODO (1) Implement onSharedPreferenceChangeListener
public class FollowingPreferenceFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String LOG_TAG = FollowingPreferenceFragment.class.getSimpleName();

    private String msg;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // TODO (2) When a SharedPreference changes, check which preference it is and subscribe or
        // un-subscribe to the correct topics.
        // Ex. FirebaseMessaging.getInstance().subscribeToTopic("key_lyla");
        // subscribes to Lyla's squawks.


        // Add visualizer preferences, defined in the XML file in res->xml->preferences_squawker
        addPreferencesFromResource(R.xml.following_squawker);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = getPreferenceManager().findPreference(key);

        if (pref!=null) {
            boolean bIsOn = sharedPreferences.getBoolean(key,false);

            if (bIsOn){
                FirebaseMessaging.getInstance().subscribeToTopic(key).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        msg="Subscription succeeded";
                        if (!task.isSuccessful()){
                            msg="Subscription failed";
                        }
                    }
                });
            }
            else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(key).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        msg="Subscription ended";
                        if (!task.isSuccessful()){
                            msg="Unsubscription failed";
                        }

                    }
                });
            }

            Log.d(LOG_TAG, "onSharedPreferenceChanged: "+msg);
            Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
        }

        {
            FirebaseMessaging.getInstance().subscribeToTopic(key).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    String msg="Subscription succeeded";
                    if (!task.isSuccessful()){
                        msg="Subscription failed";
                    }
                    Log.d(LOG_TAG, "onComplete: "+msg);
                    Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    // TODO (3) Make sure to register and unregister this as a Shared Preference Change listener, in
    // onCreate and onDestroy.


    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
