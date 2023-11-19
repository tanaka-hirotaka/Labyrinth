package com.example.labyrinth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by thiro on 2018/03/15.
 */


public class Player implements IDrawer{
    private int pointX;
    private int pointY;
    private EDirection direction;
    private int stepCount;
    private IDisplaySize displaySize;
    private IField field;
    private IItemMgr itemMgr;
    private Path path;
    private Path goalPointerPath;
    private float centerGX = 0;
    private float centerGY = 0;
    private float radius = 0;

    Player(IDisplaySize displaySize, IField field, IItemMgr itemMgr){
        this.displaySize = displaySize;
        this.field = field;
        this.itemMgr = itemMgr;
        setDirection(EDirection.DOWN);

        pointX = field.getStartX();
        pointY = field.getStartY();
        stepCount = 0;
    }

    public int getPointX(){return pointX;}
    public int getPointY(){return pointY;}
    public int getStepCount(){return stepCount;}

    private void setDirection(EDirection direction){
        this.direction = direction;

        initScale();
    }

    public void initScale(){
        radius = displaySize.getCellSize2() * 4 / 5;
        float centerX = displaySize.getDefaultOffsetX() + displaySize.getCellSize2();
        float centerY = displaySize.getDefaultOffsetY() + displaySize.getCellSize2();
        float theta = (float) direction.getTheta();
        float theta2 = theta + (float) Math.PI * 3 / 4;
        float theta3 = theta - (float)Math.PI * 3 / 4;
        path = new Path();
        path.moveTo(centerX + radius * (float)Math.cos(theta),
                centerY - radius * (float)Math.sin(theta));
        path.lineTo(centerX + radius * (float)Math.cos(theta2),
                centerY - radius * (float)Math.sin(theta2));
        path.lineTo(centerX + radius * (float)Math.cos(theta3),
                centerY - radius * (float)Math.sin(theta3));
        path.close();

        centerGX = displaySize.getDefaultOffsetX() + displaySize.getCellSize2();
        centerGY = displaySize.getDefaultOffsetY()*2 + displaySize.getCellSize2();
        double thetaG = Math.atan2(field.getGoalY() - pointY, field.getGoalX() - pointX);
        double thetaG2 = thetaG + Math.PI * 5 / 6;
        double thetaG3 = thetaG - Math.PI * 5 / 6;
        goalPointerPath = new Path();
        goalPointerPath.moveTo(centerGX + radius * (float)Math.cos(thetaG),
                                 centerGY + radius * (float)Math.sin(thetaG));
        goalPointerPath.lineTo(centerGX + radius * (float)Math.cos(thetaG2),
                                 centerGY + radius * (float)Math.sin(thetaG2));
        goalPointerPath.lineTo(centerGX + radius * (float)Math.cos(thetaG3),
                                 centerGY + radius * (float)Math.sin(thetaG3));
    }

    public void action(EDirection moveDirection){
        if(!(field.isWall(pointX, pointY,moveDirection))){
            field.moveOffset(moveDirection);
            itemMgr.movePrintItem(moveDirection);
            pointX += moveDirection.getDx();
            pointY += moveDirection.getDy();
            stepCount++;
            itemMgr.activate(pointX, pointY);
        }
        setDirection(moveDirection);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.RED);
        if(path != null){
            canvas.drawPath(path,paint);
        }

        paint.setColor(Color.BLACK);
        canvas.drawCircle(centerGX, centerGY, radius, paint);
        if(goalPointerPath != null){
            paint.setColor(Color.GREEN);
            canvas.drawPath(goalPointerPath,paint);
        }
    }
}
