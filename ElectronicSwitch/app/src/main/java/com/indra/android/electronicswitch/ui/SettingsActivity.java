package com.indra.android.electronicswitch.ui;

import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.indra.android.electronicswitch.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

//                getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

//            LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
//            Toolbar bar = (Toolbar) LayoutInflater.from(getContext()).inflate(R.layout.preferences_toolbar, root, false);
//            bar.setTitleTextColor(getResources().getColor(R.color.white));
//            bar.setSubtitleTextColor(getResources().getColor(R.color.white));
//            root.addView(bar, 0); // insert at top
//            bar.setNavigationOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    finish();
//                }
//            });

        }
    }
}
