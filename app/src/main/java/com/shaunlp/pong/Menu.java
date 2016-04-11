package com.shaunlp.pong;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.ArrayList;
import java.util.Random;

// TODO: Fix soundpool lag
// TODO: Add start button
// TODO: Tap, skips intro

public class Menu extends Activity {

    MenuView menuView;
    SoundPool sp;

    public class MenuView extends SurfaceView implements Runnable {

        Thread gameThread = null;
        private long timeThisFrame;
        volatile boolean playing;
        boolean paused;
        long fps=60;

        SurfaceHolder ourHolder;

        Canvas canvas;
        Paint paint;

        int screenX;
        int screenY;

        ArrayList<Ball> ballBuff;

        float panCount;

        Random generator = new Random();

        Rect textContainer;

        public MenuView(Context context) {
            super(context);
            paused = false;

            ourHolder = getHolder();
            paint = new Paint();
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            screenX = size.x;
            screenY = size.y;

            panCount = screenY;

            ballBuff = new ArrayList<>();

            for (int i=0; i <= 30; i++ ) {
                ballBuff.add(new Ball(screenX, screenY));
                ballBuff.get(i).setRandomVelocity();
            }

            restart();
        }

        @Override
        public void run() {
            while (playing) {
                long startFrameTime = System.currentTimeMillis();
                if (!paused) {
                    update();
                    draw();
                }
                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void update() {

            for (int i=0; i < ballBuff.size(); i++) {
                ballBuff.get(i).update(fps);

                if(ballBuff.get(i).getRect().left < 0){
                    ballBuff.get(i).reverseXVelocity();
                    ballBuff.get(i).clearObstacleX(ballBuff.get(i).getRect().width());
                }

                if(ballBuff.get(i).getRect().right > screenX){
                    ballBuff.get(i).reverseXVelocity();
                    ballBuff.get(i).clearObstacleX(screenX - ballBuff.get(i).getRect().width());
                }

                if(ballBuff.get(i).getRect().top < 0){
                    ballBuff.get(i).setPositiveYVelocity();
//                ball.clearObstacleY(1 + ball.getRect().height());
                }

                if(ballBuff.get(i).getRect().bottom > screenY){
                    ballBuff.get(i).reverseYVelocity();
//                ball.clearObstacleY(ball.getRect().height());
                }
            }

        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            int touchX = (int) motionEvent.getX();
            int touchY = (int) motionEvent.getY();

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    panCount = 499;

                    for (int i=0; i < ballBuff.size(); i++) {
                        ballBuff.get(i).update(fps);
                        ballBuff.get(i).setxVelocity(0);
                        ballBuff.get(i).setyVelocity(1000);
                    }

                    if (textContainer.contains(touchX, touchY)) {
                        Intent gameIntent = new Intent(Menu.this, SimpleGameEngine.class);
                        Menu.this.startActivity(gameIntent);
                    }

//                    if (!paused) {
//                        paused = true;
//                        Intent gameIntent = new Intent(Menu.this, SimpleGameEngine.class);
//                        Menu.this.startActivity(gameIntent);
//                    } else {
//                        paused = false;
//                    }
            }
            return true;
        }

        public void draw() {
            if (ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 0, 0, 0));

                paint.setColor(Color.argb(255, 255, 255, 255));

                for (int i=0; i < ballBuff.size(); i++) {
                    int red = generator.nextInt(255);
                    int green = generator.nextInt(255);
                    int blue = generator.nextInt(255);
                    paint.setColor(Color.argb(255, red, green, blue));
                    canvas.drawRect(ballBuff.get(i).getRect(), paint);
                }

//                canvas.drawText("FPS:" + fps, 20, 40, paint);
                paint.setTextSize(150);

                Rect bounds =  new Rect();
                paint.getTextBounds("START", 0, "START".length(), bounds);
                float mTextWidth = paint.measureText("START");
                float mTextHeight = bounds.height();
                textContainer = new Rect(
                        (int) ((screenX/2)-(mTextWidth/2)),
                        (int) ((screenY*(float) 0.8)-(mTextHeight)),
                        (int) ((screenX/2)+(mTextWidth/2)),
                        (int) ((screenY*0.8)));


                paint.setTextAlign(Paint.Align.CENTER);
                if (panCount > 500.0) {
                    panCount -= 5;
                } else {
                    canvas.drawRect(textContainer, paint);
                    paint.setColor(Color.argb(255, 0, 255, 0));
                    canvas.drawText("START", screenX / 2, screenY * (float) 0.80, paint);
                }
                paint.setTextSize(500);
                canvas.drawText("PONG", screenX/2, panCount, paint);
                int red = generator.nextInt(255);
                int green = generator.nextInt(255);
                int blue = generator.nextInt(255);
                paint.setColor(Color.argb(255, red, green, blue));
                canvas.drawText("PONG", screenX/2 + 10, panCount + 10, paint);

                ourHolder.unlockCanvasAndPost(canvas);

            }

        }

        public void restart() {
            for (int i=0; i < ballBuff.size(); i++) {
            ballBuff.get(i).reset(screenX, screenY);
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
        menuView = new MenuView(this);
        setContentView(menuView);
        sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
//        sp.setOnLoadCompleteListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        menuView.resume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        menuView.pause();
    }

}
