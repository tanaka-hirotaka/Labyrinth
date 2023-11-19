package com.example.labyrinth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thiro on 2018/03/18.
 */

public class FieldView extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder holder;
    private Thread drawThread;
    private List<IDrawer> drawers = new ArrayList<>();
    private int callCreateCount = 0;

    public FieldView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        callCreateCount = 0;
    }

    public void addDrawer(IDrawer drawer){
        drawers.add(drawer);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        createThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawThread = null;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public  void createThread(){
        if(callCreateCount == 1){
            drawThread = new DrawThread();
            drawThread.start();
        }
        else{
            callCreateCount++;
        }
    }

    public void deleteThread(){
        drawThread = null;
    }

    private class DrawThread extends Thread{
        @Override
        public void run(){
            while (drawThread != null){
                Canvas canvas = null;
                try{
                    canvas = holder.lockCanvas();

                    if(canvas != null){
                        Paint paint = new Paint();
                        //canvas.save();

                        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR); //draw background
                        for(IDrawer drawer : drawers){
                            drawer.draw(canvas,paint);
                        }
                        //canvas.restore();
                    }
                }
                finally {
                    if(canvas != null){
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
