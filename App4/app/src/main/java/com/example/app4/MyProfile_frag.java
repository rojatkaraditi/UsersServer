package com.example.app4;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyProfile_frag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyProfile_frag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "okay";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyProfile_frag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyProfile_frag.
     */
    // TODO: Rename and change types and number of parameters
    public static MyProfile_frag newInstance(String param1, String param2) {
        MyProfile_frag fragment = new MyProfile_frag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    TextView firstName_tv,lastName_tv,age_tv,gender_tv,email_tv;
    TextInputLayout firstName_TIL,lastName_TIL,age_TIL;
    TextInputEditText firstName_TIET,lastName_TIET,age_TIET;
    RadioGroup genderRadioGroup;
    MotionLayout motionLayout;
    ImageView genderImage;
    SharedPreferences preferences;
    Gson gson =  new Gson();
    User u;
    SharedPreferences.Editor editor;
    MyProfileInteractWithNavActivity obj1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile_frag, container, false);

        motionLayout = view.findViewById(R.id.motionLayout);
        genderImage = view.findViewById(R.id.App_icon);
        firstName_TIET = view.findViewById(R.id.firstName_TIET);
        lastName_TIET = view.findViewById(R.id.lastName_TIET);
        age_TIET = view.findViewById(R.id.age_TIET);
        firstName_TIL = view.findViewById(R.id.firstName_TIL);
        lastName_TIL = view.findViewById(R.id.lastName_TIL);
        age_TIL = view.findViewById(R.id.age_TIL);
        genderRadioGroup = view.findViewById(R.id.radio_group_male_female);

        firstName_tv = view.findViewById(R.id.firstName_inmyProfile);
        lastName_tv = view.findViewById(R.id.lastname_inmyProfile);;
        age_tv = view.findViewById(R.id.age_inmyProfile);;
        gender_tv = view.findViewById(R.id.gender_inmyProfile);
        email_tv = view.findViewById(R.id.email_inmyProfile);
        genderImage = view.findViewById(R.id.userImage);

        preferences = getContext().getSharedPreferences("TokeyKey", 0);

        // setting vlue in each fields
        SetValuesInFields();

        view.findViewById(R.id.editButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // update profile
                if (CheckIfFieldAreEmpty()){
                    String fnameValue = firstName_TIET.getText().toString().trim();
                    String lnameValue = lastName_TIET.getText().toString().trim();
                    //String repeatPasswordValue = repeatPassword.getText().toString().trim();
                    String ageValue = age_TIET.getText().toString().trim();
                    String genderValue;
                    if(R.id.female == genderRadioGroup.getCheckedRadioButtonId()){
                        genderValue = "Female";
                    }else{
                        genderValue = "Male";
                    }
                    new UpdateNewUser(u.id,fnameValue,lnameValue,ageValue,genderValue).execute("");
                }
            }
        });

        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.male){
                    genderImage.setImageResource(R.drawable.male);
                }else{
                    genderImage.setImageResource(R.drawable.female);
                }
            }
        });

        obj1 = (MyProfileInteractWithNavActivity) getActivity();
        return view;
    }

    private void SetValuesInFields() {
        String temp =  preferences.getString("USER",null);
        u = gson.fromJson(temp, User.class);
        //textVies
        Log.d(TAG, "SetValuesInFields: u in  my profile=>"+u);
        age_tv.setText("Age: "+u.age);
        email_tv.setText("Email: "+u.email);
        firstName_tv.setText("FirstName: "+u.fname);
        lastName_tv.setText("LastName: "+u.lname);
        gender_tv.setText("Gender: "+u.gender);
        //textlayoutedittext
        age_TIET.setText(u.age);
        firstName_TIET.setText(u.fname);
        lastName_TIET.setText(u.lname);
        if(u.gender.equals("Female")){
            genderRadioGroup.check(R.id.female);
            genderImage.setImageResource(R.drawable.female);
        }else{
            genderRadioGroup.check(R.id.male);
            genderImage.setImageResource(R.drawable.male);
        }
        //gender_tv.setText(u.gender);
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
            Toast.makeText(getContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(age_TIET.getText().toString().equals("")){
            age_TIL.setError("Cannot be empty");
            return false;
        }else{
            age_TIL.setError("");
        }
        return true;
    }

    public class UpdateNewUser extends AsyncTask<String, Void, String> {
        boolean isStatus = true;

        String passwordValue, fnameValue, lnameValue, gender, id;
        String age;
        public UpdateNewUser(String id, String fnameValue, String lnameValue, String age, String gender) {
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
            //SharedPreferences preferences = getContext().getSharedPreferences("TokeyKey", 0);
            RequestBody formBody = new FormBody.Builder()
                    .add("firstName",fnameValue)
                    .add("lastName",lnameValue)
                    .add("age",age)
                    .add("gender",gender)
                    //.add("password",passwordValue)
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
                Log.d(TAG," "+response.isSuccessful());
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
                        Toast.makeText(getContext(), root.getString("result"), Toast.LENGTH_SHORT).show();
                        //getting updated user info
                        new getUser().execute();
                    }else{
                        //Handling the error scenario here
                        JSONObject error = root.getJSONObject("error");
                        JSONArray message = error.getJSONArray("errors");
                        JSONObject arrayObject = message.getJSONObject(0);
                        Toast.makeText(getContext(), arrayObject.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class getUser extends AsyncTask<String, Void, String> {
        boolean isStatus = true;
        @Override
        protected String doInBackground(String... strings) {
            final OkHttpClient client = new OkHttpClient();
            SharedPreferences preferences = getContext().getSharedPreferences("TokeyKey", 0);

            try {
                Request request = new Request.Builder()
                        .url("http://167.99.228.2:3000/api/v1/users/profile")
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
            super.onPostExecute(result1);
            JSONObject root = null;
            Log.d(TAG,result1);
            try {
                root = new JSONObject(result1);
                if(isStatus){
                    Log.d(TAG,root.toString());
                    User user = new User();
                    user.id = root.getString("_id");
                    user.fname = root.getString("firstName");
                    user.lname = root.getString("lastName");
                    user.gender = root.getString("gender");
                    user.email = root.getString("email");
                    user.age = root.getString("age");

                    editor = preferences.edit();
                    //editor.putString("TOKEN_KEY",root.getString("token"));
                    editor.putString("ID",user.id);
                    editor.putString("USER",gson.toJson(user));
                    editor.commit();
                    //closing my profile
                    obj1.updateSideBar();
                    getActivity().onBackPressed();
                    Log.d(TAG, "onPostExecute: user token exists");
                    //result = "okay";
                }else{
                    //It means that they are some error while signing up.
                    Toast.makeText(getContext(), "Session has expired. Please login again!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //toStopAnimationOFIcon = true;
        }
    }

    interface MyProfileInteractWithNavActivity{
        void updateSideBar();
    }

}