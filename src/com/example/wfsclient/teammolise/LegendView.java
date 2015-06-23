package com.example.wfsclient.teammolise;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.wfsclient.layers.Layer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simone on 23/06/15.
 */
public class LegendView extends View{
    private static final int ELEMENT_HEIGHT = 64;
    private static final int TOP = 5;
    private static final int LEFT = 40;
    private List<Layer> layers;

    public LegendView(Context context) {
        super(context);
        this.layers = new ArrayList<Layer>();
    }

    public LegendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.layers = new ArrayList<Layer>();
    }

    public LegendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.layers = new ArrayList<Layer>();
    }

    public void setLayers(List<Layer> pLayers) {
        this.layers = pLayers;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.setMinimumHeight(ELEMENT_HEIGHT*this.layers.size()+TOP);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        int left = LEFT;
        int top = TOP;
        boolean increaseTop = false;
        for (Layer layer : this.layers) {
            paint.setColor(layer.getColor());
            canvas.drawCircle(left, top + ELEMENT_HEIGHT / 2, ELEMENT_HEIGHT / 2, paint);
            paint.setColor(Color.BLACK);
            paint.setTextSize(16);
            canvas.drawText(layer.getName(), left + ELEMENT_HEIGHT, top+ELEMENT_HEIGHT/2, paint);
            if (increaseTop) {
                left = LEFT;
                top += ELEMENT_HEIGHT;
            } else {
                left = this.getWidth()-300;
            }


            increaseTop = !increaseTop;
        }
    }
}
