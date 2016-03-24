package com.shaunlp.pong;

import android.graphics.RectF;

/**
 * Created by shaunpanjabi on 3/19/16.
 */
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

    public Paddle(int screenX, int screenY) {
        length = 130;
        height = 20;

        x = screenX / 2;
        y = screenY - 20;

        rect = new RectF(x, y, x + length, y + height);


        paddleSpeed = 350;

    }

    public RectF getRect() {
            return rect;
    }

    public void setMovementState(int state) {
        paddleMoving = state;
    }

    public void setPaddleSpeed(int speed) {paddleSpeed = speed; }

    public void update(long fps){
        if(paddleMoving == LEFT) {
            x = x - paddleSpeed / fps;
        }

        if(paddleMoving == RIGHT){
            x = x + paddleSpeed / fps;
        }

        rect.left = x;
        rect.right = x + length;
    }
}
