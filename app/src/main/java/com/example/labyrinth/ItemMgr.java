package com.example.labyrinth;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.util.SparseArray;
import android.view.SurfaceHolder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Random;

/**
 * Created by thiro on 2018/03/17.
 */

public class ItemMgr implements IItemMgr,IDrawer,Runnable,IViewLevel{
    private static final int CELL_PAR_ITEM = 20;
    private IField field;
    private SparseArray<Item> fieldItem = new SparseArray<>();
    private List<Item> effectItem = new LinkedList<>();
    private Queue<Item> activateItem = new ArrayDeque<>();
    private Bitmap[] enhancedViewImg = new Bitmap[3];
    private Bitmap statusBar;
    private IDisplaySize displaySize;
    private boolean isThreadActive = true;

    //for enhanced view
    private int viewLevel = 1;
    private float displayRadius = 0;
    private float viewUnit = 0;

    //time
    private long startTime = 0;
    private long elapsedTime = 0;

    ItemMgr(Context context, IDisplaySize displaySize, ISePlayer sePlayer, IField field){
        this.displaySize = displaySize;
        this.field = field;
        Resources resources = context.getResources();
        enhancedViewImg[0] = BitmapFactory.decodeResource(resources, R.drawable.enhanced_view1);
        enhancedViewImg[1] = BitmapFactory.decodeResource(resources, R.drawable.enhanced_view2);
        enhancedViewImg[2] = BitmapFactory.decodeResource(resources, R.drawable.enhanced_view3);

        statusBar = BitmapFactory.decodeResource(resources, R.drawable.status_bar);
        startTime = System.currentTimeMillis();
        elapsedTime = 0;

        Random random = new Random();
        for(int i = 0; i < (field.getEffectiveSize() / CELL_PAR_ITEM); i++){
            int x;
            int y;
            do{
                x = random.nextInt(field.getSizeX());
                y = random.nextInt(field.getSizeY());
            }while(!(field.isEffective(x,y)) || field.isGoal(x,y));
            fieldItem.append(convertKey(x,y), new Item(x,y,displaySize,this,sePlayer));
        }
    }

    @Override
    public void upViewLevel(){
        viewLevel++;
    }

    @Override
    public void downViewLevel(){
        viewLevel--;
    }

    public long getElapsedTime(){return elapsedTime;}
    private int convertKey(int x, int y){
        return (x * field.getSizeY() + y);
    }

    @Override
    public void activate(int x, int y){
        Item item = fieldItem.get(convertKey(x,y));
        if(item != null){
            item.activateAction();
            activateItem.add(item);
            fieldItem.remove(convertKey(x,y));
        }
    }

    public void movePrintItem(EDirection direction){
        for(int i = 0; i < fieldItem.size(); i++){
            Item item = fieldItem.valueAt(i);
            item.movePrintXY(direction);
        }
    }

    public void initScale(){
        for(int i = 0; i < enhancedViewImg.length; i++){
            enhancedViewImg[i] = Bitmap.createScaledBitmap(enhancedViewImg[i],displaySize.getCellSize(),displaySize.getCellSize(),false);
        }

        statusBar = Bitmap.createScaledBitmap(statusBar, displaySize.getPrintFieldSize(),displaySize.getCellSize(),false);

        displayRadius = (float) Math.sqrt(Math.pow( displaySize.getDefaultOffsetX(),2)+Math.pow(displaySize.getDefaultOffsetY(),2));
        viewUnit = displaySize.getPrintFieldSize() / 6;

        for(int i = 0; i < fieldItem.size(); i++){
            Item item = fieldItem.valueAt(i);
            item.initPrintXY(field.getOffsetX(),field.getOffsetY());
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if(fieldItem == null || effectItem == null){
            return;
        }

        for(int i = 0; i < fieldItem.size(); i++){
            Item item = fieldItem.valueAt(i);
            int itemLevel = item.getLevel();
            if(itemLevel >= 1 && itemLevel <= 3){
                canvas.drawBitmap(enhancedViewImg[item.getLevel() - 1],item.getPrintX(),item.getPrintY(),paint);
            }
        }

        //canvas.save();
        //canvas.clipRect(0,0,displaySize.getPrintFieldSize()+1, displaySize.getDefaultOffsetY()*2);
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(displayRadius*2 - (viewLevel * viewUnit));
        canvas.drawCircle(displaySize.getDefaultOffsetX()+displaySize.getCellSize2(),
                displaySize.getDefaultOffsetY()+displaySize.getCellSize2(),
                displayRadius, paint);
        //canvas.restore();
        paint.reset();

        //draw status bar
        canvas.drawBitmap(statusBar,0,displaySize.getDefaultOffsetY()*2,paint);
        for(int i = 0; i < effectItem.size(); i++){
            canvas.drawBitmap(enhancedViewImg[effectItem.get(i).getLevel() - 1],displaySize.getDefaultOffsetX() + (i+1) * displaySize.getCellSize(),displaySize.getDefaultOffsetY()*2,paint);
        }
        paint.setColor(Color.BLACK);
        canvas.drawRect(displaySize.getCellSize2()/2,
                displaySize.getDefaultOffsetY()*2+displaySize.getCellSize2()/3,
                displaySize.getCellSize()*3,
                displaySize.getDefaultOffsetY()*2+displaySize.getCellSize2()*5/3,paint);
        paint.setColor(Color.GREEN);
        paint.setTextSize(displaySize.getCellSize() * 2 / 3);
        canvas.drawText(String.format(Locale.US,"%02d:%02d:%02d", ((elapsedTime/1000)/60), ((elapsedTime/1000)%60), ((elapsedTime / 10)%100)),
                displaySize.getCellSize2()*2/3,
                displaySize.getDefaultOffsetY()*2+displaySize.getCellSize2()*3/2,paint);
    }

    public void stopThread(){
        this.isThreadActive = false;
    }

    @Override
    public void run() {
        try{
            while (this.isThreadActive){
                if(activateItem == null || effectItem == null){
                    continue;
                }
                while(!activateItem.isEmpty()){
                    effectItem.add(activateItem.remove());
                }
                Iterator<Item> itr = effectItem.iterator();
                while(itr.hasNext()){
                    Item item = itr.next();
                    if(item.isFinished()){
                        item.finalAction();
                        itr.remove();
                    }
                }

                elapsedTime = System.currentTimeMillis() - startTime;
            }
        }
        catch (Exception e){
            return;
        }
    }
}
