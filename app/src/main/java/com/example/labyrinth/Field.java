package com.example.labyrinth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.BoringLayout;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Created by thiro on 2018/03/15.
 */

public class Field implements IField,IDrawer{
    public static final int DEFAULT_SIZE_X = 10;
    public static final int DEFAULT_SIZE_Y = 10;
    private static final int DEFAULT_START_X = 0;
    private static final int DEFAULT_START_Y = 0;
    private static final int INIT_REPEAT = 5;
    private int shortestDistance;
    private int effectiveSize;
    private final boolean isRandomMode;
    private final int sizeX;
    private final int sizeY;
    private int startX = DEFAULT_START_X;
    private int startY = DEFAULT_START_Y;
    private int goalX;
    private int goalY;
    private int offsetX;
    private int offsetY;
    private int repeatCount = INIT_REPEAT;
    private int wallCount = 0;
    private List<List<Boolean>> depthWall;
    private List<List<Boolean>> widthWall;
    private List<List<Boolean>> effectiveField;
    private float[] pts;
    private IDisplaySize displaySize;

    public Field(IDisplaySize displaySize, int sizeX, int sizeY, boolean isRandomMode){
        this.displaySize = displaySize;
        this.isRandomMode = isRandomMode;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        goalX = sizeX - 1;
        goalY = sizeY - 1;
        offsetX = displaySize.getDefaultOffsetX();
        offsetY = displaySize.getDefaultOffsetY();

        depthWall = new ArrayList<>(sizeX + 1);
        for(int i = 0; i < sizeX + 1; i++){
            List<Boolean> tmp = new ArrayList<>(sizeY);
            for(int j = 0; j < sizeY; j++){
                tmp.add(false);
            }
            depthWall.add(tmp);
        }

        widthWall = new ArrayList<>(sizeY + 1);
        for(int i = 0; i < sizeY + 1; i++){
            List<Boolean> tmp = new ArrayList<>(sizeX);
            for(int j = 0; j < sizeX; j++){
                tmp.add(false);
            }
            widthWall.add(tmp);
        }

        effectiveField = new ArrayList<>(sizeX);
        for(int i = 0; i < sizeX; i++){
            List<Boolean> tmp = new ArrayList<>(sizeY);
            for(int j = 0; j < sizeY; j++){
                tmp.add(false);
            }
            effectiveField.add(tmp);
        }

        initialize();
    }

    public int getEffectiveSize(){return effectiveSize;}
    public int getShortestDistance(){return shortestDistance;}
    public int getMinShortest(){
        if(sizeX >= 50){
            return ((sizeX/50)*64);
        }
        else{
            return (sizeX+sizeY);
        }
    }
    @Override
    public int getSizeX(){return sizeX;}
    @Override
    public int getSizeY(){return sizeY;}
    @Override
    public int getStartX(){return startX;}
    @Override
    public int getStartY(){return startY;}

    @Override
    public int getGoalX() {
        return goalX;
    }

    @Override
    public int getGoalY() {
        return goalY;
    }

    @Override
    public int getOffsetX(){
        return (displaySize.getDefaultOffsetX() - offsetX * displaySize.getCellSize());
    }
    @Override
    public int getOffsetY(){
        return (displaySize.getDefaultOffsetY() - offsetY * displaySize.getCellSize());
    }

    @Override
    public void moveOffset(EDirection direction){
        offsetX += direction.getDx();
        offsetY += direction.getDy();

        float[] pts2 = pts.clone();
        int dx = -direction.getDx() * displaySize.getCellSize();
        int dy = -direction.getDy() * displaySize.getCellSize();
        for(int i = 0; i < pts2.length; i++){
            if((i % 2) == 0){//if it is x
                pts2[i] += dx;
            }
            else{
                pts2[i] += dy;
            }
        }
        pts = pts2.clone();
    }

