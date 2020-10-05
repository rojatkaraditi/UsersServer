package com.helloworld.usersinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private String filterType = "All Users";
    private RecyclerView.LayoutManager layoutManager;
    private Spinner dropdown;
    private EditText searchTextFilter;
    private String searchString = "";
    private CheckBox checkBoxMale;
    private CheckBox checkBoxFemale;

    ArrayList<User> userArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        dropdown = findViewById(R.id.spinner);
        searchTextFilter = findViewById(R.id.searchTextFilter);
        checkBoxMale = findViewById(R.id.checkBoxMale);
        checkBoxFemale = findViewById(R.id.checkBoxFemale);

        final ArrayList<String> dropDownList = new ArrayList<>();
        dropDownList.add("All Users");
        dropDownList.add("FirstName");
        dropDownList.add("LastName");
        dropDownList.add("Email");

        Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(UserListActivity.this, android.R.layout.simple_spinner_dropdown_item, dropDownList);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterType = dropDownList.get(position);
                if(filterType.equals("All Users")){
                    searchTextFilter.setEnabled(false);
                    searchTextFilter.setText("");
                    searchTextFilter.setHint("");
                }else if(filterType.equals("FirstName")){
                    searchTextFilter.setEnabled(true);
                    searchTextFilter.setHint("Search with First Name");
                    searchTextFilter.setText("");
                }else if(filterType.equals("LastName")){
                    searchTextFilter.setEnabled(true);
                    searchTextFilter.setHint("Search with Last Name");
                    searchTextFilter.setText("");
                }else{
                    searchTextFilter.setEnabled(true);
                    searchTextFilter.setHint("Search with Email");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        findViewById(R.id.filterUserButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //again it will get called with the corresponding user filter
                if(filterType.equals("FirstName")){
                    if(checkValidations(searchTextFilter)){
//                        firstName=test
                        searchString = "firstName="+searchTextFilter.getText().toString();
                    }
                }else if(filterType.equals("LastName")){
                    if(checkValidations(searchTextFilter)){
                        searchString = "lastName="+searchTextFilter.getText().toString();
                    }
                }else if(filterType.equals("Email")){
                    if (checkValidations(searchTextFilter)) {
                        searchString = "email=" + searchTextFilter.getText().toString();
                    }
                }

                if(!checkBoxFemale.isChecked() && !checkBoxMale.isChecked()){
                    Toast.makeText(UserListActivity.this, "Please select male and female textbox", Toast.LENGTH_SHORT).show();
                }else if(!checkBoxFemale.isChecked() && checkBoxMale.isChecked()){
                    if (searchString.equals("")) {
                        searchString = "gender=Female";
                    }else{
                        searchString += "&gender=Female";
                    }
                    new LoadUsersAsync().execute("");
                }else if(checkBoxFemale.isChecked() && !checkBoxMale.isChecked()){
                    if (searchString.equals("")) {
                        searchString = "gender=Male";
                    }else{
                        searchString += "&gender=Male";
                    }
                    new LoadUsersAsync().execute("");
                }else{
                    //everything is checked
                    new LoadUsersAsync().execute("");
                }
            }
        });

        findViewById(R.id.filterReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchString = "";
                checkBoxFemale.setChecked(true);
                checkBoxMale.setChecked(true);
                searchTextFilter.setText("");
                searchTextFilter.setHint("");
                searchTextFilter.setEnabled(false);
                dropdown.setSelection(0);
                new LoadUsersAsync().execute("");
            }
        });

        new LoadUsersAsync().execute("");
    }

    public boolean checkValidations(EditText editText){
        if(editText.getText().toString().equals("")){
            editText.setError("Cannot be empty");
            return false;
        }else{
            return true;
        }
    }

    public class LoadUsersAsync extends AsyncTask<String, Void, String> {
        boolean isStatus = true;

        @Override
        protected String doInBackground(String... strings) {
            userArrayList.clear();
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("TokeyKey", 0);
            final OkHttpClient client = new OkHttpClient();
            String listofUsers = null;
            if(!searchString.equals("")){
                searchString = "?"+searchString;
            }
            Request request = new Request.Builder()
                    .url("http://167.99.228.2:3000/api/v1/users/"+searchString)
                    .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()){
                    isStatus = true;
                }else{
                    isStatus = false;
                }
                listofUsers = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return listofUsers;
        }

        @Override
        protected void onPostExecute(String users) {
            super.onPostExecute(users);

            if (users != null) {
                try {
                    JSONObject root = new JSONObject(users);
                    if (isStatus) {
                        JSONArray rootArray = root.getJSONArray("users");
                        for (int i = 0; i < rootArray.length(); i++) {
                            JSONObject arrayObject = rootArray.getJSONObject(i);
                            User user = new User();
                            user.id = arrayObject.getString("_id");
                            user.fname = arrayObject.getString("firstName");
                            user.lname = arrayObject.getString("lastName");
                            user.gender = arrayObject.getString("gender");
                            user.age = arrayObject.getString("age");
                            user.email = arrayObject.getString("email");
                            userArrayList.add(user);
                        }
                    }else{
                        //some error has occurred.. Have to handle it.
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("demo", userArrayList.toString());

                if (userArrayList.size() > 0) {
                    //For Recycler Views:
                    recyclerView = (RecyclerView) findViewById(R.id.userRecyclerView);

                    layoutManager = new LinearLayoutManager(UserListActivity.this);
                    recyclerView.setLayoutManager(layoutManager);

                    // specify an adapter (see also next example)
                    mAdapter = new UserListAdapter(userArrayList, UserListActivity.this);
                    recyclerView.setAdapter(mAdapter);
                }else{
                    Toast.makeText(UserListActivity.this, "Sorry no users found!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}