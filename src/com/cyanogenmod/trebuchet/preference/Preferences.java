/*
 * Copyright (C) 2011 The CyanogenMod Project
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

package com.factoryrom.launcher.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.factoryrom.launcher.LauncherApplication;
import com.factoryrom.launcher.R;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class Preferences extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "FactoryROM.Preferences";

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getSharedPreferences(PreferencesProvider.PREFERENCES_KEY,
                Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_headers, target);

        updateHeaders(target);
    }

    private void updateHeaders(List<Header> headers) {
        int i = 0;
        while (i < headers.size()) {
            Header header = headers.get(i);

            if (header.id == R.id.preferences_general_section) {
                Calendar calendar = Calendar.getInstance();
                if (calendar.get(Calendar.HOUR_OF_DAY) == 3) {
                    String[] strings = getResources().getStringArray(R.array.preferences_koush);
                    Random random = new Random();
                    for (int j = 0; j < 20; j++) {
                        Header h = new Header();
                        h.title = strings[random.nextInt(strings.length)];
                        headers.add(i + 1, h);
                    }
                }
            }

            // Version preference
            if (header.id == R.id.preferences_application_version) {
                header.title = getString(R.string.application_name) + " " + getString(R.string.application_version);
            }

            // Increment if not removed
            if (headers.get(i) == header) {
                i++;
            }
        }
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            super.setListAdapter(new HeaderAdapter(this, getHeaders()));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PreferencesProvider.PREFERENCES_CHANGED, true);
        editor.commit();
    }

    public static class HomescreenFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_homescreen);

            PreferenceScreen preferenceScreen = getPreferenceScreen();
            if (LauncherApplication.isScreenLarge()) {
                preferenceScreen.removePreference(findPreference("ui_homescreen_grid"));
            }
        }
    }

    public static class DrawerFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_drawer);
        }
    }

    public static class DockFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_dock);
        }
    }

    public static class GeneralFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_general);
        }
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        private static final int HEADER_TYPE_NORMAL = 0;
        private static final int HEADER_TYPE_CATEGORY = 1;

        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_CATEGORY + 1;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
        }

        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
            if (header.id == R.id.preferences_application_section) {
                return HEADER_TYPE_CATEGORY;
            } else {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false; // because of categories
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public HeaderAdapter(Context context, List<Header> objects) {
            super(context, 0, objects);

            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            View view = null;

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);
                        holder.title = (TextView) view;
                        break;

                    case HEADER_TYPE_NORMAL:
                        view = mInflater.inflate(
                                R.layout.preference_header_item, parent,
                                false);
                        holder.icon = (ImageView) view.findViewById(R.id.icon);
                        holder.title = (TextView)
                                view.findViewById(com.android.internal.R.id.title);
                        holder.summary = (TextView)
                                view.findViewById(com.android.internal.R.id.summary);
                        break;
                }
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            // All view fields must be updated every time, because the view may be recycled
            switch (headerType) {
                case HEADER_TYPE_CATEGORY:
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    break;

                case HEADER_TYPE_NORMAL:
                    holder.icon.setImageResource(header.iconRes);
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    CharSequence summary = header.getSummary(getContext().getResources());
                    if (!TextUtils.isEmpty(summary)) {
                        holder.summary.setVisibility(View.VISIBLE);
                        holder.summary.setText(summary);
                    } else {
                        holder.summary.setVisibility(View.GONE);
                    }
                    break;
            }

            return view;
        }
    }
}
