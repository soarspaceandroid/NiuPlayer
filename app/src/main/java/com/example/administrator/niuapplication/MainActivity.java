package com.example.administrator.niuapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    FMVideoPlayer fmVideoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        fmVideoPlayer = findViewById(R.id.player);
        fmVideoPlayer.setVideoDurition(1000)
        .setCoverPath("http://cdn-uat.tvsonar.com/fly/pic/6478214352490126337e3af504933904f479c5c0075e64f2c241553665565590.png")
        .setVideoPath("http://cdn-uat.tvsonar.com/fly/video/647821435249012633773e39810c6644bebb202ae09425802921553665562853.mp4");

        fmVideoPlayer.postDelayed(new Runnable() {
            @Override
            public void run() {
                fmVideoPlayer.start();
            }
        }  , 500);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        fmVideoPlayer.release();
    }
}
