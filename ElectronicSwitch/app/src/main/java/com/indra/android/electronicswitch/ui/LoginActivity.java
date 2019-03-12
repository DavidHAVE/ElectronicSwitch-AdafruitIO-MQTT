package com.indra.android.electronicswitch.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.indra.android.electronicswitch.R;
import com.indra.android.electronicswitch.helper.Constant;
import com.indra.android.electronicswitch.helper.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText editTextUsername, editTextPassword;
    ProgressBar progressBar;
    private ProgressDialog loadingDialog;

    private String username2, response2;

    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        setUpSharedPreferences();
        if (isLoggedIn()){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        if (!isAccount()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", "masuk");
            editor.putString("password", "masuk1234");
            editor.putString("usernameaio", "David0101");
            editor.putString("aiokey", "2f88f84f5fe84e938631fad0cbb25fdc");
            editor.apply();
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);


        //if user presses on login
        //calling the method login
        findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });

    }


    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("electronicswitch", Context.MODE_PRIVATE);
        return sharedPreferences.getString("username", null) != null;
    }

    public boolean isAccount() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString("username", null) != null;
    }


    private void userLogin() {
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCanceledOnTouchOutside(true);
        loadingDialog.setCancelable(true);
        loadingDialog.setMessage("Please Wait...");
        loadingDialog.show();

        //first getting the values
        final String usernameET = editTextUsername.getText().toString().trim();
        final String passwordET = editTextPassword.getText().toString().trim();

        //validating inputs
        if (TextUtils.isEmpty(usernameET)) {
            editTextUsername.setError("Please enter your username");
            editTextUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(passwordET)) {
            editTextPassword.setError("Please enter your password");
            editTextPassword.requestFocus();
            return;
        }

        Log.e("LoginActivity", "usernameET : "+usernameET+", passwordET : "+passwordET);

        getSharedPreferences();

        Log.e("LoginActivity", "username : "+username+", password : "+password);

        if (usernameET.equals(username) && passwordET.equals(password)){
            SharedPreferences sharedPreferences = this.getSharedPreferences("electronicswitch", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", usernameET);
            editor.putString("password", passwordET);
            editor.apply();

            loadingDialog.dismiss();
            Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else{
            loadingDialog.dismiss();
            Toast.makeText(this, "Login Gagal", Toast.LENGTH_SHORT).show();
        }

//        Log.e("LoginActivity", "username2 : "+username2);
    }


    private void getSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        username = sharedPreferences.getString("username", "1");
        password = sharedPreferences.getString("password", "1");

        //Register Listener
        //  sharedPreferences.registerOnSharedPreferenceChangeListener(this);
//        Log.e("setUp", "sKalori = " + sumberKalori + " sLatihan = " + sumberLatihan);
    }
}
