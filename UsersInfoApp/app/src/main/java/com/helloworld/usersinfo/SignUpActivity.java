package com.helloworld.usersinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

    private EditText fname;
    private EditText lname;
    private EditText password;
    private EditText repeatPassword;
    private EditText email;
    private EditText age;
    private EditText gender;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setTitle("Sign Up");
        fname = findViewById(R.id.editTextFname);
        lname = findViewById(R.id.editTextLname);
        password = findViewById(R.id.editTextChoosePassword);
        gender = findViewById(R.id.editTextGender);
        repeatPassword = findViewById(R.id.editTextRepeatPassword);
        email = findViewById(R.id.editTextEmail);
        age = findViewById(R.id.editTextAge);

        findViewById(R.id.buttonSignupFirst).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidations(fname) &&
                        checkValidations(lname) &&
                        checkValidations(email) &&
                        checkValidations(gender) &&
                        checkValidations(age) &&
                        checkValidations(password) &&
                        checkValidations(repeatPassword) ){
                    String fnameValue = fname.getText().toString().trim();
                    String lnameValue = lname.getText().toString().trim();
                    String passwordValue = password.getText().toString().trim();
                    String repeatPasswordValue = repeatPassword.getText().toString().trim();
                    String emailValue = email.getText().toString().trim();
                    String ageValue = age.getText().toString().trim();
                    String genderValue = gender.getText().toString().trim();
                    if(passwordValue.equals(repeatPasswordValue)){
                        byte[] passwordEncoded = new byte[0];
                        String base64 = "";
                        try {
                            passwordEncoded = passwordValue.getBytes("UTF-8");
                            base64 = Base64.encodeToString(passwordEncoded, Base64.NO_WRAP);
                            new createNewUser(emailValue,base64,fnameValue,lnameValue,ageValue,genderValue).execute("");
                        } catch (UnsupportedEncodingException e) {
                            Toast.makeText(SignUpActivity.this, "Error occured in Password. Please try again", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }else{
                        repeatPassword.setError("Both passwords should match");
                        password.setError("Both passwords should match");
                    }
                }
            }
        });

        findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
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
                        editor.commit();
                        Toast.makeText(SignUpActivity.this, "User Successfully created", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SignUpActivity.this, UserListActivity.class);
                        intent.putExtra("UserObject", user);
                        startActivityForResult(intent, 100);
                        finish();
                    }else{
                        //Handling the error scenario here
                        JSONObject error = root.getJSONObject("error");
                        if(error.length() > 1){
                            //It means duplicate email issue
                            JSONObject keyValue = error.getJSONObject("keyValue");
                            if(keyValue.getString("email") != null){
                                Toast.makeText(SignUpActivity.this, "Email already exist. Please login again!", Toast.LENGTH_SHORT).show();
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
        }
    }
}