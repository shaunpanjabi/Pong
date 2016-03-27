package com.shaunlp.pong;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Vibrator;

// TODO: Add multitouch support

public class SimpleGameEngine extends Activity {
    GameView gameView;
    Vibrator vibez;

    public class GameView extends SurfaceView implements Runnable {
        Thread gameThread = null;

        SurfaceHolder ourHolder;

        volatile boolean playing;

        boolean paused = true;
        boolean hapticFeedback = true;

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
                    if (motionEvent.getX() > piddler.getRect().centerX()) {
                        piddler.setMovementState(piddler.RIGHT);
                    } else {
                        piddler.setMovementState(piddler.LEFT);
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

            if(ball.getRect().left < 0){
                Log.e("Action: ", "Wall collision");
                ball.reverseXVelocity();
                ball.clearObstacleX(ball.getRect().width());

                if (hapticFeedback) {
                    vibez.vibrate(10);
                }
            }

            if(ball.getRect().right > screenX){
                Log.e("Action: ", "Wall collision");
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - ball.getRect().width());
                if (hapticFeedback) {
                    vibez.vibrate(10);
                }
            }

            if(ball.getRect().bottom > paddle.getRect().top){
                Log.e("Action: ", "In here 1");
                if(ball.getRect().centerX() <= paddle.getRect().right && ball.getRect().centerX() >= paddle.getRect().left) {
                    Log.e("Action: ", "Collision detected 3");
                    if (hapticFeedback) {
                        vibez.vibrate(10);
                    }
                    ball.setNegativeVelocity();
                    ball.clearObstacleY(paddle.getRect().top);
                } else {
                    p1_score += 1;
                    if (hapticFeedback) {
                        vibez.vibrate(10);
                    }
                    ball.reset(screenX, screenY);
                }
            }

            if(ball.getRect().top <= paddle2.getRect().bottom){
                if(ball.getRect().centerX() <= paddle2.getRect().right && ball.getRect().centerX() >= paddle2.getRect().left) {
                    Log.e("Action: ", "Collision detected 4");
                    if (hapticFeedback) {
                        vibez.vibrate(10);
                    }
                    ball.setPositiveVelocity();
                    ball.clearObstacleY(paddle2.getRect().bottom + paddle2.getHeight());

                } else {
                    p2_score += 1;
                    if (hapticFeedback) {
                        vibez.vibrate(10);
                    }
                    ball.reset(screenX, screenY);
                }
            }

            ball.update(fps);

        }

        public void draw() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 0, 0, 0));

                paint.setColor(Color.argb(255, 255, 255, 255));

                // draw paddle
                // 1080 x 1776
                canvas.drawRect(paddle.getRect(), paint);
                canvas.drawRect(paddle2.getRect(), paint);

                canvas.drawRect(0, screenY/2 - 10, screenX, screenY/2 + 10, paint);
                canvas.drawCircle(screenX / 2, screenY / 2, 150, paint);
                paint.setColor(Color.argb(255, 0, 0, 0));
                canvas.drawCircle(screenX / 2, screenY / 2, 130, paint);
                paint.setColor(Color.argb(255, 255, 255, 255));

                canvas.drawRect(ball.getRect(), paint);

                paint.setTextSize(45);

                canvas.drawText("FPS:" + fps, 20, 40, paint);
                paint.setTextSize(100);
                canvas.drawText(Integer.toString(p1_score), 0, screenY/2-30, paint);
                canvas.drawText(Integer.toString(p2_score), 0, screenY/2+100, paint);

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
        vibez = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
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
