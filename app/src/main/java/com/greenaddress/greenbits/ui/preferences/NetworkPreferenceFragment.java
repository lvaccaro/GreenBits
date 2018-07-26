package com.greenaddress.greenbits.ui.preferences;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.google.common.base.Joiner;
import com.greenaddress.greenapi.Network;
import com.greenaddress.greenapi.Network2;
import com.greenaddress.greenbits.ui.R;
import com.greenaddress.greenbits.ui.UI;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
            final Set<String> customNetworksNew = mService.cfg().getStringSet("custom_networks", new HashSet<>());
            customNetworksNew.remove(newValue);
            final Set<String> networkSelectorPreferences = mService.cfg().getStringSet("network_selector", new HashSet<>());
            if (networkSelectorPreferences.contains(newValue)) {
                networkSelectorPreferences.remove(newValue);
                mService.cfg().edit()
                        .putStringSet("network_selector", networkSelectorPreferences)
                        .apply();
                networkSelector.setSummary( Joiner.on(", ").join(networkSelectorPreferences) );

            }
            syncCustomNetworks(networkCustomRemove, networkSelector, customNetworksNew);

            return true;
        });

        final EditTextPreference networkCustomAdd = find("custom_networks_add");
        networkCustomAdd.setOnPreferenceChangeListener((preference, newValue) -> {
            if (URLUtil.isValidUrl(newValue.toString())) {
                final Set<String> customNetworksNew = mService.cfg().getStringSet("custom_networks", new HashSet<>());
                if (customNetworksNew.contains(newValue.toString())) {
                    UI.toast(getActivity(), "Custom URL already present", Toast.LENGTH_LONG);
                } else {
                    customNetworksNew.add(newValue.toString());
                    syncCustomNetworks(networkCustomRemove, networkSelector, customNetworksNew);
                    UI.toast(getActivity(), "Custom URL added, enable it in the Network selector chooser", Toast.LENGTH_LONG);
                }
            } else {
                UI.toast(getActivity(), "Not a valid URL", Toast.LENGTH_LONG);
            }

            return true;
        });


        /*
        TODO remove
         */
        String json = "{\n" +
                "  \"name\": \"Bitcoin\",\n" +
                "  \"network\": \"mainnet\",\n" +
                "  \"liquid\": false,\n" +
                "  \"gait_wamp_url\": \"wss://prodwss.greenaddress.it/v2/ws/\",\n" +
                "  \"gait_wamp_cert_pins\": [\n" +
                "    \"25:84:7D:66:8E:B4:F0:4F:DD:40:B1:2B:6B:07:40:C5:67:DA:7D:02:43:08:EB:6C:2C:96:FE:41:D9:DE:21:8D\",\n" +
                "    \"A7:4B:0C:32:B6:5B:95:FE:2C:4F:8F:09:89:47:A6:8B:69:50:33:BE:D0:B5:1D:D8:B9:84:EC:AE:89:57:1B:B6\"\n" +
                "  ],\n" +
                "  \"blockexplorers\": [{\n" +
                "    \"address\": \"https://sandbox.smartbit.com.au/address/\",\n" +
                "    \"tx\": \"https://sandbox.smartbit.com.au/tx/\"\n" +
                "  }],\n" +
                "  \"deposit_pubkey\": \"0322c5f5c9c4b9d1c3e22ca995e200d724c2d7d8b6953f7b38fddf9296053c961f\",\n" +
                "  \"deposit_chain_code\": \"e9a563d68686999af372a33157209c6860fe79197a4dafd9ec1dbaa49523351d\",\n" +
                "  \"gait_onion\": \"s7a4rvc6425y72d2.onion\",\n" +
                "  \"default_peers\": []\n" +
                "}";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final Network2 net;
        try {
            net = objectMapper.readValue(json, Network2.class);
            Log.i(TAG, "net:" + net);
        } catch (IOException e) {
            e.printStackTrace();
        }


        syncCustomNetworks(networkCustomRemove, networkSelector, customNetworks);

    }

    private void syncCustomNetworks(ListPreference networkCustomRemove, MultiSelectListPreference networkSelector, Set<String> customNetworksNew) {
        mService.cfg().edit()
                .putStringSet("custom_networks", customNetworksNew)
                .apply();
        final List<String> customNetworksNewList = new ArrayList<>(customNetworksNew);
        Collections.sort(customNetworksNewList);
        final String[] entriesNow = customNetworksNewList.toArray(new String[customNetworksNewList.size()]);
        networkCustomRemove.setEntries(entriesNow);
        networkCustomRemove.setEntryValues(entriesNow);

        final String[] standardNetworks = getResources().getStringArray(R.array.available_networks);
        final List<String> standardAndCustomNetworks = new ArrayList<>();
        standardAndCustomNetworks.addAll( Arrays.asList(standardNetworks) );
        standardAndCustomNetworks.addAll( customNetworksNewList );

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
