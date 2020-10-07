package com.example.app4;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "okay";

    // this is a login activity for this app

    TextInputLayout email_TIL,password_TIL;
    TextInputEditText email_TIET,password_TIET;
    ImageView icon;
    MotionLayout motionLayout;
    String result ="";
    //for animations
    boolean toStopAnimationOFIcon = false;
    boolean isItFromSignUp = false;
    //adding gson
    Gson gson =  new Gson();
    private int SIGNUPCode = 1111;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // seting animations it should be called before content is loaded
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);

        icon = findViewById(R.id.App_Icon);
        motionLayout = findViewById(R.id.motionLayout);
        email_TIET = findViewById(R.id.email_TIET);
        password_TIET = findViewById(R.id.password_TIET);
        email_TIL = findViewById(R.id.email_TIL);
        password_TIL = findViewById(R.id.password_TIL);

        //setting motion/transition listener
        MotionListner();

        //checking if users has already loged in
        checkIfUserIsLogedIN();


        findViewById(R.id.signup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: signup button");
                Intent i =  new Intent(MainActivity.this,SignUpActivity.class);
                ActivityOptions  options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,icon,"Icon");
                startActivityForResult(i,SIGNUPCode,options.toBundle());
            }
        });

        findViewById(R.id.sigin_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckIfEmailAndPasswordAreEmpty()){
                    String loginText = email_TIET.getText().toString().trim();
                    String passwordText = password_TIET.getText().toString().trim();
                    Log.d("demo",loginText+" "+passwordText);
                    //starting animantion here
                    Log.d(TAG, "onClick: started motionlayout animation to end");
                    motionLayout.transitionToEnd();
                    Log.d(TAG, "onClick: calling async");
                    new getTokeyAsync(loginText, passwordText).execute();
                }
            }
        });

    }

    private void checkIfUserIsLogedIN() {
        preferences = getApplicationContext().getSharedPreferences("TokeyKey",0);
        String pastTokenKey = preferences.getString("TOKEN_KEY", null);
        if(pastTokenKey!=null && !pastTokenKey.equals("")){
            //starting animation and async to check user
            motionLayout.transitionToEnd();
            new getUser().execute();
        }
    }

    private void MotionListner() {
        motionLayout.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {

            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {

            }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int x) {
                if(R.id.start == x){
                    Log.d(TAG, "onTransitionCompleted: it has reached to start");
                }else{
                    //    start rotate icon when it reaches to end state in motionLayout
                    Log.d(TAG, "onTransitionCompleted: it has reached to end");
                    StartIconRotate();
                }
            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

            }
        });
    }

    void StartIconRotate(){

        icon.animate().rotation(360).setDuration(1000).withEndAction(new Runnable() {
            @Override
            public void run() {
                if(toStopAnimationOFIcon){
                    Log.d(TAG, "run: animation should stop now");
                    if(result.equals("okay")){
                        Log.d(TAG, "run: result = okay, may be from signup activity");
                        Intent i =  new Intent(MainActivity.this,NavActivity.class);
                        startActivity(i);
                        finish();
                    }else {
                        motionLayout.transitionToStart();
                        //setting to start aimation again
                        toStopAnimationOFIcon = false;
                    }

                }else{
                    Log.d(TAG, "run: animate will go again");
                    Log.d(TAG, "run: icon rotation"+icon.getRotation());
                    icon.animate().rotation(icon.getRotation()+360).setDuration(1000).withEndAction(this);
                }
            }
        });
    }

    private boolean CheckIfEmailAndPasswordAreEmpty() {
        if(email_TIET.getText().toString().equals("")){
            email_TIL.setError("Cannot be empty");
            return false;
        }else{
            email_TIL.setError("");
        }
        if(password_TIET.getText().toString().equals("")){
            password_TIL.setError("Cannot be empty");
            return false;
        }else{
            password_TIL.setError("");
        }
        return true;
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

            Log.d(TAG, "doInBackground: async called for login");

            byte[] encodedValue = new byte[0];
            try {
                encodedValue = decodedValue.getBytes("UTF-8");
                String encodedString = Base64.encodeToString(encodedValue, Base64.NO_WRAP);

                Request request = new Request.Builder()
                        .url("http://167.99.228.2:3000/api/v1/login")
                        .header("Authorization", "Basic " + encodedString)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    String result = response.body().string();
                    Log.d(TAG, "doInBackground: login response=>"+result);
                    if (response.isSuccessful()){
                        isStatus = true;
                    }else{
                        isStatus = false;
                    }
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (UnsupportedEncodingException e) {
                Toast.makeText(MainActivity.this, "Some problem occured with the password", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result1) {
            super.onPostExecute(result);
            JSONObject root = null;
            Log.d("demo",result);
            try {
                root = new JSONObject(result1);
                if(isStatus){
                    Log.d("demo",root.toString());
                    User user = new User();
                    user.id = root.getString("_id");
                    user.fname = root.getString("firstName");
                    user.lname = root.getString("lastName");
                    user.gender = root.getString("gender");
                    user.email = root.getString("email");
                    user.age = root.getString("age");
                    preferences = getApplicationContext().getSharedPreferences("TokeyKey",0);
                    editor = preferences.edit();
                    editor.putString("TOKEN_KEY",root.getString("token"));
                    editor.putString("ID",user.id);
                    editor.putString("USER",gson.toJson(user));
                    editor.commit();
                    result = "okay";
//                    Intent intent = new Intent(MainActivity.this, UserListActivity.class);
//                    intent.putExtra("UserObject", user);
//                    startActivity(intent);
                }else{
                    //It means that they are some error while signing up.
                    Toast.makeText(MainActivity.this, root.getString("error"), Toast.LENGTH_SHORT).show();
                    //calling transion in middle
                    motionLayout.transitionToStart();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // if result is suscesfult or not
            toStopAnimationOFIcon = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SIGNUPCode){
            Log.d(TAG, "onActivityResult: got back to mainactivty");
            // starting aimation
            motionLayout.transitionToEnd();
            result = "okay";
            toStopAnimationOFIcon = true;
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
        protected void onPostExecute(String result1) {
            super.onPostExecute(result);
            JSONObject root = null;
            Log.d(TAG,result);
            try {
                root = new JSONObject(result1);
                if(isStatus){
                    Log.d("demo",root.toString());
                    User user = new User();
                    user.id = root.getString("_id");
                    user.fname = root.getString("firstName");
                    user.lname = root.getString("lastName");
                    user.gender = root.getString("gender");
                    user.email = root.getString("email");
                    user.age = root.getString("age");
//                    Intent intent = new Intent(MainActivity.this, UserListActivity.class);
//                    intent.putExtra("UserObject", user);
//                    startActivity(intent);
                    // to send user to next activity is this is successful
                    Log.d(TAG, "onPostExecute: user token exists");
                    result = "okay";
                }else{
                    //It means that they are some error while signing up.
                    Toast.makeText(MainActivity.this, "Session has expired. Please login again!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            toStopAnimationOFIcon = true;
        }
    }
}