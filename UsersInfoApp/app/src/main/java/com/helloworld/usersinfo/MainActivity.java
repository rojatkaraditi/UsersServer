package com.helloworld.usersinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText login;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = findViewById(R.id.login);
        password = findViewById(R.id.password);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("TokeyKey", 0);
        String pastTokenKey = preferences.getString("TOKEN_KEY", null);
//        String userId = preferences.getString("ID", null);
        if(pastTokenKey!=null && !pastTokenKey.equals("")){
            new getUser().execute();
        }

        findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(checkValidations(login)&&checkValidations(password)){
                    String loginText = login.getText().toString().trim();
                    String passwordText = password.getText().toString().trim();
                    Log.d("demo",loginText+" "+passwordText);
                    new getTokeyAsync(loginText, passwordText).execute();
                }
            }
        });

        findViewById(R.id.buttonSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

    public boolean checkValidations(EditText editText){
        if(editText.getText().toString().equals("")){
            editText.setError("Cannot be empty");
            return false;
        }else{
            return true;
        }
    }

    public class getTokeyAsync extends AsyncTask<String, Void, String> {

        String username, password;
        boolean isStatus =true;

        public getTokeyAsync(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(String... strings) {
            final OkHttpClient client = new OkHttpClient();
            String decodedValue = username+":"+password;

            byte[] encodedValue = new byte[0];
            try {
                encodedValue = decodedValue.getBytes("UTF-8");
                String encodedString = Base64.encodeToString(encodedValue, Base64.NO_WRAP);

                Request request = new Request.Builder()
                        .url("http://167.99.228.2:3000/api/v1/login")
                        .header("Authorization", "Basic " + encodedString)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()){
                        isStatus = true;
                    }else{
                        isStatus = false;
                    }
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (UnsupportedEncodingException e) {
                Toast.makeText(MainActivity.this, "Some problem occured with the password", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

//
//            // Sending side
//            byte[] data = text.getBytes("UTF-8");
//            String base64 = Base64.encodeToString(data, Base64.DEFAULT);
//
//            // Receiving side
//            byte[] data = Base64.decode(base64, Base64.DEFAULT);
//            String text = new String(data, "UTF-8");

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject root = null;
            Log.d("demo",result);
            try {
                root = new JSONObject(result);
                if(isStatus){
                    Log.d("demo",root.toString());
                    User user = new User();
                    user.id = root.getString("_id");
                    user.fname = root.getString("firstName");
                    user.lname = root.getString("lastName");
                    user.gender = root.getString("gender");
                    user.email = root.getString("email");
                    user.age = root.getString("age");
                    SharedPreferences preferences = getApplicationContext().getSharedPreferences("TokeyKey",0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("TOKEN_KEY",root.getString("token"));
                    editor.putString("ID",user.id);
                    editor.commit();
                    Intent intent = new Intent(MainActivity.this, UserListActivity.class);
                    intent.putExtra("UserObject", user);
                    startActivity(intent);
                }else{
                    //It means that they are some error while signing up.
                    Toast.makeText(MainActivity.this, root.getString("error"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class getUser extends AsyncTask<String, Void, String> {
        boolean isStatus = true;
        @Override
        protected String doInBackground(String... strings) {
            final OkHttpClient client = new OkHttpClient();
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("TokeyKey", 0);

            try {
                Request request = new Request.Builder()
                        .url("http://167.99.228.2:3000/api/v1/users/"+preferences.getString("ID", null))
                        .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()){
                        isStatus = true;
                    }else{
                        isStatus = false;
                    }
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (Exception e){

            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject root = null;
            Log.d("demo",result);
            try {
                root = new JSONObject(result);
                if(isStatus){
                    Log.d("demo",root.toString());
                    User user = new User();
                    user.id = root.getString("_id");
                    user.fname = root.getString("firstName");
                    user.lname = root.getString("lastName");
                    user.gender = root.getString("gender");
                    user.email = root.getString("email");
                    user.age = root.getString("age");
                    Intent intent = new Intent(MainActivity.this, UserListActivity.class);
                    intent.putExtra("UserObject", user);
                    startActivity(intent);
                }else{
                    //It means that they are some error while signing up.
                    Toast.makeText(MainActivity.this, root.getString("Session has expired. Please login again!"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}