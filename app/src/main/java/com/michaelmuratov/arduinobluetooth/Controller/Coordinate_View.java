package com.michaelmuratov.arduinobluetooth.Controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Coordinate_View extends View {

    private float[] coordinates = new float[10];

    private Paint paint = new Paint();

    public Coordinate_View(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        coordinates[0] = 0;
        coordinates[1] = 0;
        coordinates[2] = 0;
        coordinates[3] = 0;
        coordinates[4] = 0;
        coordinates[5] = 0;

        coordinates[6] = 0;
        coordinates[7] = 0;
        coordinates[8] = 0;
        coordinates[9] = 0;

        paint.setStrokeWidth(15);
    }

    public void setCentreControl(float x, float y){
        coordinates[0] = x;
        coordinates[1] = y;
    }

    public void setTouchCoordinates(float x, float y){
        coordinates[2] = x;
        coordinates[3] = y;
    }

    public void setCentreCircle(float x, float y){
        coordinates[4] = x;
        coordinates[5] = y;
    }

    public void setFrontDistance(float x1, float y1, float x2, float y2){
        coordinates[6] = x1;
        coordinates[7] = y1;
        coordinates[8] = x2;
        coordinates[9] = y2;
    }

    public void updateOverlay(){
        invalidate();
    }

    @Override
    protected void onDraw (Canvas canvas) {
            paint.setColor(Color.CYAN);
            canvas.drawLine(coordinates[6], coordinates[7],
                    coordinates[8], coordinates[9], paint);
/*
            paint.setColor(Color.BLACK);
            canvas.drawLine(coordinates[0], coordinates[1],
                            coordinates[2], coordinates[3], paint);
            paint.setColor(Color.RED);
            canvas.drawLine(coordinates[0], coordinates[1],
                            coordinates[2], coordinates[1], paint);
            paint.setColor(Color.BLUE);
            canvas.drawLine(coordinates[2], coordinates[1],
                            coordinates[2], coordinates[3], paint);

            paint.setColor(Color.GREEN);
            canvas.drawLine(coordinates[4], coordinates[5],
                            coordinates[2], coordinates[3], paint);
*/
        //Log.d("Drawing START","X: "+mCoordinates[0]+",Y: "+mCoordinates[1]);
        //Log.d("Drawing END","X: "+mCoordinates[2]+",Y: "+mCoordinates[3]);
    }

}