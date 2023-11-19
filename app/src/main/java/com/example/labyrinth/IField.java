package com.example.labyrinth;

/**
 * Created by thiro on 2018/03/15.
 */

public interface IField {
    int getSizeX();
    int getSizeY();
    int getStartX();
    int getStartY();
    int getGoalX();
    int getGoalY();
    int getOffsetX();
    int getOffsetY();
    int getEffectiveSize();
    boolean isEffective(int x, int y);
    boolean isGoal(int x, int y);
    void moveOffset(EDirection direction);
    boolean isWall(int x, int y, EDirection direction);
}
