package com.example.kaare.billettoorganizer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    onClickLogin(v);
                }
                return false;
            }
        });
    }

    public void onClickLogin(View v) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL url = new URL("https://billetto.dk/api/v1/user/login");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(this, ActiveEvents.class);
        startActivity(intent);
    }

    public void onClickSignUp(View v){
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        Intent intent = new Intent(this, CreateUserActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    private class checkLogin extends AsyncTask<URL, String, String> {

        @Override
        protected String doInBackground(URL... params) {
            try {
                URL url = params[0];
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("ORGANIZATION", "1");
                conn.setRequestProperty("Accept-Language", "en");

                String body = "{\"email\":\"test@gruppe2.dk\", \"password\":\"test12345\", \"device_id\":\"0001\"}";
                conn.setDoOutput(true);
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(body);
                dos.flush();
                dos.close();

                System.out.println("Response Code: " + conn.getResponseCode());
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                StringBuilder response = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                System.out.println(response.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}


