package com.example.app4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.motion.widget.MotionScene;

import android.animation.Animator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

        findViewById(R.id.signup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: signup button");
                Intent i =  new Intent(MainActivity.this,SignUpActivity.class);
                ActivityOptions  options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,icon,"Icon");
                startActivity(i,options.toBundle());
            }
        });

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
                Log.d(TAG, "onClick:transition completed sigin Button");
                StartIsonRotate();
            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

            }
        });
    }


    void StartIsonRotate(){
        /// remove this handler when writing code for authentication
            final int[] i = {0};
            final Handler h =  new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    toStopAnimationOFIcon = true;
                }
            };
            // in te seconds handler will change the above boolean to true to stop icon rotation
            h.postDelayed(r, 10000);

        icon.animate().rotation(360).setDuration(1000).withEndAction(new Runnable() {
            @Override
            public void run() {
                if(toStopAnimationOFIcon){
                    Log.d(TAG, "run: animation should stop now");
                    if(result.equals("okay")){
                        Intent i =  new Intent(MainActivity.this,NavActivity.class);
                        startActivity(i);
                    }else {
                        motionLayout.transitionToStart();
                    }

                }else{
                    Log.d(TAG, "run: animate will go again");
                    Log.d(TAG, "run: icon rotation"+icon.getRotation());
                    icon.animate().rotation(icon.getRotation()+360).setDuration(1000).withEndAction(this);
                }
            }
        });
    }


}