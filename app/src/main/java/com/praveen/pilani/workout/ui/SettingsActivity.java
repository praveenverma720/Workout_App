package com.praveen.pilani.workout.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.praveen.pilani.workout.R;

import timber.log.Timber;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    public static class SettingsFragment extends PreferenceFragment {

        private static final String TAG = SettingsFragment.class.getSimpleName();
        private Preference disconnectGoogleFitPref;
        private RingtonePreference notificationRingtonePref;
        private GoogleApiClient googleApiClient;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

            String keyNotificationRingtone = getString(R.string.pref_key_notification_ringtone);
            notificationRingtonePref = (RingtonePreference) findPreference(keyNotificationRingtone);
            disconnectGoogleFitPref = findPreference(getString(R.string.pref_key_disconnect_g_fit));


            updateRingtoneSummary(notificationRingtonePref, prefs.getString(keyNotificationRingtone, Settings.System.NOTIFICATION_SOUND));
        }

        @Override
        public void onStart() {
            super.onStart();
            notificationRingtonePref.setOnPreferenceChangeListener((preference, newValue) -> {
                updateRingtoneSummary(preference, (String) newValue);
                return true;
            });

            disconnectGoogleFitPref.setOnPreferenceClickListener(preference -> {
                disconnectGoogleFit();
                return true;
            });

        }

        @Override
        public void onStop() {
            super.onStop();
            notificationRingtonePref.setOnPreferenceChangeListener(null);
            disconnectGoogleFitPref.setOnPreferenceClickListener(null);
        }


        private void updateRingtoneSummary(Preference preference, String strUri) {
            String name;
            if (strUri.isEmpty()) {
                name = getString(R.string.pref_notification_ringtone_silent);
            } else {
                Uri ringtoneUri = Uri.parse(strUri);
                Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
                name = ringtone.getTitle(getActivity());
            }
            preference.setSummary(name);
        }


        private void disconnectGoogleFit() {
            if (googleApiClient != null && (googleApiClient.isConnecting() || googleApiClient.isConnected())) {
                // disconnect already in progress
                return;
            }
            Context context = getActivity().getApplicationContext();
            //noinspection CodeBlock2Expr,CodeBlock2Expr
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Fitness.CONFIG_API)
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    PendingResult<Status> result = Fitness.ConfigApi.disableFit(googleApiClient);
                                    result.setResultCallback(status -> {
                                        if (status.isSuccess()) {
                                            Toast.makeText(context, R.string.msg_fit_disconnect_success, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, R.string.msg_fit_disconnect_failure, Toast.LENGTH_SHORT).show();
                                        }
                                        googleApiClient.disconnect();
                                    });

                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    Timber.d("connection suspended");
                                }
                            }
                    )
                    .addOnConnectionFailedListener(
                            result -> {
                                Toast.makeText(context, R.string.msg_fit_disconnect_no_connection, Toast.LENGTH_SHORT).show();
                            }
                    )
                    .build();
            googleApiClient.connect();
        }
    }
}
