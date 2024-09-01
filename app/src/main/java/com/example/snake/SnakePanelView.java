package com.example.snake;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakePanelView extends View {

  private final static String TAG = SnakePanelView.class.getSimpleName();
  public static boolean DEBUG = true;
  private int foodCount = 0;  // Compteur de nourritures attaquées
  private long startTime;
  private long elapsedTime = 0;

  private List<List<GridSquare>> mGridSquare = new ArrayList<>();
  private List<GridPosition> mSnakePositions = new ArrayList<>();

  private GridPosition mSnakeHeader; // Position de la tête du serpent
  private GridPosition mFoodPosition; // Position de la nourriture
  private int mSnakeLength = 3;
  private long mSpeed = 6;
  private int mSnakeDirection = GameTypeSnake.RIGHT;
  private boolean mIsEndGame = false;
  private int mGridSize = 17;
  private Paint mGridPaint = new Paint();
  private Paint mStrokePaint = new Paint();
  private int mRectSize = dp2px(getContext(), 23);

  private int mStartX, mStartY;

  public SnakePanelView(Context context) {
    this(context, null);
  }

  public SnakePanelView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SnakePanelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    List<GridSquare> squares;
    for (int i = 0; i < mGridSize; i++) {
      squares = new ArrayList<>();
      for (int j = 0; j < mGridSize; j++) {
        squares.add(new GridSquare(GameTypeSnake.GRID));
      }
      mGridSquare.add(squares);
    }
    mSnakeHeader = new GridPosition(10, 10);
    mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
    mFoodPosition = new GridPosition(5, 7);
    mIsEndGame = true;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mStartX = w / 2 - mGridSize * mRectSize / 2;
    mStartY = dp2px(getContext(), 20);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int height = mStartY + mGridSize * mRectSize;
    setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
  }


  public void startGame() {
    mIsEndGame = false;
    mSnakePositions.clear();
    mSnakeLength = 1;
    foodCount = 0;
    elapsedTime = 0;
    startTime = System.currentTimeMillis(); // Initialiser le temps de départ
    //generateSnake();
    generateFood();
    invalidate();
  }


  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawColor(Color.WHITE);
    startTime = System.currentTimeMillis();

    mGridPaint.reset();
    mGridPaint.setAntiAlias(true);
    mGridPaint.setStyle(Paint.Style.FILL);

    mStrokePaint.reset();
    mStrokePaint.setColor(Color.BLACK);
    mStrokePaint.setStyle(Paint.Style.STROKE);
    mStrokePaint.setAntiAlias(true);





    // Dessiner la grille
    for (int i = 0; i < mGridSize; i++) {
      for (int j = 0; j < mGridSize; j++) {
        int left = mStartX + i * mRectSize;
        int top = mStartY + j * mRectSize;
        int right = left + mRectSize;
        int bottom = top + mRectSize;
        canvas.drawRect(left, top, right, bottom, mStrokePaint);
        mGridPaint.setColor(mGridSquare.get(i).get(j).getColor());
        canvas.drawRect(left, top, right, bottom, mGridPaint);
      }
    }

    // Dessiner le serpent
    mGridPaint.setColor(Color.GREEN);
    for (GridPosition position : mSnakePositions) {
      int left = mStartX + position.getX() * mRectSize;
      int top = mStartY + position.getY() * mRectSize;
      int right = left + mRectSize;
      int bottom = top + mRectSize;
      canvas.drawRect(left, top, right, bottom, mGridPaint);
    }

    // Dessiner la nourriture
    mGridPaint.setColor(Color.RED);
    int foodLeft = mStartX + mFoodPosition.getX() * mRectSize;
    int foodTop = mStartY + mFoodPosition.getY() * mRectSize;
    int foodRight = foodLeft + mRectSize;
    int foodBottom = foodTop + mRectSize;
    canvas.drawRect(foodLeft, foodTop, foodRight, foodBottom, mGridPaint);

    // Afficher le nombre de nourritures consommées
    mGridPaint.setColor(Color.BLACK);
    mGridPaint.setTextSize(40);
    String foodCountText = "Food Count: " + foodCount;
    canvas.drawText(foodCountText, mStartX, mStartY - 20, mGridPaint);


    float foodCountTextWidth = mGridPaint.measureText(foodCountText);
  }








  private void refreshFood(GridPosition foodPosition) {
    // Réinitialiser la case de la nourriture précédente
    mGridSquare.get(mFoodPosition.getX()).get(mFoodPosition.getY()).setType(GameTypeSnake.GRID);
    // Mettre à jour la case de la nouvelle nourriture
    mGridSquare.get(foodPosition.getX()).get(foodPosition.getY()).setType(GameTypeSnake.FOOD);
  }


  public void setSpeed(long speed) {
    mSpeed = speed;
  }

  public void setGridSize(int gridSize) {
    mGridSize = gridSize;
  }

  public void setSnakeDirection(int snakeDirection) {
    if (mSnakeDirection == GameTypeSnake.RIGHT && snakeDirection == GameTypeSnake.LEFT) return;
    if (mSnakeDirection == GameTypeSnake.LEFT && snakeDirection == GameTypeSnake.RIGHT) return;
    if (mSnakeDirection == GameTypeSnake.TOP && snakeDirection == GameTypeSnake.BOTTOM) return;
    if (mSnakeDirection == GameTypeSnake.BOTTOM && snakeDirection == GameTypeSnake.TOP) return;
    mSnakeDirection = snakeDirection;
  }

  private class GameMainThread extends Thread {

    @Override
    public void run() {
      while (!mIsEndGame) {
        moveSnake(mSnakeDirection);
        checkCollision();
        refreshGridSquare();
        handleSnakeTail();
        postInvalidate();
        handleSpeed();
      }
    }

    private void handleSpeed() {
      try {
        sleep(1500 / mSpeed);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }


  private void checkCollision() {
    GridPosition headerPosition = mSnakePositions.get(mSnakePositions.size() - 1);
    for (int i = 0; i < mSnakePositions.size() - 2; i++) {
      GridPosition position = mSnakePositions.get(i);
      if (headerPosition.getX() == position.getX() && headerPosition.getY() == position.getY()) {
        mIsEndGame = true;
        showMessageDialog();
        return;
      }
    }

    if (headerPosition.getX() == mFoodPosition.getX()
            && headerPosition.getY() == mFoodPosition.getY()) {
      mSnakeLength++;
      foodCount++; // Incrémenter le compteur de nourritures consommées
      generateFood();
    }
  }



  private void showMessageDialog() {
    post(new Runnable() {
      @Override
      public void run() {
        new AlertDialog.Builder(getContext())
                .setMessage("Game Over! Food attaquée: " + foodCount)  // Afficher le nombre de nourritures attaquées
                .setCancelable(false)
                .setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    reStartGame();
                  }
                })
                .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                  }
                })
                .create()
                .show();
      }
    });
  }


  public void reStartGame() {
    if (!mIsEndGame) return;

    // Réinitialiser la grille
    for (List<GridSquare> squares : mGridSquare) {
      for (GridSquare square : squares) {
        square.setType(GameTypeSnake.GRID);
      }
    }

    // Réinitialiser le serpent
    if (mSnakeHeader != null) {
      mSnakeHeader.setX(10);
      mSnakeHeader.setY(10);
    } else {
      mSnakeHeader = new GridPosition(10, 10);
    }
    mSnakePositions.clear();
    mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
    mSnakeLength = 3;
    mSnakeDirection = GameTypeSnake.RIGHT;
    mSpeed = 8;

    foodCount = 0;  // Réinitialiser le compteur
    generateFood();

    // Démarrer le fil de jeu
    mIsEndGame = false;
    GameMainThread thread = new GameMainThread();
    thread.start();
  }



  private void generateFood() {
    Random random = new Random();
    int foodX, foodY;
    boolean isValidPosition;

    do {
      foodX = random.nextInt(mGridSize);
      foodY = random.nextInt(mGridSize);
      isValidPosition = true;

      // Vérifier si la nourriture n'est pas sur le serpent
      for (GridPosition position : mSnakePositions) {
        if (foodX == position.getX() && foodY == position.getY()) {
          isValidPosition = false;
          break;
        }
      }
    } while (!isValidPosition);

    mFoodPosition.setX(foodX);
    mFoodPosition.setY(foodY);
    refreshFood(mFoodPosition);
  }


  private void moveSnake(int snakeDirection) {
    switch (snakeDirection) {
      case GameTypeSnake.LEFT:
        if (mSnakeHeader.getX() - 1 < 0) {
          mSnakeHeader.setX(mGridSize - 1);
        } else {
          mSnakeHeader.setX(mSnakeHeader.getX() - 1);
        }
        mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
        break;
      case GameTypeSnake.TOP:
        if (mSnakeHeader.getY() - 1 < 0) {
          mSnakeHeader.setY(mGridSize - 1);
        } else {
          mSnakeHeader.setY(mSnakeHeader.getY() - 1);
        }
        mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
        break;
      case GameTypeSnake.RIGHT:
        if (mSnakeHeader.getX() + 1 >= mGridSize) {
          mSnakeHeader.setX(0);
        } else {
          mSnakeHeader.setX(mSnakeHeader.getX() + 1);
        }
        mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
        break;
      case GameTypeSnake.BOTTOM:
        if (mSnakeHeader.getY() + 1 >= mGridSize) {
          mSnakeHeader.setY(0);
        } else {
          mSnakeHeader.setY(mSnakeHeader.getY() + 1);
        }
        mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
        break;
    }
  }

  private void refreshGridSquare() {
    for (GridPosition position : mSnakePositions) {
      mGridSquare.get(position.getX()).get(position.getY()).setType(GameTypeSnake.SNAKE);
    }
  }

  private void handleSnakeTail() {
    int snakeLength = mSnakeLength;
    for (int i = mSnakePositions.size() - 1; i >= 0; i--) {
      if (snakeLength > 0) {
        snakeLength--;
      } else {
        GridPosition position = mSnakePositions.get(i);
        mGridSquare.get(position.getX()).get(position.getY()).setType(GameTypeSnake.GRID);
      }
    }
    snakeLength = mSnakeLength;
    for (int i = mSnakePositions.size() - 1; i >= 0; i--) {
      if (snakeLength > 0) {
        snakeLength--;
      } else {
        mSnakePositions.remove(i);
      }
    }
  }

  public static int dp2px(Context context, float dpVal) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
            context.getResources().getDisplayMetrics());
  }
}
