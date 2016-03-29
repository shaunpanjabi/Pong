package com.shaunlp.pong;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Menu extends Activity {

    MenuView menuView;
    int screenX;
    int screenY;

    public class MenuView extends SurfaceView implements Runnable {

        Thread gameThread = null;
        private long timeThisFrame;
        volatile boolean playing;
        boolean paused = true;
        long fps;

        SurfaceHolder ourHolder;

        Canvas canvas;
        Paint paint;

        int screenX;
        int screenY;

        Ball ball;
        ArrayList<Ball> ballBuff;

        int touchCount;

        public MenuView(Context context) {
            super(context);

            ourHolder = getHolder();
            paint = new Paint();
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            screenX = size.x;
            screenY = size.y;

            ballBuff = new ArrayList<>();

            for (int i=0; i <= 9; i++ ) {
                ballBuff.add(new Ball(screenX, screenY));
                ballBuff.get(i).setRandomVelocity();
            }

            restart();
        }

        @Override
        public void run() {
            while (playing) {
                long startFrameTime = System.currentTimeMillis();
                if (!paused){
                    update();
                }

                draw();

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
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    paused = false;
                    if (touchCount >= 2) {
                        Intent gameIntent = new Intent(Menu.this, SimpleGameEngine.class);
                        Menu.this.startActivity(gameIntent);
                    } else {
                        touchCount += 1;
                    }

            }
            return true;
        }

        public void draw() {
            if (ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 0, 0, 0));

                paint.setColor(Color.argb(255, 255, 255, 255));

                for (int i=0; i < ballBuff.size(); i++) {
                    canvas.drawRect(ballBuff.get(i).getRect(), paint);
                }

                canvas.drawText("FPS:" + fps, 20, 40, paint);

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
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(menuView);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenX = size.x;
        screenY = size.y;

        RelativeLayout relativeLayout= new RelativeLayout(this);

//        TextView textview1 = (TextView) findViewById(R.id.texty);
        TextView dynamicTextView = new TextView(this);
        dynamicTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        dynamicTextView.setText(" PONG ");
        dynamicTextView.setTextSize(100);
        dynamicTextView.setX(screenX / 2 - 500);
        dynamicTextView.setY(screenY / 2 - 500);
        dynamicTextView.setTypeface(Typeface.SERIF);

        relativeLayout.addView(frameLayout);
        relativeLayout.addView(dynamicTextView);

        setContentView(relativeLayout);
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
