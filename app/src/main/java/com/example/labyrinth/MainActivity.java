package com.example.labyrinth;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(MainActivity.this, ModeSlcActivity.class);

        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finishMedia();
                startActivity(intent);
            }
        });

        initMedia();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(mediaPlayer == null){
            initMedia();
        }
        else if(!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        finishMedia();
    }

    private void initMedia(){
        mediaPlayer = MediaPlayer.create(this,R.raw.main_menu_pokemon);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void finishMedia(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
