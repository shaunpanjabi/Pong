package com.shaunlp.pong;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.SurfaceView;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.ArrayList;
import java.util.Random;

// TODO: Fix soundpool lag

public class Menu extends Activity {

    MenuView menuView;

    public class MenuView extends SurfaceView implements Runnable {

        Thread gameThread = null;
        private long timeThisFrame;
        long fps = 60;

        boolean paused;
        volatile boolean playing;

        SurfaceHolder ourHolder;

        Canvas canvas;
        Paint paint;

        int screenX;
        int screenY;

        ArrayList<Ball> ballBuff;

        float panCount;

        Rect textContainer;

        String PONG_TITLE_TEXT = getString(R.string.menu_title);
        String PONG_START_TEXT = getString(R.string.menu_start);

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
                ballBuff.add(new Ball((int) (2 * screenX * (((float) i) / 30.0)), screenY));
                ballBuff.get(i).setxVelocity(0);
                ballBuff.get(i).setyVelocity(500);
//                ballBuff.get(i).setRandomVelocity();
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

//                    for (int i=0; i < ballBuff.size(); i++) {
//                        ballBuff.get(i).update(fps);
//                        ballBuff.get(i).setxVelocity(0);
//                    ballBuff.get(i).setyVelocity(1000);
//                    }

                    if (textContainer.contains(touchX, touchY)) {
                        Intent gameIntent = new Intent(Menu.this, SimpleGameEngine.class);
                        Menu.this.startActivity(gameIntent);
                    }
            }
            return true;
        }

        public void draw() {
            if (ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();

                // Bottom layer == black
                canvas.drawColor(Color.argb(255, 0, 0, 0));

                paint.setColor(Color.argb(255, 255, 255, 255));

                for (int i=0; i < ballBuff.size(); i++) {
                    paint.setColor(Colors.getRandomColor());
                    canvas.drawRect(ballBuff.get(i).getRect(), paint);
                }

                paint.setTextSize(250);

                Rect bounds =  new Rect();
                paint.getTextBounds(PONG_START_TEXT, 0, PONG_START_TEXT.length(), bounds);
                float mTextWidth = paint.measureText(PONG_START_TEXT);
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
                    canvas.drawText(PONG_START_TEXT, screenX / 2, screenY * (float) 0.80, paint);
                }
                paint.setTextSize(screenX / 4);
                canvas.drawText(PONG_TITLE_TEXT, screenX/2, panCount, paint);
                paint.setColor(Colors.getRandomColor());
                canvas.drawText(PONG_TITLE_TEXT, screenX/2 + 10, panCount + 10, paint);
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void restart() {
            for (int i=0; i < ballBuff.size(); i++) {
                int xPos = (int) (2*screenX*((float) i/30.0));
                int yPos = (int) ((double) screenY/200 * Math.sin((double) xPos));
                ballBuff.get(i).reset(xPos, yPos);
                ballBuff.get(i).setRandomVelocity();
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
