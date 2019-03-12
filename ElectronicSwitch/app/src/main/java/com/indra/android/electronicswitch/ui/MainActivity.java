package com.indra.android.electronicswitch.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indra.android.electronicswitch.R;
import com.indra.android.electronicswitch.helper.Constant;
import com.indra.android.electronicswitch.helper.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private CardView mControlling1CardView, mControlling2CardView;
    private TextView mName1TextView, mName2TextView, mDescription1TextView, mDescription2TextView;
    private Switch mControlling1Switch, mControlling2Switch;
    private ImageView mControlling1ImageView, mControlling2ImageView;
    private ProgressDialog loadingDialog;
    private ProgressDialog loadingDialog2;

    private String name;
    private List<String> nameList;

    private String value1, value2, switchValue;
    private String usernameaio, aiokey, electronicName1, electronicName2;

    //Volley Request Queue
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mControlling1CardView = (CardView) findViewById(R.id.controlling1_card_view);
        mControlling2CardView = (CardView) findViewById(R.id.controlling2_card_view);
        mName1TextView = (TextView) findViewById(R.id.name1_text_view);
        mName2TextView = (TextView) findViewById(R.id.name2_text_view);
        mDescription1TextView = (TextView) findViewById(R.id.description1_text_view);
        mDescription2TextView = (TextView) findViewById(R.id.description2_text_view);
        mControlling1ImageView = (ImageView) findViewById(R.id.controlling1_image_view);
        mControlling2ImageView = (ImageView) findViewById(R.id.controlling2_image_view);
        mControlling1Switch = (Switch) findViewById(R.id.controlling1_switch);
        mControlling2Switch = (Switch) findViewById(R.id.controlling2_switch);

        nameList = new ArrayList<>();

        getSharedPreferences();

        mControlling1CardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditElectronicNameActivity.class);
                intent.putExtra("electronic", "1");
                startActivity(intent);
                return true;
            }
        });

        mControlling2CardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditElectronicNameActivity.class);
                intent.putExtra("electronic", "2");
                startActivity(intent);
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mControlling1Switch.setOnCheckedChangeListener(null);
        mControlling2Switch.setOnCheckedChangeListener(null);
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCanceledOnTouchOutside(true);
        loadingDialog.setCancelable(true);
        loadingDialog.setMessage("Please Wait...");
        loadingDialog.show();
        nameList.clear();
        requestQueue = Volley.newRequestQueue(this);
        getAllFeeds();
