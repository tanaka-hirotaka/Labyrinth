package com.example.labyrinth;

/**
 * Created by thiro on 2018/03/15.
 */

public enum EDirection {
    RIGHT(0, "RIGHT"),
    UP(1, "UP"),
    LEFT(2, "LEFT"),
    DOWN(3, "DOWN");

    private final int id;
    private final double theta;
    private final String name;

    EDirection(int id, String name){
        this.id = id;
        this.name = name;
        this.theta = (Math.PI / 2) * id;
    }

    public double getTheta(){return theta;}
    public int getDx(){
        switch (this){
            case RIGHT:
                return 1;
            case LEFT:
                return -1;
            default:
                return 0;
        }
    }

    @Override
    public String toString(){
        return name;
    }

    public int getDy(){
        switch (this){
            case DOWN:
                return 1;
            case UP:
                return -1;
            default:
                return 0;
        }
    }

    public int getId(){return id;}

    public EDirection getBack(){
        EDirection directions[] = EDirection.values();
        for(EDirection d : directions){
            int length =  directions.length;
            if(d.getId() == (id +  length/2) % length){
                return d;
            }
        }
        return null;
    }
}
