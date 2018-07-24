package com.greenaddress.greenbits.ui.preferences;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Joiner;
import com.greenaddress.greenapi.Network;
import com.greenaddress.greenbits.ui.R;
import com.greenaddress.greenbits.ui.UI;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NetworkPreferenceFragment extends GAPreferenceFragment {
    private static final String TAG = GAPreferenceFragment.class.getSimpleName();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mService == null) {
            Log.d(TAG, "Avoiding create on null service");
            return;
        }

        addPreferencesFromResource(R.xml.pref_network);
        setHasOptionsMenu(true);

        final Preference host = find("proxy_host");
        host.setOnPreferenceChangeListener(mListener);
        host.setSummary(mService.getProxyHost());
        final Preference port = find("proxy_port");
        port.setSummary(mService.getProxyPort());
        port.setOnPreferenceChangeListener(mListener);
        final Preference torEnabled  = find("tor_enabled");
        if (Network.GAIT_ONION == null)
            torEnabled.setEnabled(false);
        else {
            torEnabled.setSummary(getString(R.string.torSummary, Network.GAIT_ONION));
            torEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, final Object o) {
                    if (mService != null)
                        mService.disconnect(true);
                    return true;
                }
            });
        }

        final Set<String> customNetworks = mService.cfg().getStringSet("custom_networks", new HashSet<>());

        // Network selector
        final MultiSelectListPreference networkSelector = find("network_selector");
        networkSelector.setOnPreferenceChangeListener((preference, selectedPreferencesObject) -> {
            final Set<String> selectedPreferences = (Set<String>) selectedPreferencesObject;
            if (selectedPreferences.isEmpty()) {
                UI.toast(getActivity(), "Cannot select any network. selecting Bitcoin by default", Toast.LENGTH_LONG);
                selectedPreferences.add("Bitcoin");
                mService.cfg().edit().putStringSet("network_selector", selectedPreferences).apply();
            }
            networkSelector.setSummary( Joiner.on(", ").join(selectedPreferences) );
            return true;
        });
        final Set<String> selectedPreferences = mService.cfg().getStringSet("network_selector", new HashSet<>());
        networkSelector.setSummary( Joiner.on(", ").join(selectedPreferences) );

        final ListPreference networkCustomRemove = find("custom_networks_remove");
        final String[] entries = customNetworks.toArray(new String[customNetworks.size()]);
        networkCustomRemove.setEntries(entries);
        networkCustomRemove.setEntryValues(entries);
        networkCustomRemove.setOnPreferenceChangeListener((preference, newValue) -> {
            final Set<String> customNetworksNow = mService.cfg().getStringSet("custom_networks", new HashSet<>());
            Log.w(TAG, "customNetworks1: " + customNetworksNow);
            customNetworksNow.remove(newValue);
            Log.w(TAG, "customNetworks2: " + customNetworksNow);
            syncCustomNetworks(networkCustomRemove, networkSelector, customNetworksNow);

            return false;
        });

        final EditTextPreference networkCustomAdd = find("custom_networks_add");
        networkCustomAdd.setOnPreferenceChangeListener((preference, newValue) -> {
            //TODO check newValue is URL
            final Set<String> customNetworksNow = mService.cfg().getStringSet("custom_networks", new HashSet<>());
            customNetworksNow.add(newValue.toString());
            Log.w(TAG, "customNetworks: " + customNetworksNow);
            syncCustomNetworks(networkCustomRemove, networkSelector, customNetworksNow);

            return false;
        });

        syncCustomNetworks(networkCustomRemove, networkSelector, customNetworks);

    }

    private void syncCustomNetworks(ListPreference networkCustomRemove, MultiSelectListPreference networkSelector, Set<String> customNetworksNow) {
        mService.cfg().edit()
                .putStringSet("custom_networks", customNetworksNow)
                .apply();
        final String[] entriesNow = customNetworksNow.toArray(new String[customNetworksNow.size()]);
        networkCustomRemove.setEntries(entriesNow);
        networkCustomRemove.setEntryValues(entriesNow);

        final String[] standardNetworks = getResources().getStringArray(R.array.available_networks);
        final Set<String> standardAndCustomNetworks = new HashSet<>(customNetworksNow);
        standardAndCustomNetworks.addAll( Arrays.asList(standardNetworks) );

        final String[] standardAndCustomNetworksArray = standardAndCustomNetworks.toArray(new String[standardAndCustomNetworks.size()]);
        networkSelector.setEntries(standardAndCustomNetworksArray);
        networkSelector.setEntryValues(standardAndCustomNetworksArray);
    }

    private final Preference.OnPreferenceChangeListener mListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(final Preference preference, final Object o) {
            preference.setSummary(o.toString());
            if (mService != null)
                mService.disconnect(true);
            return true;
        }
    };
}
