package com.example.labyrinth;

/**
 * Created by thiro on 2018/03/18.
 */

public interface IItemMgr {
    void activate(int x, int y);
    void movePrintItem(EDirection direction);
}
