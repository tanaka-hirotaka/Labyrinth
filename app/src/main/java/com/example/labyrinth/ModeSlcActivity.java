package com.example.labyrinth;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ModeSlcActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_slc);

        intent = new Intent(ModeSlcActivity.this,GameActivity.class);

        TextView textViewVE = (TextView)findViewById(R.id.slc_text_very_easy);
        textViewVE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("X SIZE", 10);
                intent.putExtra("Y SIZE", 10);
                intent.putExtra("IS RANDOM MODE", false);
                finishMedia();
                startActivity(intent);
            }
        });

        TextView textViewE = (TextView)findViewById(R.id.slc_text_easy);
        textViewE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("X SIZE", 50);
                intent.putExtra("Y SIZE", 50);
                intent.putExtra("IS RANDOM MODE", true);
                finishMedia();
                startActivity(intent);
            }
        });

        TextView textViewN = (TextView)findViewById(R.id.slc_text_normal);
        textViewN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("X SIZE", 100);
                intent.putExtra("Y SIZE", 100);
                intent.putExtra("IS RANDOM MODE", true);
                finishMedia();
                startActivity(intent);
            }
        });

        TextView textViewD = (TextView)findViewById(R.id.slc_text_danger);
        textViewD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("X SIZE", 150);
                intent.putExtra("Y SIZE", 150);
                intent.putExtra("IS RANDOM MODE", true);
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
        mediaPlayer = MediaPlayer.create(this,R.raw.bgm_mode_slc_we_are_explorers_pokemon);
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
