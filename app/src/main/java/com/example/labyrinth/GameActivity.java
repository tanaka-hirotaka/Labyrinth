package com.example.labyrinth;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Random;

public class GameActivity extends AppCompatActivity implements IDisplaySize,ISePlayer{
    public int layoutWidth;
    public int layoutHeight;
    public static final int CELL_NUM_PAR_VIEW = 13;
    public int cellSize;
    public int cellSize2;
    private int defaultOffsetX;
    private int defaultOffsetY;
    private Field field;
    private ItemMgr itemMgr;
    private Player player;
    private Thread updateThread;
    private float viewOffset = 0.0f;
    FieldView fieldView;
    private MediaPlayer mediaPlayer;
    private int bgmResourceId;
    private SoundPool soundPool;
    private int soundViewUp;
    private int soundViewDown;

    private LinearLayout layout;
    private boolean hasInit = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && layout != null){
            layoutWidth = layout.getWidth();
            layoutHeight = layout.getHeight();
            cellSize = layoutWidth / CELL_NUM_PAR_VIEW;
            cellSize2 = cellSize / 2;
            defaultOffsetX = layoutWidth / 2 - cellSize2;
            defaultOffsetY = layoutHeight / 2 - cellSize2;

            field.initScale();
            itemMgr.initScale();
            player.initScale();

            if(hasInit){
                updateThread = new Thread(itemMgr);
                updateThread.start();
                fieldView.createThread();
                hasInit = false;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        fieldView = new FieldView(this);

        layout = (LinearLayout)findViewById(R.id.activity_game);
        Intent intent = getIntent();
        field = new Field(this,intent.getIntExtra("X SIZE", Field.DEFAULT_SIZE_X),
                intent.getIntExtra("Y SIZE", Field.DEFAULT_SIZE_Y),
                intent.getBooleanExtra("IS RANDOM MODE", false));
        itemMgr = new ItemMgr(this,this,this,field);
        player = new Player(this, field, itemMgr);

        fieldView.addDrawer(field);
        fieldView.addDrawer(itemMgr);
        fieldView.addDrawer(player);
        fieldView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v,MotionEvent event) {
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(isUpperThanLTRB(x,y)){
                            if(isUpperThanRTLB(x,y)){
                                player.action(EDirection.DOWN);
                            }
                            else{
                                player.action(EDirection.LEFT);
                            }
                        }
                        else{
                            if(isUpperThanRTLB(x,y)){
                                player.action(EDirection.RIGHT);
                            }
                            else{
                                player.action(EDirection.UP);
                            }
                        }
                        if(field.isGoal(player.getPointX(),player.getPointY())){
                            itemMgr.stopThread();
                            Intent nextIntent = new Intent(getApplicationContext(),ResultActivity.class);
                            nextIntent.putExtra("MOVEMENT", player.getStepCount());
                            nextIntent.putExtra("TIME", itemMgr.getElapsedTime());
                            nextIntent.putExtra("SHORTEST", field.getShortestDistance());
                            nextIntent.putExtra("EFFECTIVE", field.getEffectiveSize());
                            finishMedia();
                            fieldView.deleteThread();
                            startActivity(nextIntent);
                            finish();
                        }
                        v.performClick();
                        break;
                }
                return true;
            }
        });

        layout.addView(fieldView);

        soundPool = new SoundPool(2,AudioManager.STREAM_MUSIC,0);
        soundViewUp = soundPool.load(this,R.raw.view_up, 0);
        soundViewDown = soundPool.load(this, R.raw.view_down,0);

        if(field.getShortestDistance() >= (field.getMinShortest()*2)){
            bgmResourceId = R.raw.bgm_primal_dialga_battle_pokemon;
        }
        else{
            Random random = new Random();

            switch (random.nextInt(9)){
                case 0:
                    bgmResourceId = R.raw.bgm_amp_plains_pokemon;
                    break;
                case 1:
                    bgmResourceId = R.raw.bgm_beach_cave_pokemon;
                    break;
                case 2:
                    bgmResourceId = R.raw.bgm_brine_cave_pokemon;
                    break;
                case 3:
                    bgmResourceId = R.raw.bgm_craggy_coast_pokemon;
                    break;
                case 4:
                    bgmResourceId = R.raw.bgm_drenched_bluff_pokemon;
                    break;
                case 5:
                    bgmResourceId = R.raw.bgm_foggy_forest_pokemon;
                    break;
                case 6:
                    bgmResourceId = R.raw.bgm_miracle_sea_pokemon;
                    break;
                case 7:
                    bgmResourceId = R.raw.bgm_mt_bristle_pokemon;
                    break;
                case 8:
                default:
                    bgmResourceId = R.raw.bgm_sentry_duty_pokemon;
                    break;
            }
        }
        initMedia();
    }

    @Override
    public void playSoundViewUp(){
        soundPool.play(soundViewUp, 1.0f,1.0f,0,0,1.0f);
    }

    @Override
    public void playSoundViewDown(){
        soundPool.play(soundViewDown, 1.0f,1.0f,0,0,1.0f);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(itemMgr != null){
                itemMgr.stopThread();
            }
            if(fieldView != null){
                fieldView.deleteThread();
            }
        }
        return super.onKeyDown(keyCode,event);
    }

    private void initMedia(){
        mediaPlayer = MediaPlayer.create(this,bgmResourceId);
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
    private boolean isUpperThanLTRB(float x, float y){
        return (y + viewOffset> x * ((float) layoutHeight/layoutWidth));
    }

    private boolean isUpperThanRTLB(float x, float y){
        return (y + viewOffset > ( -x*((float) layoutHeight/layoutWidth) + getDefaultOffsetY()*2));
    }

    @Override
    public int getCellSize() {
        return cellSize;
    }

    @Override
    public int getCellSize2() {
        return cellSize2;
    }

    @Override
    public int getDefaultOffsetX() {
        return defaultOffsetX;
    }

    @Override
    public int getDefaultOffsetY(){
        return defaultOffsetY;
    }

    @Override
    public int getPrintFieldSize(){
        return (cellSize*CELL_NUM_PAR_VIEW);
    }
}
