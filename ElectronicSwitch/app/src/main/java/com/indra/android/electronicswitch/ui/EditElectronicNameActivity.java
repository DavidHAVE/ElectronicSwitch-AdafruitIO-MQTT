package com.indra.android.electronicswitch.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.indra.android.electronicswitch.R;
import com.indra.android.electronicswitch.helper.Constant;

public class EditElectronicNameActivity extends AppCompatActivity {

    private EditText mElectronicNameEditText;
    private Button mChangeButton;

    private String electronic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_electronic_name);

        mElectronicNameEditText = (EditText) findViewById(R.id.electronic_name_edit_text);
        mChangeButton = (Button) findViewById(R.id.change_button);

        if (getIntent() != null) {
            electronic = getIntent().getExtras().getString("electronic");
        }


        getSharedPreferences();

        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String electronicName = mElectronicNameEditText.getText().toString().trim();

                savePreference(electronicName);
            }
        });
    }

    private void getSharedPreferences() {
        if (electronic.equals("1")) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String electronicName = sharedPreferences.getString("electronicname1", "Lampu");

            mElectronicNameEditText.setText(electronicName);
        } else if (electronic.equals("2")) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String electronicName = sharedPreferences.getString("electronicname2", "Lampu");

            mElectronicNameEditText.setText(electronicName);
        }
    }


    private void savePreference(String electronicName) {
        if (electronic.equals("1")) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("electronicname1", electronicName);
            editor.apply();
        } else if (electronic.equals("2")) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("electronicname2", electronicName);
            editor.apply();
        }

        finish();
    }
}
