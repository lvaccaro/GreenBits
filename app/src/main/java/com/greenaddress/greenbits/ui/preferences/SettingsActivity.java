package com.greenaddress.greenbits.ui.preferences;
import android.annotation.TargetApi;
import android.os.Build;

import com.greenaddress.greenbits.GreenAddressApplication;
import com.greenaddress.greenbits.ui.R;

import java.util.List;

public class SettingsActivity extends GaPreferenceActivity {

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(final List<Header> target) {
        if (  ((GreenAddressApplication)getApplication()).mService.isElements() )
            loadHeadersFromResource(R.xml.pref_headers_elements, target);
        else
            loadHeadersFromResource(R.xml.pref_headers, target);
    }
}