//        addFeedToServer();
//        addDataToServer();
//        getLastData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings_action_menu) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }else if (id == R.id.logout_action_menu){
            SharedPreferences sharedPreferences = this.getSharedPreferences("electronicswitch", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        usernameaio = sharedPreferences.getString("usernameaio", "1");
        aiokey = sharedPreferences.getString("aiokey", "1");

        Log.e("getSharedPreferences", "usernameAIO = " + usernameaio + " aiokey = " + aiokey);

        electronicName1 = sharedPreferences.getString("electronicname1", "Lampu 1");
        electronicName2 = sharedPreferences.getString("electronicname2", "Lampu 2");

        mName1TextView.setText(electronicName1);
        mName2TextView.setText(electronicName2);

        mDescription1TextView.setText(electronicName1);
        mDescription2TextView.setText(electronicName2);
        //Register Listener
          sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    //GET ALL FEEDS

    private JsonArrayRequest getAllFeedsFromServer() {

        //JsonArrayRequest of volley
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Constant.READ_ALL_FEEDS_URL + usernameaio + "/feeds/",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
//                        Calling method parseData to parse the json response

                        Log.e("MainActivity", "responseAllFeeds :" + response);
                        parseData(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MainActivity", "error");
                        loadingDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Cannot Load Data", Toast.LENGTH_SHORT).show();
                    }
                })

        {

            /** Passing some request headers* */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
//                headers.put("Content-Type", "application/json");
                headers.put("X-AIO-Key", aiokey);
                return headers;
            }
        };

        //Returning the request
        return jsonArrayRequest;
    }

    //This method will parse json data
    private void parseData(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                //Getting json
                json = array.getJSONObject(i);

                name = json.getString("name");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (name.equals("switch1") || name.equals("switch2")) {
                nameList.add(name);
            }
        }

        if(nameList.size() == 0) {
            addFeed1ToServer();
        }else if (!nameList.get(0).equals("switch1")){
            addFeed1ToServer();
        }else if (!nameList.get(0).equals("switch2")){
            addFeed2ToServer();
        }

        for (int i = 0; i < nameList.size(); i++) {
            Log.e("MainActivity", "name :"+nameList.get(i));
            if (nameList.size() > 0 && nameList.get(i).equals("switch1")) {
                getLastData(i);
            } else if (nameList.size() > 0 && nameList.get(i).equals("switch2")) {
                getLastData(i);
            }
        }

    }

    //This method will get data from the web api
    private void getAllFeeds() {
        //Adding the method to the queue by calling the method getDataFromServer
        getAllFeedsFromServer().setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(getAllFeedsFromServer());
    }

    private void changeStatus(int status, String value) {
        if (status == 0) {
            if (value.equals("0")) {
                mControlling1CardView.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
                mControlling1ImageView.setImageResource(R.drawable.lightoff_80);
                mControlling1Switch.setText("OFF");
                mControlling1Switch.setChecked(false);
            }else if (value.equals("1")){
                mControlling1CardView.setCardBackgroundColor(getResources().getColor(R.color.componentColor));
                mControlling1ImageView.setImageResource(R.drawable.lighton_80);
                mControlling1Switch.setText("ON");
                mControlling1Switch.setChecked(true);
            }
            mControlling1Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b == true){
                        switchValue = "1";
                        addData1ToServer();
                        changeStatus(0, "1");
                    }else {

                        switchValue = "0";
                        addData1ToServer();
                        changeStatus(0, "0");
                    }
                }
            });
            loadingDialog.dismiss();
        } else if (status == 1) {
            if (value.equals("0")) {
                mControlling2CardView.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
                mControlling2ImageView.setImageResource(R.drawable.lightoff_80);
                mControlling2Switch.setText("OFF");
                mControlling2Switch.setChecked(false);
            }else if (value.equals("1")){
                mControlling2CardView.setCardBackgroundColor(getResources().getColor(R.color.componentColor));
                mControlling2ImageView.setImageResource(R.drawable.lighton_80);
                mControlling2Switch.setText("ON");
                mControlling2Switch.setChecked(true);
            }
            mControlling2Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b == true){
                        switchValue = "1";
                        addData2ToServer();
                        changeStatus(1, "1");
                    }else {
                        switchValue = "0";
                        addData2ToServer();
                        changeStatus(1, "0");
                    }
                }
            });
            loadingDialog.dismiss();
        }
    }


    private void addFeed1ToServer() {

        StringRequest stringRequest4 = new StringRequest(Request.Method.POST, Constant.ADD_FEED_URL + usernameaio + "/feeds",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        progressBar.setVisibility(View.GONE);
                        Log.e("MainActivity", "ResponseAddFeed1Report: " + response);

                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);

                            addFeed2ToServer();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        changeStatus(0, "0");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog.dismiss();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })

        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
//                headers.put("Content-Type", "application/json");
                headers.put("X-AIO-Key", aiokey);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("name", "switch1");
                params.put("description", "Controlling Electronic Switch");

                return params;
            }
        };
        stringRequest4.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest4);
    }

    private void addFeed2ToServer() {

        StringRequest stringRequest4 = new StringRequest(Request.Method.POST, Constant.ADD_FEED_URL + usernameaio + "/feeds",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("MainActivity", "ResponseAddFeed2Report: " + response);

                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        changeStatus(1, "0");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog.dismiss();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })

        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
