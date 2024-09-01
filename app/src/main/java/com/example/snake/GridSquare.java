package com.example.snake;

import android.graphics.Color;

public class GridSquare {
  private int mType;

  public GridSquare(int type) {
    mType = type;
  }

  public int getColor() {
    switch (mType) {
      case GameTypeSnake.GRID://空格子
        return Color.WHITE;
      case GameTypeSnake.FOOD://食物
        return Color.BLUE;
      case GameTypeSnake.SNAKE://蛇
        return Color.parseColor("#FF4081");
    }
    return Color.WHITE;
  }

  public void setType(int type) {
    mType = type;
  }
}
