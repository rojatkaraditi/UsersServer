package com.helloworld.usersinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
    private RecyclerView.LayoutManager layoutManager;

    ArrayList<User> userArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
        new LoadUsersAsync().execute("");

    }

    public class LoadUsersAsync extends AsyncTask<String, Void, String> {
        boolean isStatus = true;

        @Override
        protected String doInBackground(String... strings) {
            userArrayList.clear();
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("TokeyKey", 0);
            final OkHttpClient client = new OkHttpClient();
            String listofUsers = null;
            Request request = new Request.Builder()
                    .url("http://167.99.228.2:3000/api/v1/users/")
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
                }
            }
        }
    }
}