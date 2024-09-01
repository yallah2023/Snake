package com.example.snake;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

public class MainActivitySnake extends AppCompatActivity implements View.OnClickListener {

  private com.example.snake.SnakePanelView mSnakePanelView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_snake);

    mSnakePanelView = findViewById(R.id.snake_view);

    findViewById(R.id.left_btn).setOnClickListener(this);
    findViewById(R.id.right_btn).setOnClickListener(this);
    findViewById(R.id.top_btn).setOnClickListener(this);
    findViewById(R.id.bottom_btn).setOnClickListener(this);
    findViewById(R.id.start_btn).setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.left_btn) {
      mSnakePanelView.setSnakeDirection(com.example.snake.GameTypeSnake.LEFT);
    } else if (v.getId() == R.id.right_btn) {
      mSnakePanelView.setSnakeDirection(com.example.snake.GameTypeSnake.RIGHT);
    } else if (v.getId() == R.id.top_btn) {
      mSnakePanelView.setSnakeDirection(GameTypeSnake.TOP);
    } else if (v.getId() == R.id.bottom_btn) {
      mSnakePanelView.setSnakeDirection(GameTypeSnake.BOTTOM);
    } else if (v.getId() == R.id.start_btn) {
      mSnakePanelView.reStartGame();
    }

    /* findViewById(R.id.go_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivitySnake.this, LancementActivityFly.class));
            }
        });*/
  }

}
