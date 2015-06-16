package com.example.wfsclient;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.example.wfsclient.layers.Layer;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class DrawView extends View {

	private final static Logger LOGGER = Logger.getLogger(DrawView.class .getName());
	private List<Layer> lista;

	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 0.0002f;

	private float mLastTouchX;
	private float mLastTouchY;

	private float mPosX;
	private float mPosY;

	Paint paint = new Paint();
	Paint paintTesto = new Paint();
    private float centerX;
    private float centerY;

    public DrawView(Context context,List<Layer> layers) {
		super(context);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        lista = layers;

        centerX = (float)lista.get(0).getGeometries().get(0).getCoordinates()[0].x;
        centerY = (float)lista.get(0).getGeometries().get(0).getCoordinates()[0].y;

        /*
        Geometry buffer1 = this.applyBuffers(lista.get(166), 30000);
        Geometry buffer2 = this.applyBuffers(lista, 30000);

        //lista.add(buffer1);
        lista.add(buffer2);

        //List<Geometry> toIntersect = new ArrayList<Geometry>();
        //toIntersect.add(buffer1);
        //toIntersect.add(buffer2);

        //lista.add(this.applyIntersection(toIntersect));
        */
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();
			mLastTouchX = x;
			mLastTouchY = y;
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			final float x = ev.getX();
			final float y = ev.getY();
			final float dx = x - mLastTouchX;
			final float dy = y - mLastTouchY;

			mPosX += dx;
			mPosY += dy;
			mLastTouchX = x;
			mLastTouchY = y;

			invalidate();
			break;
		}
		}
		mScaleDetector.onTouchEvent(ev);
		return true;
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();
			//Eventuali limiti allo scaling
			//mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 5.0f));
			invalidate();
			return true;
		}
	}

	protected void onDraw(Canvas canvas) {
        if (lista.size() == 0)
            return;

		super.onDraw(canvas);

		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		canvas.translate(mPosX, mPosY);
		canvas.scale(mScaleFactor, mScaleFactor,getWidth()/2,getHeight()/2);//scala verso il centro dello schermo

        double fromX = (this.mPosX-canvas.getWidth()/2)/canvas.getDensity();
        double toX = (this.mPosX+canvas.getWidth()/2)/canvas.getDensity();
        LOGGER.info("From: " + fromX +
                ", to:" + toX);
        paint.setStyle(Paint.Style.STROKE);
        for (Layer layer : this.lista) {
            for (Geometry geometry : layer.getGeometries()) {
                if (geometry instanceof Point) {
                    drawPoint((Point) geometry, canvas, paint);
                } else if (geometry instanceof LineString) {
                    drawLineString((LineString) geometry, canvas, paint);
                } else if (geometry instanceof LinearRing) {
                    drawLinearRing((LinearRing) geometry, canvas, paint);
                } else if (geometry instanceof Polygon) {
                    drawPolygon((Polygon)geometry, canvas, paint);
                } else if (geometry instanceof MultiPoint)
                    drawMultiPoint((MultiPoint)geometry, canvas, paint);
            }
        }
	}

    private void drawPolygon(Polygon o, Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.rgb(o.getNumPoints() % 255, (int)o.getArea() % 255, o.getBoundaryDimension() % 255));
        Path polygonPath = new Path();
        Collection<Object> holeVerticesCollection= new LinkedList<Object>();
        polygonPath = toPath(o.getExteriorRing().getCoordinates());
        int n= o.getNumInteriorRing();
        if(n!=0){
            for(int i=0;i<n;i++){
                holeVerticesCollection.add(o.getInteriorRingN(i).getCoordinates());
            }
            for(Iterator<Object> i = holeVerticesCollection.iterator(); i.hasNext(); ) {
                Coordinate[] holeVertices = (Coordinate[]) i.next();
                polygonPath.addPath(toPath(holeVertices));
            }
            canvas.drawPath(polygonPath, paint);
        }else{
            canvas.drawPath(polygonPath, paint);
        }
    }

    private Path toPath(Coordinate[] coordinates ) {
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        if (coordinates.length > 0) {
            path.moveTo((float) coordinates[0].x-centerX, (float) coordinates[0].y-centerY);
            for( int i = 0; i < coordinates.length; i++ ) {
                path.lineTo((float) coordinates[i].x-centerX, (float) coordinates[i].y-centerY);
            }
        }
        return path;
    }

    private void drawLinearRing(LinearRing o, Canvas canvas, Paint paint) {
        int n = o.getNumPoints();
        int k = 0;
        int arraySize=(n-1)*4;
        float[] ptn = new float [arraySize];
        for(int i=0;i<arraySize;i=i+4){
            ptn[i]=(float)o.getPointN(k).getX()-centerX;
            ptn[i+1]=(float)o.getPointN(k).getY()-centerY;
            ptn[i+2]=(float)o.getPointN(k+1).getX()-centerX;
            ptn[i+3]=(float)o.getPointN(k+1).getY()-centerY;
            k++;
        }
        canvas.drawLines(ptn, paint);
    }

    private void drawPoint(Point p, Canvas canvas, Paint paint) {
        //Disegna il punto
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle((float)p.getX()-centerX,(float) p.getY()-centerY, 3/mScaleFactor, paint);
        //canvas.drawPoint((float)p.getX()-centerX,(float) p.getY()-centerY, paint);
        //Ottieni result e i dati da visualizzare

        if(p.getUserData()!=null){
            String usdData=(String)p.getUserData();
            float result= getResources().getDimensionPixelSize(R.dimen.font_size)/mScaleFactor;

            //Settaggi per il testo
            paintTesto.setColor(Color.BLACK);
            paintTesto.setStrokeWidth(2);
            paintTesto.setStyle(Paint.Style.FILL);
            paintTesto.setAntiAlias(true);

            TextPaint pt = new TextPaint(paintTesto);

            if (result>600)result=600;
            pt.setTextSize(result);

            int s=4;
//			if(mScaleFactor<=0.001f)
//				s=4;
//			else if(mScaleFactor<=0.01f)
//				s=3;
            pt.setTextScaleX(s);
            pt.setTextScaleX(4);
            canvas.drawText(usdData,(float)p.getX()-centerX-10*s,(float) p.getY()-centerY-10*s, pt);
        }
    }

    public void drawLineString(LineString line,Canvas canvas, Paint paint){
        int n=0;
        float [] ptn=null;
        int k=0;
        n=line.getNumPoints();
        int arraySize=(n-1)*4;
        ptn = new float [arraySize];
        for(int i=0;i<arraySize;i=i+4){
            ptn[i]=(float)line.getPointN(k).getX()-centerX;
            ptn[i+1]=(float)line.getPointN(k).getY()-centerY;
            ptn[i+2]=(float)line.getPointN(k+1).getX()-centerX;
            ptn[i+3]=(float)line.getPointN(k+1).getY()-centerY;
            k++;
        }
        canvas.drawLines(ptn, paint);
    }

    private void drawMultiPoint(MultiPoint mp, Canvas canvas, Paint paint) {
        Coordinate coord[]= mp.getCoordinates();
        float ptn []=new float[mp.getNumPoints()*2];
        int j=0;
        for(int i=0;i<coord.length;i++){
            ptn[j]=(float)coord[i].x-centerX;
            ptn[j+1]=(float)coord[i].y-centerY;
            j=j+2;
        }
        canvas.drawPoints(ptn, paint);
    }
}
