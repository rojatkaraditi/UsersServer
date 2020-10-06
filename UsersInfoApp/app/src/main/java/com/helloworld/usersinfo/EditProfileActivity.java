package com.helloworld.usersinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class EditProfileActivity extends AppCompatActivity {

    private EditText editProfileActivityFname;
    private EditText editProfileActivityLname;
    private EditText editProfileActivityGender;
    private EditText editProfileActivityPassword;
    private EditText editProfileActivityRepeatPassword;
    private TextView epapassword;
    private TextView eparpassword;
    private Button eapsave;
    private EditText eapAge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        final User user = (User) getIntent().getExtras().getSerializable("UserObject");

        EditText editProfileActivityEmail = findViewById(R.id.editProfileActivityEmail);
        editProfileActivityEmail.setText(user.email);
        editProfileActivityEmail.setEnabled(false);

        editProfileActivityFname = findViewById(R.id.editProfileActivityFname);
        editProfileActivityLname = findViewById(R.id.editProfileActivityLname);
        editProfileActivityGender = findViewById(R.id.editProfileActivityGender);
        editProfileActivityPassword = findViewById(R.id.editProfileActivityPassword);
        editProfileActivityRepeatPassword = findViewById(R.id.editProfileActivityRepeatPassword);
        eapAge = findViewById(R.id.eapAge);
        eapsave = findViewById(R.id.eapsave);
        eapsave.setEnabled(false);

        editProfileActivityFname.setText(user.fname);
        editProfileActivityLname.setText(user.lname);
        editProfileActivityGender.setText(user.gender);
        eapAge.setText(user.age);

        epapassword = findViewById(R.id.epapassword);
        eparpassword = findViewById(R.id.eparpassword);

        editProfileActivityFname.setEnabled(false);
        editProfileActivityLname.setEnabled(false);
        editProfileActivityGender.setEnabled(false);
        eapAge.setEnabled(false);

        findViewById(R.id.editProfileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfileActivityFname.setEnabled(true);
                editProfileActivityLname.setEnabled(true);
                editProfileActivityGender.setEnabled(true);
                eapAge.setEnabled(true);
                epapassword.setVisibility(TextView.VISIBLE);
                eparpassword.setVisibility(TextView.VISIBLE);
                editProfileActivityPassword.setVisibility(TextView.VISIBLE);
                editProfileActivityRepeatPassword.setVisibility(TextView.VISIBLE);
                eapsave.setEnabled(true);
            }
        });

        findViewById(R.id.editProfileClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.editProfileClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        eapsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidations(editProfileActivityFname) &&
                        checkValidations(editProfileActivityLname) &&
                        checkValidations(editProfileActivityGender) &&
                        checkValidations(eapAge) &&
                        checkValidations(editProfileActivityPassword) &&
                        checkValidations(editProfileActivityRepeatPassword) ){
                    String fnameValue = editProfileActivityFname.getText().toString().trim();
                    String lnameValue = editProfileActivityLname.getText().toString().trim();
                    String passwordValue = editProfileActivityPassword.getText().toString().trim();
                    String repeatPasswordValue = editProfileActivityRepeatPassword.getText().toString().trim();
                    String ageValue = eapAge.getText().toString().trim();
                    String genderValue = editProfileActivityGender.getText().toString().trim();
                    if(passwordValue.equals(repeatPasswordValue)){
                        byte[] passwordEncoded = new byte[0];
                        String base64 = "";
                        try {
                            passwordEncoded = passwordValue.getBytes("UTF-8");
                            base64 = Base64.encodeToString(passwordEncoded, Base64.NO_WRAP);
                            new UpdateNewUser(user.id,base64,fnameValue,lnameValue,ageValue,genderValue).execute("");
                        } catch (UnsupportedEncodingException e) {
                            Toast.makeText(EditProfileActivity.this, "Error occured in Password. Please try again", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }else{
                        editProfileActivityPassword.setError("Both passwords should match");
                        editProfileActivityRepeatPassword.setError("Both passwords should match");
                    }
                }
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

    public class UpdateNewUser extends AsyncTask<String, Void, String> {
        boolean isStatus = true;

        String passwordValue, fnameValue, lnameValue, gender, id;
        String age;
        public UpdateNewUser(String id, String passwordValue, String fnameValue, String lnameValue, String age, String gender) {
            this.id = id;
            this.passwordValue = passwordValue;
            this.fnameValue = fnameValue;
            this.lnameValue = lnameValue;
            this.age = age;
            this.gender = gender;
        }

        @Override
        protected String doInBackground(String... strings) {

            final OkHttpClient client = new OkHttpClient();
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("TokeyKey", 0);
            RequestBody formBody = new FormBody.Builder()
                    .add("_id",id)
                    .add("firstName",fnameValue)
                    .add("lastName",lnameValue)
                    .add("age",age)
                    .add("gender",gender)
                    .add("password",passwordValue)
                    .build();
            Request request = new Request.Builder()
                    .url("http://167.99.228.2:3000/api/v1/users")
                    .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
                    .put(formBody)
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
                        Toast.makeText(EditProfileActivity.this, root.getString("result"), Toast.LENGTH_SHORT).show();
                    }else{
                        //Handling the error scenario here
                        JSONObject error = root.getJSONObject("error");
                        JSONArray message = error.getJSONArray("errors");
                        JSONObject arrayObject = message.getJSONObject(0);
                        Toast.makeText(EditProfileActivity.this, arrayObject.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}