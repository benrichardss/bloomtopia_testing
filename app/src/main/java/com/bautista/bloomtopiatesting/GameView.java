package com.bautista.bloomtopiatesting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;
    private int playerX = 520;  // Player's X position
    private int playerY = 2000;  // Player's Y position
    private int playerWidth = 40; // Player's width
    private int playerHeight = 60; // Player's height
    private int speed = 5;  // Initial movement speed for the player
    private int obstacleSpeed = 10; // Initial speed of obstacles
    private int score = 0; // Game score

    private int[] obstacleX = {180, 490, 800}; // X positions of obstacles
    private int[] obstacleY = {500, 1000, 1500}; // Y position of obstacles (same for all)
    private int obstacleWidth = 300;
    private int obstacleHeight = 300;

    private boolean gameOver = false;
    private long lastUpdateTime = System.currentTimeMillis(); // Used for scoring and timing

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    public void update() {
        if (gameOver) return;

        // Update obstacles' Y positions (move down)
        for (int i = 0; i < obstacleX.length; i++) {
            obstacleY[i] += obstacleSpeed;  // Move obstacles down

            // Reset obstacles to the top if they go off-screen (when they move past the bottom of the screen)
            if (obstacleY[i] > getHeight()) {
                obstacleY[i] = -obstacleHeight * (int) (Math.random() * 50);  // Reset to the top of the screen
            }

            // Check for collision with player
            if (new android.graphics.Rect(playerX, playerY, playerX + playerWidth, playerY + playerHeight)
                    .intersect(new android.graphics.Rect(obstacleX[i], obstacleY[i], obstacleX[i] + obstacleWidth, obstacleY[i] + obstacleHeight))) {
                gameOver = true;  // End game if player collides
            }
        }

        // Increase speed over time (both player and obstacles)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= 1000) {  // If 1 second has passed
            score++;  // Increase score every second

            // Increase player and obstacle speed gradually over time
            if (score % 10 == 0) {  // Every 10 seconds, increase speed
                speed += 1;  // Increase player speed
                obstacleSpeed += 1;  // Increase obstacle speed
            }

            lastUpdateTime = currentTime;  // Update the last update time
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            playerX = (int) event.getX() - playerWidth / 2;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        render(canvas);
    }

    public void render(Canvas canvas) {
        // Set background color
        canvas.drawColor(Color.CYAN);

        if (gameOver) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setTextSize(50);
            canvas.drawText("Game Over!", 250, 250, paint);
            paint.setTextSize(30);
            canvas.drawText("Score: " + score, 330, 300, paint);
            return;
        }

        // Draw player (a simple rectangle)
        Paint playerPaint = new Paint();
        playerPaint.setColor(Color.GREEN);
        canvas.drawRect(playerX, playerY, playerX + playerWidth, playerY + playerHeight, playerPaint);

        // Draw obstacles (red rectangles)
        Paint obstaclePaint = new Paint();
        obstaclePaint.setColor(Color.RED);
        for (int i = 0; i < obstacleX.length; i++) {
            canvas.drawRect(obstacleX[i], obstacleY[i], obstacleX[i] + obstacleWidth, obstacleY[i] + obstacleHeight, obstaclePaint);
        }

        // Draw score
        Paint scorePaint = new Paint();
        scorePaint.setColor(Color.BLACK);
        scorePaint.setTextSize(20);
        canvas.drawText("Score: " + score, 20, 30, scorePaint);
    }
}