//                headers.put("Content-Type", "application/json");
                headers.put("X-AIO-Key", aiokey);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("name", "switch2");
                params.put("description", "Controlling Electronic Switch 2");

                return params;
            }
        };
        stringRequest4.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest4);
    }

    private JsonObjectRequest getLastData1FromServer() {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constant.READ_LAST_DATA1_URL + usernameaio + "/feeds/switch1/data/last/", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("MainActivity", "lastData1Response :" + response);
                        //Success Callback
                        try {
                            value1 = response.getString("value");
                            changeStatus(0, value1);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog.dismiss();
                        Log.e("MainActivity", "error");
                        Toast.makeText(MainActivity.this, "Cannot Load Data", Toast.LENGTH_SHORT).show();
                        //Failure Callback
                    }
                })

        {

            /** Passing some request headers* */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
//                headers.put("Content-Type", "application/json");
                headers.put("X-AIO-Key", aiokey);
                return headers;
            }
        };

        return jsonObjectRequest;
    }


    private JsonObjectRequest getLastData2FromServer() {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constant.READ_LAST_DATA2_URL + usernameaio + "/feeds/switch2/data/last/", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("MainActivity", "lastData2Response :" + response);
                        //Success Callback
                        try {
                            value2 = response.getString("value");
                            changeStatus(1, value2);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog.dismiss();
                        Log.e("MainActivity", "error");
                        Toast.makeText(MainActivity.this, "Cannot Load Data", Toast.LENGTH_SHORT).show();
                        //Failure Callback
                    }
                })

        {

            /** Passing some request headers* */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
//                headers.put("Content-Type", "application/json");
                headers.put("X-AIO-Key", aiokey);
                return headers;
            }
        };

        return jsonObjectRequest;
    }

    //This method will get data from the web api
    private void getLastData(int mode) {
        //Adding the method to the queue by calling the method getDataFromServer
        if (mode == 0) {
            getLastData1FromServer().setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(getLastData1FromServer());
        }else if (mode == 1){
            getLastData2FromServer().setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(getLastData2FromServer());
        }
    }


    private void addData1ToServer() {

        loadingDialog2 = new ProgressDialog(this);
        loadingDialog2.setCanceledOnTouchOutside(true);
        loadingDialog2.setCancelable(true);
        loadingDialog2.setMessage("Please Wait...");
        loadingDialog2.show();

        StringRequest stringRequest4 = new StringRequest(Request.Method.POST, Constant.ADD_DATA1_URL + usernameaio + "/feeds/switch1/data",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        progressBar.setVisibility(View.GONE);
                        Log.e("OrderDetailActivity", "ResponseAddData1Report: " + response);

                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);

                            //if no error in response
                            if (!obj.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        loadingDialog2.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog2.dismiss();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
//                headers.put("Content-Type", "application/json");
                headers.put("X-AIO-Key", aiokey);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("value", switchValue);

                return params;
            }
        };
        stringRequest4.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest4);
    }

    private void addData2ToServer() {

        loadingDialog2 = new ProgressDialog(this);
        loadingDialog2.setCanceledOnTouchOutside(true);
        loadingDialog2.setCancelable(true);
        loadingDialog2.setMessage("Please Wait...");
        loadingDialog2.show();

        StringRequest stringRequest4 = new StringRequest(Request.Method.POST, Constant.ADD_DATA2_URL + usernameaio + "/feeds/switch2/data",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        progressBar.setVisibility(View.GONE);
                        Log.e("OrderDetailActivity", "ResponseAddData2Report: " + response);

                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);

                            //if no error in response
                            if (!obj.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        loadingDialog2.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog2.dismiss();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
//                headers.put("Content-Type", "application/json");
                headers.put("X-AIO-Key", aiokey);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("value", switchValue);

                return params;
            }
        };
        stringRequest4.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest4);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        usernameaio = sharedPreferences.getString("usernameaio", "1");
        aiokey = sharedPreferences.getString("aiokey", "1");

        electronicName1 = sharedPreferences.getString("electronicname1", "Lampu 1");
        electronicName2 = sharedPreferences.getString("electronicname2", "Lampu 2");

        mName1TextView.setText(electronicName1);
        mName2TextView.setText(electronicName2);

        mDescription1TextView.setText(electronicName1);
        mDescription2TextView.setText(electronicName2);
    }
}
