package com.example.labyrinth;

/**
 * Created by thiro on 2018/03/17.
 */

public class Item {
    private static final int LIMIT_TIME_UNIT = 1000 * 20;
    private int level;
    private String name = "EnhancedView";
    private int x;
    private int y;
    private int printX = 0;
    private int printY = 0;
    private long startTime = 0;
    private IViewLevel viewLevel;
    private ISePlayer sePlayer;
    private IDisplaySize displaySize;

    public Item(int x, int y, IDisplaySize displaySize, IViewLevel viewLevel, ISePlayer sePlayer){
        this.viewLevel = viewLevel;
        this.sePlayer = sePlayer;
        this.displaySize = displaySize;
        double rate = Math.random();
        if(rate < 0.5){
            level = 1;
        }
        else if(rate < (0.5 + 0.35)){
            level = 2;
        }
        else{
            level = 3;
        }
        this.x = x;
        this.y = y;
    }

    public void initPrintXY(int offsetX, int offsetY){
        printX = offsetX + x * displaySize.getCellSize();
        printY = offsetY + y * displaySize.getCellSize();
    }

    public void movePrintXY(EDirection direction){
        printX += -direction.getDx() * displaySize.getCellSize();
        printY += -direction.getDy() * displaySize.getCellSize();
    }

    public int getPrintX(){return printX;}
    public int getPrintY(){return printY;}

    public int getLevel(){return level;}
    private long getLimitTime(){
        return ((long)level * LIMIT_TIME_UNIT);
    }

    public boolean isFinished(){
        return (getLimitTime() < System.currentTimeMillis() - startTime);
    }

    public void activateAction(){
        viewLevel.upViewLevel();
        startTime = System.currentTimeMillis();
        sePlayer.playSoundViewUp();
    }

    public void finalAction(){
        viewLevel.downViewLevel();
        sePlayer.playSoundViewDown();
    }
}
