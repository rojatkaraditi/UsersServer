package com.example.app4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "okay";
    TextInputLayout firstName_TIL,lastName_TIL,age_TIL,email_TIL,password_TIL;
    TextInputEditText firstName_TIET,lastName_TIET,age_TIET,email_TIET,password_TIET;
    RadioGroup genderRadioGroup;
    ImageView icon;
    MotionLayout motionLayout;
    Gson gson =  new Gson();

    //for animation
    private boolean toStopAnimationOFIcon=false;
    private String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // seting animations it should be called before content is loaded
        SetAnimation();
        setContentView(R.layout.activity_sign_up);

        motionLayout = findViewById(R.id.motionLayout);
        icon = findViewById(R.id.App_icon);
        firstName_TIET = findViewById(R.id.firstName_TIET);
        lastName_TIET = findViewById(R.id.lastName_TIET);
        age_TIET = findViewById(R.id.age_TIET);
        email_TIET = findViewById(R.id.email_TIET);
        password_TIET = findViewById(R.id.password_TIET);
        firstName_TIL = findViewById(R.id.firstName_TIL);
        lastName_TIL = findViewById(R.id.lastName_TIL);
        age_TIL = findViewById(R.id.age_TIL);
        email_TIL = findViewById(R.id.email_TIL);
        password_TIL = findViewById(R.id.password_TIL);
        genderRadioGroup = findViewById(R.id.radio_group_male_female);

        MotionListner();

        findViewById(R.id.sigin_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckIfFieldAreEmpty()){
                    //call animation
                    motionLayout.transitionToEnd();
                }
            }
        });

    }

    private void SetAnimation() {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
                                    // new Explode()
                                    // new Slide()
        getWindow().setSharedElementExitTransition(new Slide());
        getWindow().setExitTransition(new Explode());
        // new Fade()
        // new Slide()
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAfterTransition();
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
                    //call async task
                    String fnameValue = firstName_TIET.getText().toString().trim();
                    String lnameValue = lastName_TIET.getText().toString().trim();
                    String passwordValue = password_TIET.getText().toString().trim();
                    //String repeatPasswordValue = repeatPassword.getText().toString().trim();
                    String emailValue = email_TIET.getText().toString().trim();
                    String ageValue = age_TIET.getText().toString().trim();
                    String genderValue;
                    if(R.id.female == genderRadioGroup.getCheckedRadioButtonId()){
                        genderValue = "Female";
                    }else{
                        genderValue = "Male";
                    }
                    try {
                        byte[] passwordEncoded = passwordValue.getBytes("UTF-8");
                        String base64 = Base64.encodeToString(passwordEncoded, Base64.NO_WRAP);
                        new createNewUser(emailValue,base64,fnameValue,lnameValue,ageValue,genderValue).execute("");
                    } catch (UnsupportedEncodingException e) {
                        Toast.makeText(SignUpActivity.this, "Error occured in Password. Please try again", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

            }
        });
    }

    void StartIconRotate(){
        /// remove this handler when writing code for authentication
//        final int[] i = {0};
//        final Handler h =  new Handler();
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                toStopAnimationOFIcon = true;
//            }
//        };
//        // in te seconds handler will change the above boolean to true to stop icon rotation
//        h.postDelayed(r, 10000);

        icon.animate().rotationY(360).setDuration(1000).withEndAction(new Runnable() {
            @Override
            public void run() {
                if(toStopAnimationOFIcon){
                    Log.d(TAG, "SignUpActivity run: animation should stop now");
                    if(result.equals("okay")){
                        Log.d(TAG, "run: result is okay in animation ");
                        Intent i =  new Intent();
                        setResult(RESULT_OK);
                        finishAfterTransition();
//                        Intent i =  new Intent(SignUpActivity.this,NavActivity.class);
//                        startActivity(i);
                    }else {
                        motionLayout.transitionToStart();
                        //setting to start animation again if needed
                        toStopAnimationOFIcon = false;
                    }

                }else{
                    Log.d(TAG, "SignUpActivity run: animate will go again");
                    Log.d(TAG, "SignUpActivity run: icon rotation"+icon.getRotationY());
                    icon.animate().rotationY(icon.getRotationY()+360).setDuration(1000).withEndAction(this);
                }
            }
        });
    }

    public boolean CheckIfFieldAreEmpty(){
        if(firstName_TIET.getText().toString().equals("")){
            firstName_TIL.setError("Cannot be empty");
            return false;
        }else{
            firstName_TIL.setError("");
        }
        if(lastName_TIET.getText().toString().equals("")) {
            lastName_TIL.setError("Cannot be empty");
            return false;
        }else{
            lastName_TIL.setError("");
        }
        if(genderRadioGroup.getCheckedRadioButtonId() == -1){
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(age_TIET.getText().toString().equals("")){
            age_TIL.setError("Cannot be empty");
            return false;
        }else{
            age_TIL.setError("");
        }
        if(email_TIET.getText().toString().equals("")){
            email_TIL.setError("Cannot be empty");
            return false;
        }else{
            email_TIL.setError("");
        }
        String x = password_TIET.getText().toString();
        if(x.equals("")){
            password_TIL.setError("Cannot be empty");
            return false;
        }
//        }else if(x.length()<6 || x.length()>20){
//            password_TIL.setError("should be of length more than 6 and less than 20");
//            return false;
//        }
        else{
            password_TIL.setError("");
        }
        return true;
    }

    public class createNewUser extends AsyncTask<String, Void, String> {
        boolean isStatus = true;

        String emailValue, passwordValue, fnameValue, lnameValue, gender;
        String age;
        public createNewUser(String emailValue, String passwordValue, String fnameValue, String lnameValue, String age, String gender) {
            this.emailValue = emailValue;
            this.passwordValue = passwordValue;
            this.fnameValue = fnameValue;
            this.lnameValue = lnameValue;
            this.age = age;
            this.gender = gender;
        }

        @Override
        protected String doInBackground(String... strings) {

            final OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("firstName",fnameValue)
                    .add("lastName",lnameValue)
                    .add("age",age)
                    .add("gender",gender)
                    .add("email", emailValue)
                    .add("password",passwordValue)
                    .build();
            Request request = new Request.Builder()
                    .url("http://167.99.228.2:3000/api/v1/signup")
                    .post(formBody)
                    .build();
            String responseValue = null;
            try (Response response = client.newCall(request).execute()) {
                if(response.isSuccessful()){
                    isStatus = true;
                }else{
                    isStatus = false;
                }
                Log.d("demo"," "+response.isSuccessful());
                responseValue = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseValue;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("demo",s);
            if(s!=null){
                JSONObject root = null;
                try {
                    root = new JSONObject(s);
                    if(isStatus){
                        User user = new User();
                        user.id = root.getString("_id");
                        user.fname = root.getString("firstName").trim();
                        user.lname = root.getString("lastName").trim();
                        user.age = root.getString("age");
                        user.gender = root.getString("gender");
                        user.email = root.getString("email").trim();
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("TokeyKey",0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("TOKEN_KEY",root.getString("token"));
                        editor.putString("ID",user.id);
                        editor.putString("USER",gson.toJson(user));
                        editor.commit();
                        Toast.makeText(SignUpActivity.this, "User Successfully created", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onPostExecute: User Successfully created");
                        result = "okay";
//                        Intent intent = new Intent(SignUpActivity.this, UserListActivity.class);
//                        intent.putExtra("UserObject", user);
//                        startActivityForResult(intent, 100);
//                        finish();
                    }else{
                        //Handling the error scenario here
                        JSONObject error = root.getJSONObject("error");
                        if(error.length() > 1){
                            //It means duplicate email issue
                            JSONObject keyValue = error.getJSONObject("keyValue");
                            if(keyValue.getString("email") != null){
                                Toast.makeText(SignUpActivity.this, "Email already exist. Please use another email!", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(SignUpActivity.this, "Some error occured. Please try again!", Toast.LENGTH_SHORT).show();
                            }
                        }else if(error.length() == 1){
                            JSONArray message = error.getJSONArray("errors");
                            JSONObject arrayObject = message.getJSONObject(0);
                            Toast.makeText(SignUpActivity.this, arrayObject.getString("msg"), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(SignUpActivity.this, "Some error occured. Please try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // whatever is the result animation should stop
            toStopAnimationOFIcon = true;
        }
    }

}