package com.jordiv.mmorpg.mmorpg_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;


public class LoginActivity extends ActionBarActivity {

    public static final String PREFS_NAME = "mmorpgPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String token = settings.getString("token", null);
        if(token != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        Button loginButton = (Button) findViewById(R.id.loginButton);
        Button signUpButton = (Button) findViewById(R.id.signUpButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                LoginTask loginTask = new LoginTask();
                loginTask.execute();
                try {
                    String token = loginTask.get();

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("token", token);
                    editor.commit();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                String token = settings.getString("token", null);
                if(token != null) {
                    Intent intent = new Intent(v.getContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * required in order to prevent issues in earlier Android version.
     */
    private static void disableConnectionReuseIfNecessary() {
        // see HttpURLConnection API doc
        if (Integer.parseInt(Build.VERSION.SDK)
                < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private static String getResponseText(InputStream inStream) {
        // very nice trick from
        // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
        return new Scanner(inStream).useDelimiter("\\A").next();
    }

    class LoginTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... params) {

            String tokenValue = null;

            try {
                EditText username = (EditText) findViewById(R.id.usernameText);
                EditText password = (EditText) findViewById(R.id.usernamePassword);

                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPOST = new HttpPost("http://10.100.3.163:8080/mmorpg/api/login");
                String json = "";
                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username.getText().toString());
                jsonObject.put("password", password.getText().toString());

                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);
                // 6. set httpPost Entity
                httpPOST.setEntity(se);
                // 7. Set some headers to inform server about the type of the content
                //httpPUT.setHeader("Accept", "application/json");
                httpPOST.setHeader("Content-type", "application/json");

                HttpResponse httpResponse = httpclient.execute(httpPOST);

                JSONObject myObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));

                String tokenType = myObject.getString("token_type");
                String tokenUsername = myObject.getString("username");
                tokenValue = myObject.getString("access_token");

            } catch (Exception e) {
                Log.d("JORDI", e.toString());
            }

            return tokenValue;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            //showDialog("Downloaded " + result + " bytes");
        }
    }
}

