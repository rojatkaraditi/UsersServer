package com.example.app4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "okay";
    TextInputLayout firstName_TIL,lastName_TIL,age_TIL,email_TIL,password_TIL;
    TextInputEditText firstName_TIET,lastName_TIET,age_TIET,email_TIET,password_TIET;
    ImageView icon;
    MotionLayout motionLayout;

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

        MotionListner();

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
                Log.d(TAG, "SignUpActivity onClick:transition completed sigin Button");
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

        icon.animate().rotationY(360).setDuration(1000).withEndAction(new Runnable() {
            @Override
            public void run() {
                if(toStopAnimationOFIcon){
                    Log.d(TAG, "SignUpActivity run: animation should stop now");
                    if(result.equals("okay")){
                        Intent i =  new Intent(SignUpActivity.this,NavActivity.class);
                        startActivity(i);
                    }else {
                        motionLayout.transitionToStart();
                    }

                }else{
                    Log.d(TAG, "SignUpActivity run: animate will go again");
                    Log.d(TAG, "SignUpActivity run: icon rotation"+icon.getRotation());
                    icon.animate().rotationY(icon.getRotationY()+360).setDuration(1000).withEndAction(this);
                }
            }
        });
    }
}