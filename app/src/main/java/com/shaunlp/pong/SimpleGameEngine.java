package com.shaunlp.pong;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

// TODO: Add multitouch support
// TODO: fix edge paddle collision
// TODO: Add A.I

public class SimpleGameEngine extends Activity {
    GameView gameView;

    public class GameView extends SurfaceView implements Runnable {
        private Thread gameThread = null;
        SurfaceHolder ourHolder;

        volatile boolean playing;

        boolean paused = true;

        Canvas canvas;
        Paint paint;

        long fps;
        private long timeThisFrame;
        int screenX;

        int screenY;
        int p1_score;

        int p2_score;
        Paddle paddle;
        Paddle paddle2;

        Ball ball;

        double paddingY;

        // Settings
        boolean debug = true;
        boolean rainbowBall = false;
        boolean hapticFeedback = true;

        public GameView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            screenX = size.x;
            screenY = size.y;

            paddingY = (double) size.y * (1.0 - (0.8)) / 2;

            paddle2 = new Paddle(screenX, screenY, screenX/2, (int) paddingY); // top
            paddle = new Paddle(screenX, screenY, screenX / 2, screenY - (int) paddle2.getHeight() - (int) paddingY); // bottom
            paddle2.ai = true;
            paddle.ai = false;
            ball = new Ball(screenX, screenY);

            p1_score = 0;
            p2_score = 0;

            restart();

        }

        public void restart() {
            ball.reset(screenX, screenY);
        }

        @Override
        public void run() {
            while (playing) {
                long startFrameTime = System.currentTimeMillis();

                if (!paused) {
                    update();
                }

                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    paused = false;
                    Paddle piddler;
                    if (motionEvent.getY() > screenY / 2 ) {
                        piddler = paddle;
                    } else {
                        piddler = paddle2;
                    }

                    if (!piddler.ai) {
                        if (motionEvent.getX() > piddler.getRect().centerX()) {
                            piddler.setMovementState(piddler.RIGHT);
                        } else {
                            piddler.setMovementState(piddler.LEFT);
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    paddle.setMovementState(paddle.STOPPED);
                    paddle2.setMovementState(paddle.STOPPED);
                    break;
            }
            return true;
        }

        public void update() {
            paddle.update(fps);
            paddle2.update(fps);

            if (paddle2.ai) {
                if (ball.getRect().centerX() > paddle2.getRect().centerX()) {
                    paddle2.setMovementState(paddle2.RIGHT);
                    } else if (ball.getRect().centerX() < paddle2.getRect().centerX()) {
                    paddle2.setMovementState(paddle2.LEFT);
                } else {
                    paddle2.setMovementState(paddle2.STOPPED);
                }
            }

            if (paddle.ai) {
                if (ball.getRect().centerX() > paddle.getRect().centerX()) {
                    paddle.setMovementState(paddle.RIGHT);
                } else {
                    paddle.setMovementState(paddle.LEFT);
                }
            }

            if(ball.getRect().left < 0){
                ball.reverseXVelocity();
                ball.clearObstacleX(ball.getRect().width());
            }

            if(ball.getRect().right > screenX){
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - ball.getRect().width());
            }

            if(ball.getRect().bottom >= paddle.getRect().top){
                if(ball.getRect().right >= paddle.getRect().left && ball.getRect().left <= paddle.getRect().right) {
                    ball.setNegativeYVelocity();
                    ball.clearObstacleY(paddle.getRect().top);
                    ball.speedUpYVelocity((float) 0.1);
                } else {
                    p1_score += 1;
                    ball.reset(screenX, screenY);
                }
            }

            if(ball.getRect().top <= paddle2.getRect().bottom){
                if(ball.getRect().right >= paddle2.getRect().left && ball.getRect().left <= paddle2.getRect().right) {
                    ball.setPositiveYVelocity();
                    ball.clearObstacleY(paddle2.getRect().bottom + paddle2.getHeight());
                    ball.speedUpYVelocity((float) 0.1);
                } else {
                    p2_score += 1;
                    p2_score += 1;
                    ball.reset(screenX, screenY);
                }
            }

            ball.update(fps);

        }

        public void draw() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();

                // draw black background
                canvas.drawColor(Colors.WHITE.getColor());


                // draw paddle
                paint.setColor(Colors.HS_ORANGE.getColor());

                // draw arena
                canvas.drawRect(0, screenY / 2 - 10, screenX, screenY / 2 + 10, paint);
                canvas.drawCircle(screenX / 2, screenY / 2, 150, paint);
                paint.setColor(Colors.WHITE.getColor());
                canvas.drawCircle(screenX / 2, screenY / 2, 130, paint);


                // draw ball
                if (rainbowBall) {
                    paint.setColor(Colors.getRandomColor());
                } else {
                    paint.setColor(Colors.HS_ORANGE.getColor());
                }
                canvas.drawRect(ball.getRect(), paint);

                // draw paddles
                paint.setColor(Colors.HS_ORANGE.getColor());
                canvas.drawRect(paddle.getRect(), paint);
                canvas.drawRect(paddle2.getRect(), paint);

                // black layer covers ball if it goes below paddle
                paint.setColor(Colors.WHITE.getColor());
                canvas.drawRect(new RectF(paddle2.getRect().left, 0, paddle2.getRect().right, paddle2.getRect().top), paint);
                canvas.drawRect(new RectF(paddle.getRect().left, paddle.getRect().bottom, paddle.getRect().right, screenY), paint);

                paint.setColor(Colors.GREEN.getColor());

                if (debug) {
                    paint.setTextSize(45);
                    canvas.drawText("FPS:" + fps, 20, 40, paint);
                }
                paint.setTextSize(100);
                canvas.drawText(Integer.toString(p1_score), 0, screenY / 2 - 30, paint);
                canvas.drawText(Integer.toString(p2_score), 0, screenY / 2 + 100, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }
        }

        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}
