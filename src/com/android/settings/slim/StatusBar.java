/*
 * Copyright (C) 2013 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.slim;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.omni.PackageUtils;

import java.util.List;
import java.util.ArrayList;

import com.android.settings.tesla.SeekBarPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class StatusBar extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "StatusBarSettings";
    private static final String CUSTOM_HEADER_IMAGE = "status_bar_custom_header";
    private static final String DAYLIGHT_HEADER_PACK = "daylight_header_pack";
    private static final String DEFAULT_HEADER_PACKAGE = "com.android.systemui";
    private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
    private static final String PREF_CUSTOM_HEADER = "status_bar_custom_header";

    private SwitchPreference mCustomHeader;
    private ListPreference mDaylightHeaderPack;
    private SwitchPreference mCustomHeaderImage;
    private SeekBarPreference mHeaderShadow;

    private static final String TAG = "StatusBarSettings";

    @Override
    protected int getMetricsCategory() {
        // todo add a constant in MetricsLogger.java
        return MetricsLogger.MAIN_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        final ContentResolver resolver = getActivity().getContentResolver();

        mCustomHeader = (SwitchPreference) prefSet.findPreference(PREF_CUSTOM_HEADER);
        mCustomHeader.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER, 0) == 1));
        mCustomHeader.setOnPreferenceChangeListener(this);

        // header image packs
        final boolean customHeaderImage = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER, 0) == 1;
        mCustomHeaderImage = (SwitchPreference) findPreference(CUSTOM_HEADER_IMAGE);
        mCustomHeaderImage.setChecked(customHeaderImage);

        String imageHeaderPackage = Settings.System.getString(getContentResolver(),
                Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK);
        if (imageHeaderPackage == null) {
            imageHeaderPackage = DEFAULT_HEADER_PACKAGE;
        }
        mDaylightHeaderPack = (ListPreference) findPreference(DAYLIGHT_HEADER_PACK);
        List<String> entries = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        getAvailableHeaderPacks(entries, values);
        mDaylightHeaderPack.setEntries(entries.toArray(new String[entries.size()]));
        mDaylightHeaderPack.setEntryValues(values.toArray(new String[values.size()]));

        int valueIndexHeader = mDaylightHeaderPack.findIndexOfValue(imageHeaderPackage);
        if (valueIndexHeader == -1) {
            // no longer found
            imageHeaderPackage = DEFAULT_HEADER_PACKAGE;
            Settings.System.putString(getContentResolver(),
                    Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK, imageHeaderPackage);
            valueIndexHeader = mDaylightHeaderPack.findIndexOfValue(imageHeaderPackage);
        }
        mDaylightHeaderPack.setValueIndex(valueIndexHeader >= 0 ? valueIndexHeader : 0);
        mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntry());
        mDaylightHeaderPack.setOnPreferenceChangeListener(this);
        mDaylightHeaderPack.setEnabled(customHeaderImage);

        // header image shadows
        mHeaderShadow = (SeekBarPreference) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
        int headerShadow = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, 0);
        mHeaderShadow.setValue(headerShadow);
        mHeaderShadow.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
    ContentResolver resolver = getActivity().getContentResolver();
      if (preference == mCustomHeader) {
            Settings.System.putInt(getContentResolver(),
                 Settings.System.STATUS_BAR_CUSTOM_HEADER,
                 (Boolean) newValue ? 1 : 0);
      } else if (preference == mDaylightHeaderPack) {
            String value = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK, value);
            int valueIndex = mDaylightHeaderPack.findIndexOfValue(value);
            mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntries()[valueIndex]);
      } else if (preference == mHeaderShadow) {
         int headerShadow = (Integer) newValue;
         Settings.System.putInt(getActivity().getContentResolver(),
                 Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, headerShadow);
      }
        return true;
    }

    private void getAvailableHeaderPacks(List<String> entries, List<String> values) {
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.DaylightHeaderPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            if (packageName.equals(DEFAULT_HEADER_PACKAGE)) {
                values.add(0, packageName);
            } else {
                values.add(packageName);
            }
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            if (packageName.equals(DEFAULT_HEADER_PACKAGE)) {
                entries.add(0, label);
            } else {
                entries.add(label);
            }
        }
        i.setAction("org.omnirom.DaylightHeaderPack1");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            values.add(packageName  + "/" + r.activityInfo.name);
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = packageName;
            }
            entries.add(label);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCustomHeaderImage) {
            final boolean value = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_CUSTOM_HEADER, value ? 1 : 0);
            mDaylightHeaderPack.setEnabled(value);
            return true;
         }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}
