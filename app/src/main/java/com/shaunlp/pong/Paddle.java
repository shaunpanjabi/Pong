package com.shaunlp.pong;

import android.graphics.RectF;

public class Paddle {
    private RectF rect;

    private float length;
    private float height;

    private float x;
    private float y;

    private float paddleSpeed;

    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int paddleMoving = STOPPED;

    private int screenyX;

    public boolean ai = false;

    public Paddle(int screenX, int screenY, int start_x, int start_y) {
        length = screenX * (float) 0.15;
        height = 30;

        x = start_x;
        y = start_y;

        screenyX = screenX;

        rect = new RectF(start_x, start_y, start_x + length, start_y + height);


        paddleSpeed = 1200;

    }

    public RectF getRect() {
            return rect;
    }

    public float getHeight() {
        return height;
    }

    public void setMovementState(int state) {
        paddleMoving = state;
    }

    public void setPaddleSpeed(int speed) {paddleSpeed = speed; }

    public void setAi(boolean aiSet) {ai = aiSet; }

    public void update(long fps){
        if(paddleMoving == LEFT) {
            if (x >= 0 ) {
                x = x - paddleSpeed / fps;
            }
        }

        if(paddleMoving == RIGHT){
            if (x + length <= screenyX) {
                x = x + paddleSpeed / fps;
            }
        }

        rect.left = x;
        rect.right = x + length;
    }
}
