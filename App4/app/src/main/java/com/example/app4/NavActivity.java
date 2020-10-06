package com.example.app4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

public class NavActivity extends AppCompatActivity {

    private static final String TAG = "okay";
    private AppBarConfiguration configuration;
    private NavController navController;
    SharedPreferences preferences;
    User u;
    Gson gson = new Gson();
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        //    setting toolbar
        Toolbar t =  findViewById(R.id.app_bar);
        t.setNavigationIcon(R.drawable.menu_icon);
        setSupportActionBar(t);

        preferences =  getApplicationContext().getSharedPreferences("TokeyKey",0);
        String temp = preferences.getString("USER", null);
        u = gson.fromJson(temp,User.class);

        //setting up the drawers
        DrawerLayout drawerLayout = findViewById(R.id.drawer_for_nav);
        navigationView = findViewById(R.id.nav_host);
        configuration = new AppBarConfiguration.Builder(
                R.id.nav_users,R.id.nav_my_profile)
                .setDrawerLayout(drawerLayout)
                .build();
        navController = Navigation.findNavController(this,R.id.frag_container);
        NavigationUI.setupActionBarWithNavController(this,navController,configuration);
        NavigationUI.setupWithNavController(navigationView,navController);

        // setting user info in sidebar
        SettingInfoInHeaderInSidebar();

    }

    private void SettingInfoInHeaderInSidebar() {
        ImageView iv = navigationView.getHeaderView(0).findViewById(R.id.user_image_inNavHeader);
        TextView tv = navigationView.getHeaderView(0).findViewById(R.id.user_name_inNavHeader);
        Log.d(TAG, "SettingInfoInHeaderInSidebar: iv name="+iv.toString());
        if(u.gender.equals("Female")){
            iv.setImageResource(R.drawable.female);
        }else{
            iv.setImageResource(R.drawable.male);
        }
//        tv.setText(u.fname+" "+u.lname);
        tv.setText(u.email);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass to next activity for profile info
            }
        });
    }

    //creating menu or inflating the menu

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = new MenuInflater(this);
//        menuInflater.inflate(R.menu.nav_bar_menu,menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, configuration)
                || super.onSupportNavigateUp();
    }
}