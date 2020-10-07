package com.example.app4;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsersFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFrag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "okay";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UsersFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UsersFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static UsersFrag newInstance(String param1, String param2) {
        UsersFrag fragment = new UsersFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private String filterType = "All Users";
    private RecyclerView.LayoutManager layoutManager;
    private Spinner dropdown;
    private EditText searchTextFilter;
    private String searchString = "";
    private SharedPreferences preferences;
    ArrayList<User> userArrayList = new ArrayList<>();
    private CheckBox checkBoxMale;
    private CheckBox checkBoxFemale;

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
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        dropdown = view.findViewById(R.id.spinner);
        searchTextFilter = view.findViewById(R.id.searchTextFilter);
        checkBoxMale = view.findViewById(R.id.checkBoxMale);
        checkBoxFemale = view.findViewById(R.id.checkBoxFemale);
        preferences = getActivity().getApplicationContext().getSharedPreferences("TokeyKey", 0);

        final ArrayList<String> dropDownList = new ArrayList<>();
        dropDownList.add("All Users");
        dropDownList.add("FirstName");
        dropDownList.add("LastName");
        dropDownList.add("Email");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, dropDownList);
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

        view.findViewById(R.id.filterUserButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //again it will get called with the corresponding user filter
                searchString = "";
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
                    Toast.makeText(getActivity(), "Please select male and female textbox", Toast.LENGTH_SHORT).show();
                }else if(checkBoxFemale.isChecked() && !checkBoxMale.isChecked()){
                    if (searchString.equals("")) {
                        searchString = "gender=Female";
                    }else{
                        searchString += "&gender=Female";
                    }
                    new LoadUsersAsync().execute("");
                }else if(!checkBoxFemale.isChecked() && checkBoxMale.isChecked()){
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

        view.findViewById(R.id.filterReset).setOnClickListener(new View.OnClickListener() {
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

        recyclerView = (RecyclerView) view.findViewById(R.id.userRecyclerView);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new UserListAdapter(userArrayList, getActivity());
        recyclerView.setAdapter(mAdapter);


        new LoadUsersAsync().execute("");
        return view;
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
//                    recyclerView = (RecyclerView) findViewById(R.id.userRecyclerView);
//
//                    layoutManager = new LinearLayoutManager(getContext());
//                    recyclerView.setLayoutManager(layoutManager);
//
//                    // specify an adapter (see also next example)
//                    mAdapter = new UserListAdapter(userArrayList, getActivity());
//                    recyclerView.setAdapter(mAdapter);
                    Log.d(TAG, "onPostExecute: calling notify datasetchanged");
                    mAdapter.notifyDataSetChanged();
                }else{
                    //userArrayList.clear();
                    mAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Sorry no users found!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}