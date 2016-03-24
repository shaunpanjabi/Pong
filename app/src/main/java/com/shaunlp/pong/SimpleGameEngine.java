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

public class SimpleGameEngine extends Activity {
    GameView gameView;

    public class GameView extends SurfaceView implements Runnable {
        Thread gameThread = null;

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

        public GameView(Context context) {
            super(context);

            ourHolder = getHolder();
            paint = new Paint();

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            screenX = size.x;
            screenY = size.y;

            paddle = new Paddle(screenX, screenY);
            paddle2 = new Paddle(screenX, 20);
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
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    paused = false;
                    Paddle piddler;
                    if (motionEvent.getY() > screenY / 2 ) {
                        piddler = paddle;
                    } else {
                        piddler = paddle2;
                    }
                    if (motionEvent.getX() > screenX / 2) {
//                        if (piddler.getRect().right >
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

            if(ball.getRect().left < 0 || ball.getRect().right > screenX){
                ball.reverseXVelocity();
            }

            if (ball.getRect().top < 0) {
                ball.reverseYVelocity();
            }

            if (RectF.intersects(paddle.getRect(), ball.getRect()) || RectF.intersects(paddle2.getRect(), ball.getRect())){
                ball.reverseYVelocity();
            }

            if(ball.getRect().bottom > screenY){
                p1_score += 1;
                ball.reset(screenX, screenY);
            }
            if(ball.getRect().top < 0){
                p2_score += 1;
                ball.reset(screenX, screenY);
            }

            ball.update(fps);

        }

        public void draw() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 0, 0, 0));

                paint.setColor(Color.argb(255, 255, 255, 255));

                // draw paddle
                canvas.drawRect(paddle.getRect(), paint);
                canvas.drawRect(paddle2.getRect(), paint);
                canvas.drawRect(ball.getRect(), paint);

                canvas.drawRect(0, screenY/2 - 10, screenX, screenY/2 + 10, paint);
                canvas.drawCircle(screenX / 2, screenY / 2, 150, paint);
                paint.setColor(Color.argb(255, 0, 0, 0));
                canvas.drawCircle(screenX / 2, screenY / 2, 130, paint);
                paint.setColor(Color.argb(255, 255, 255, 255));

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
