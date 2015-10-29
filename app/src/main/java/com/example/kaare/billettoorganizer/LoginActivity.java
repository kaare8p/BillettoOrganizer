package com.example.kaare.billettoorganizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {;
    private static final String PREFS_NAME = "AUTHENTICATION_PREFS";
    private SharedPreferences prefs;

    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView fail;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emailEditText = (EditText) findViewById(R.id.email);
        fail = (TextView) findViewById(R.id.loginFail);
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

        String token = prefs.getString("token", null);

        if (expirationDateString != null) {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expirationDate = null;
            Date now = new Date();
            try {
                expirationDate = format.parse(expirationDateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (expirationDate != null && expirationDate.after(now)) {
                Intent intent = new Intent(this, ActiveEvents.class);
                intent.putExtra("token", token);
                startActivity(intent);
            }
        }
    }

    public void onClickLogin(View v) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        email =  emailEditText.getText().toString(); //"test@gruppe2.dk";
        password = passwordEditText.getText().toString(); //"test1234";

        new checkLogin().execute();
    }

    private class checkLogin extends AsyncTask<Objects, Object, Object> {

        @Override
        protected Object doInBackground(Objects... params) {
            try {
                URL url = new URL("https://billetto.dk/api/v1/user/login");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("ORGANIZATION", "1");
                conn.setRequestProperty("Accept-Language", "en");

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
                fail.setVisibility(View.VISIBLE);
            } else {
                fail.setVisibility(View.INVISIBLE);
                try {
                    JSONObject JSONob = new JSONObject((String) response);
                    JSONObject JSONobData = JSONob.getJSONObject("data");
                    JSONObject JSONobUser = JSONobData.getJSONObject("User");
                    JSONObject JSONobAuth = JSONobUser.getJSONObject("authentication_token");

                    String token = JSONobAuth.getString("token");
                    String expirationDate = JSONobAuth.getString("expiration_date").substring(0, 10);
                    prefs.edit().putString("token", token).apply();
                    prefs.edit().putString("expirationDate", expirationDate).apply();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onPostExecute(response);
                Intent intent = new Intent(LoginActivity.this, ActiveEvents.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.dialog_exit);
        builder.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fail.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}