    private boolean canReachGoalFromStart(){
        class Node{
            private int x;
            private int y;
            private int distance;

            private Node(int x, int y, int distance){
                this.x = x;
                this.y = y;
                this.distance = distance;
            }
        }

        Queue<Node> queue = new ArrayDeque<>();
        List<Node> goalList = new ArrayList<>();
        boolean hasReachedGoal = false;
        Random random = new Random();

        //initialize effective field
        for(int i = 0; i < effectiveField.size(); i++){
            for(int j = 0; j < effectiveField.get(i).size(); j++){
                effectiveField.get(i).set(j,false);
            }
        }

        if(isRandomMode){
            startX = random.nextInt(sizeX);
            startY = random.nextInt(sizeY);
        }

        queue.add(new Node(startX, startY,0));
        effectiveField.get(startX).set(startY, true);
        effectiveSize = 1;
        while(!queue.isEmpty()){
            Node node = queue.remove();
            EDirection[] directions = EDirection.values();
            for(EDirection d : directions){
                int x = adjustXToDomain(node.x + d.getDx());
                int y = adjustYToDomain(node.y + d.getDy());
                if(!(effectiveField.get(x).get(y)) && !(isWall(node.x,node.y,d))){
                    Node newNode = new Node(x, y, node.distance + 1);
                    queue.add(newNode);
                    effectiveField.get(x).set(y, true);
                    effectiveSize++;
                    if(isRandomMode){
                        if(newNode.distance >= getMinShortest()){
                            goalList.add(newNode);
                        }
                    }
                    else if(!hasReachedGoal && isGoal(x,y)){
                        shortestDistance = newNode.distance;
                        hasReachedGoal = true;
                    }
                }
            }
        }

        if(isRandomMode){
            if(goalList.size() > 0){
                Node goal = goalList.get(random.nextInt(goalList.size()));
                goalX = goal.x;
                goalY = goal.y;
                shortestDistance = goal.distance;
                hasReachedGoal = true;
            }
            else if(repeatCount > 0){
                repeatCount--;
                hasReachedGoal = !(canReachGoalFromStart());
                repeatCount = INIT_REPEAT;
            }
        }

        return (!hasReachedGoal);
    }

    @Override
    public boolean isWall(int x, int y, EDirection direction){
        switch (direction){
            case DOWN:
                return widthWall.get(y + 1).get(x);
            case UP:
                return widthWall.get(y).get(x);
            case LEFT:
                return depthWall.get(x).get(y);
            case RIGHT:
                return depthWall.get(x + 1).get(y);
            default:
                return true;
        }
    }

    @Override
    public boolean isEffective(int x, int y){
        return (effectiveField.get(x).get(y));
    }

    @Override
    public boolean isGoal(int x, int y){
        return ((x == goalX) && (y == goalY));
    }

    private int adjustXToDomain(int x){
        if(x < 0){
            return 0;
        }
        else if(x >= sizeX){
            return (sizeX - 1);
        }
        return x;
    }

    private int adjustYToDomain(int y){
        if(y < 0){
            return 0;
        }
        else if(y >= sizeY){
            return (sizeY - 1);
        }
        return y;
    }

    public void initScale(){
        pts = new float[wallCount*4];
        int count = 0;
        for(int i = 0; i < depthWall.size(); i++){
            for(int j = 0; j < depthWall.get(i).size(); j++){
                if(depthWall.get(i).get(j)){
                    float x = i * displaySize.getCellSize() + getOffsetX();
                    float y = j * displaySize.getCellSize() + getOffsetY();
                    pts[count] = x;
                    count++;
                    pts[count] = y;
                    count++;
                    pts[count] = x;
                    count++;
                    pts[count] = y + displaySize.getCellSize();
                    count++;
                }
            }
        }
        for(int i = 0; i < widthWall.size(); i++){
            for(int j = 0; j < widthWall.get(i).size(); j++){
                if(widthWall.get(i).get(j)){
                    float x = j * displaySize.getCellSize() + getOffsetX();
                    float y = i * displaySize.getCellSize() + getOffsetY();
                    pts[count] = x;
                    count++;
                    pts[count] = y;
                    count++;
                    pts[count] = x + displaySize.getCellSize();
                    count++;
                    pts[count] = y;
                    count++;
                }
            }
        }
    }

    public void initialize(){
        Random random = new Random();
        //generate field
        do{
            wallCount = 0;
            //set wall
            for(int i = 0; i < depthWall.size(); i++){
                for(int j = 0; j < depthWall.get(i).size(); j++){
                    if(i == 0 || i == depthWall.size() - 1){
                        //the outermost wall is absolutely true
                        depthWall.get(i).set(j,true);
                        wallCount++;
                    }
                    else{
                        if(random.nextBoolean()){
                            depthWall.get(i).set(j, true);
                            wallCount++;
                        }
                        else{
                            depthWall.get(i).set(j, false);
                        }
                    }
                }
            }
            for(int i = 0; i < widthWall.size(); i++){
                for(int j = 0; j < widthWall.get(i).size(); j++){
                    if(i == 0 || i == widthWall.size() - 1){
                        //the outermost wall is absolutely true
                        widthWall.get(i).set(j,true);
                        wallCount++;
                    }
                    else{
                        if(random.nextBoolean()){
                            widthWall.get(i).set(j, true);
                            wallCount++;
                        }
                        else{
                            widthWall.get(i).set(j, false);
                        }
                    }
                }
            }

        }while(canReachGoalFromStart());

        offsetX = startX;
        offsetY = startY;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        if(pts != null){
            canvas.drawLines(pts,paint);
        }
        paint.setColor(Color.BLUE);
        canvas.drawCircle(goalX * displaySize.getCellSize() + displaySize.getCellSize2() + getOffsetX(),
                          goalY * displaySize.getCellSize() + displaySize.getCellSize2() + getOffsetY(), displaySize.getCellSize2() * 4 / 5, paint);
    }
}
