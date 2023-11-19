package com.example.labyrinth;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Intent nextTopIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        nextTopIntent = new Intent(getApplicationContext(), MainActivity.class);
        nextTopIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        nextTopIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent intent = getIntent();
        int movement = intent.getIntExtra("MOVEMENT", 10000);
        int shortest = intent.getIntExtra("SHORTEST", 128);
        int effective = intent.getIntExtra("EFFECTIVE", 10000);
        long elapsedTime = intent.getLongExtra("TIME", 1000 * 60 * 15);
        double difficulty = (double)shortest * Math.log(effective) / Math.log(shortest);

        double score = 0.0;
        score += 50.0*Math.exp(-Math.log(2.0)*(double)(movement - shortest) / difficulty);
        score += 50.0*Math.exp(-Math.log(2.0)*(double)(elapsedTime) / (difficulty * 1000.0));

        TextView scoreTextView = (TextView) findViewById(R.id.scoreText);
        scoreTextView.setText(String.format(Locale.US,"  SCORE          :%7.3f/100", score));
        TextView movementTextView = (TextView) findViewById(R.id.movementText);
        movementTextView.setText(String.format(Locale.US,"  MOVEMENT:%d/%d",movement,shortest));
        TextView timeTextView = (TextView) findViewById(R.id.timeText);
        timeTextView.setText(String.format(Locale.US,"  TIME             :%02d:%02d:%02d", ((elapsedTime/1000)/60), ((elapsedTime/1000)%60), ((elapsedTime / 10)%100)));

        Button restartButton = (Button) findViewById(R.id.restartButton);
        restartButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finishMedia();
                finish();
            }
        });

        Button goToTopButton = (Button) findViewById(R.id.goToTopButton);
        goToTopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finishMedia();
                startActivity(nextTopIntent);
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
        mediaPlayer = MediaPlayer.create(this,R.raw.bgm_result_mission_reward_pokemon);
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
