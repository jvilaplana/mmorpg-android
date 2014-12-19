package com.jordiv.mmorpg.mmorpg_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


public class SignUpActivity extends ActionBarActivity {

    EditText username = null;
    EditText password = null;
    EditText password2 = null;
    Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mContext = this;

        Button signUpButton = (Button) findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = (EditText) findViewById(R.id.usernameText);
                password = (EditText) findViewById(R.id.usernamePassword);
                password2 = (EditText) findViewById(R.id.usernamePassword2);

                if(password.getText().toString().length() > 1 && password.getText().toString().equals(password2.getText().toString())) {

                    SignUpTask signUpTask = new SignUpTask();
                    signUpTask.execute();
                    try {
                        Integer statusCode = signUpTask.get();

                        Log.d("JORDI", "statusCode: " + statusCode);

                        // UNACCEPTABLE
                        if(statusCode == 201) { // CREATED
                            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(mContext);

                            dlgAlert.setMessage(R.string.activity_sign_up_username_created);
                            dlgAlert.setTitle(R.string.activity_sign_up_username_created_title);
                            dlgAlert.setPositiveButton(R.string.button_okay, null);
                            dlgAlert.setCancelable(true);
                            dlgAlert.setPositiveButton(R.string.button_okay,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                            dlgAlert.create().show();
                        }
                        else if(statusCode == 406) { // UNACCEPTABLE
                            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(v.getContext());

                            dlgAlert.setMessage(R.string.activity_sign_up_username_incorrect_exists);
                            dlgAlert.setTitle(R.string.activity_sign_up_username_incorrect_title);
                            dlgAlert.setPositiveButton(R.string.button_okay, null);
                            dlgAlert.setCancelable(true);
                            dlgAlert.setPositiveButton(R.string.button_okay,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            dlgAlert.create().show();
                        }
                        else {

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }


                }
                else {
                    AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(v.getContext());

                    dlgAlert.setMessage(R.string.activity_sign_up_password_incorrect_match);
                    dlgAlert.setTitle(R.string.activity_sign_up_password_incorrect_title);
                    dlgAlert.setPositiveButton(R.string.button_okay, null);
                    dlgAlert.setCancelable(true);
                    dlgAlert.create().show();

                    dlgAlert.setPositiveButton(R.string.button_okay,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                }


                //Intent intent = new Intent(v.getContext(), SignUpActivity.class);
                //startActivity(intent);
                //finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
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

    class CheckUsernameTask extends AsyncTask<String, Integer, Boolean> {
        protected Boolean doInBackground(String... params) {
            Boolean exists = true;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost http = new HttpPost(Constants.hostURL + "/api/user/");
                String json = "";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username.getText().toString());
                json = jsonObject.toString();
                StringEntity se = new StringEntity(json);
                http.setEntity(se);
                http.setHeader("Accept", "application/json");
                http.setHeader("Content-type", "application/json");

                HttpResponse httpResponse = httpclient.execute(http);

                JSONObject myObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));

                exists = myObject.getBoolean("exists");
            } catch (Exception e) {
                Log.d("JORDI", e.toString());
            }
            return exists;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            //showDialog("Downloaded " + result + " bytes");
        }
    }

    class SignUpTask extends AsyncTask<String, Integer, Integer> {
        protected Integer doInBackground(String... params) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost http = new HttpPost(Constants.hostURL + "/user/save");
                String json = "";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username.getText().toString());
                jsonObject.put("password", password.getText().toString());
                json = jsonObject.toString();
                StringEntity se = new StringEntity(json);
                http.setEntity(se);
                http.setHeader("Accept", "application/json");
                http.setHeader("Content-type", "application/json");

                HttpResponse httpResponse = httpclient.execute(http);
                return httpResponse.getStatusLine().getStatusCode();

            } catch (Exception e) {
                Log.d("JORDI", e.toString());
            }
            return -1;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            //showDialog("Downloaded " + result + " bytes");
        }
    }
}
