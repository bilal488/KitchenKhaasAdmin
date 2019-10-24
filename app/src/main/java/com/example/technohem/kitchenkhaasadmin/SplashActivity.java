package com.example.technohem.kitchenkhaasadmin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final ImageView imageView = (ImageView)findViewById(R.id.imageView_logo);

        final Animation fade_in_anim = AnimationUtils.loadAnimation(getBaseContext(),R.anim.fade_in);
        final Animation fade_out_anim = AnimationUtils.loadAnimation(getBaseContext(),R.anim.fade_out);

        imageView.startAnimation(fade_in_anim);
        fade_in_anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                imageView.startAnimation(fade_out_anim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fade_out_anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                finish();
                Intent i = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(i);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }
}
