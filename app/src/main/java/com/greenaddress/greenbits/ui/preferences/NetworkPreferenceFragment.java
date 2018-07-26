package com.greenaddress.greenbits.ui.preferences;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.greenaddress.greenbits.ui.R;
import com.greenaddress.greenbits.ui.UI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.schildbach.wallet.ui.ScanActivity;

/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NetworkPreferenceFragment extends GAPreferenceFragment {
    private static final String TAG = GAPreferenceFragment.class.getSimpleName();
    private static final int QRSCANNER = 1338;
    private static final int CAMERA_PERMISSION = 150;

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
        if (mService.getNetwork().getGaitOnion() == null)
            torEnabled.setEnabled(false);
        else {
            torEnabled.setSummary(getString(R.string.torSummary, mService.getNetwork().getGaitOnion()));
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

        final Preference networkCustomAddQr = find("custom_networks_add_qr");
        networkCustomAddQr.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                onScanClicked();
                return true;
            }
        });

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

    private void onScanClicked() {
        final String[] perms = { "android.permission.CAMERA" };
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 &&
                getActivity().checkSelfPermission(perms[0]) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(perms, CAMERA_PERMISSION);
        else {
            final Intent scanner = new Intent(getActivity(), ScanActivity.class);
            startActivityForResult(scanner, QRSCANNER);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case QRSCANNER:
                if (data != null && data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT) != null) {
                    String config = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
                    Log.d(TAG, "Network config: " + config);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] granted) {
        if (requestCode == CAMERA_PERMISSION &&
                isPermissionGranted(granted, R.string.err_qrscan_requires_camera_permissions))
            startActivityForResult(new Intent(getActivity(), ScanActivity.class), QRSCANNER);
    }

    protected boolean isPermissionGranted(final int[] granted, final int msgId) {
        if (granted == null || granted.length == 0 || granted[0] != PackageManager.PERMISSION_GRANTED) {
            UI.toast(getActivity(), msgId, Toast.LENGTH_SHORT);
            return false;
        }
        return true;
    }
}
