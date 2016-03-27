package com.shaunlp.pong;

import android.graphics.RectF;

import java.util.Random;

public class Ball {
    RectF rect;
    float xVelocity;
    float yVelocity;
    float ballWidth = 30;
    float ballHeight = 30;

    public Ball(int screenX, int screenY) {
        xVelocity = 500;
        yVelocity = 1000;

        rect = new RectF();
    }

    public RectF getRect(){
        return rect;
    }

    public float getBallWidth() {return ballWidth;}

    public void update(long fps){
        rect.left = rect.left + (xVelocity / fps);
        rect.top = rect.top + (yVelocity / fps);
        rect.right = rect.left + ballWidth;
        rect.bottom = rect.top - ballHeight;
    }

    public void reverseYVelocity() {
        yVelocity = -yVelocity;
    }

    public void setNegativeVelocity() {
        yVelocity = -1 * Math.abs(yVelocity);
    }

    public void setPositiveVelocity() {
        yVelocity = Math.abs(yVelocity);
    }

    public void reverseXVelocity() {
        xVelocity = -xVelocity;
    }

    public void setRandomXVelocity() {
        Random generator = new Random();
        int answer = generator.nextInt(2);

        if (answer == 0) {
            reverseXVelocity();
        }
    }

    public void clearObstacleY(float y){
        rect.bottom = y;
        rect.top = y - ballHeight;
    }

    public void clearObstacleX(float x){
        rect.left = x;
        rect.right = x+ballWidth;
    }

    public void reset(int x, int y){
        rect.left = x / 2;
        rect.top = y /2;
        rect.right = x / 2 + ballWidth;
        rect.bottom = y / 2 - ballHeight;
    }
}
