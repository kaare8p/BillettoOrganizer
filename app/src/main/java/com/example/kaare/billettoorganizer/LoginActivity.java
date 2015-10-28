package com.example.kaare.billettoorganizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {;
    private static final String PREFS_NAME = "AUTHENTICATION_PREFS";
    private SharedPreferences prefs;
    private String token;

    private EditText emailEditText;
    private EditText passwordEditText;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    onClickLogin(v);
                }
                return false;
            }
        });

        String expirationDateString = prefs.getString("expirationDate", null);

        token = prefs.getString("token", null);

        if (expirationDateString != null) {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date expirationDate = null;
            Date now = new Date();
            try {
                expirationDate = format.parse(expirationDateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (expirationDate.after(now)) {
                Intent intent = new Intent(this, ActiveEvents.class);
                intent.putExtra("token", token);
                startActivity(intent);
            }
        }
    }

    public void onClickLogin(View v) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    URL url = new URL("https://billetto.dk/api/v1/user/login");
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("ORGANIZATION", "1");
                    conn.setRequestProperty("Accept-Language", "en");

                    email = "test@gruppe2.dk"; //emailEditText.getText().toString();
                    password = "test1234"; //passwordEditText.getText().toString();

                    String body = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\", \"device_id\":\"0001\"}";
                    conn.setDoOutput(true);
                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(body);
                    dos.flush();
                    dos.close();

                    if (conn.getResponseCode() != 200) {
                        return conn.getResponseCode();
                    } else {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line;
                        StringBuilder response = new StringBuilder();

                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                        return response.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object response) {
                if (response instanceof Integer) {
                    emailEditText.setText("FAILFAIL");
                } else {
                    try {
                        JSONObject JSONob = new JSONObject((String) response);
                        JSONObject JSONobData = JSONob.getJSONObject("data");
                        JSONObject JSONobUser = JSONobData.getJSONObject("User");
                        JSONObject JSONobAuth = JSONobUser.getJSONObject("authentication_token");
                        String token = JSONobAuth.getString("token");
                        String expirationDate = JSONobAuth.getString("expiration_date").substring(0, 10);
                        prefs.edit().putString("token", token).commit();
                        prefs.edit().putString("expirationDate", expirationDate).commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    super.onPostExecute(response);
                    Intent intent = new Intent(LoginActivity.this, ActiveEvents.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }
            }
        }.execute();
    }

    public void onClickSignUp(View v){
        Intent intent = new Intent(this, CreateUserActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        startActivity(intent);
    }
}


