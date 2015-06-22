package com.example.wfsclient.layers;

import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollectionIterator;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by simone on 04/06/15.
 */
public class Layer {
    private List<Geometry> geometries;
    private boolean cancelOperation;
    private LayerListener listener;
    private String name;

    public Layer(List<Geometry> pGeometries) {
        this.geometries = pGeometries;
        this.cancelOperation = false;
        this.listener = new VoidListener();
    }

    public Layer() {
        this(new ArrayList<Geometry>());
    }


    public void addGeometry(Geometry pGeometry) {
        this.geometries.add(pGeometry);
        this.listener.onLayerChange();
    }

    public List<Geometry> getGeometries() {
        return this.geometries;
    }

    @Deprecated
    public Geometry applyBuffers(Geometry pGeometry, double pDistance) {
        this.cancelOperation = false;
        Geometry buffer = pGeometry.buffer(pDistance);

        return buffer;
    }

    public Geometry applyBuffers(List<Geometry> pGeometries, final double pDistance) throws InterruptedException {
        if (pGeometries.size() == 0)
            return new GeometryFactory().createPoint(new Coordinate(0, 0));

        Geometry currentGeometry = pGeometries.get(0);

        for (int i = 1; i < pGeometries.size(); i++)
            currentGeometry = currentGeometry.union(pGeometries.get(i));

        final Geometry baseGeometry = currentGeometry;
        final Geometry buffer;

        OperationalThread thread = new OperationalThread<Double>(currentGeometry, new Double[] {pDistance}) {
            @Override
            public void run() {
                result = base.buffer(params[0]);
            }
        };

        thread.start();
        this.waitForThread(thread);

        return thread.getResult();
    }

    public Geometry applyIntersection(List<Geometry> pGeometries) throws InterruptedException {

        if (pGeometries.size() == 0)
            return new GeometryFactory().createPoint(new Coordinate(0, 0));

        OperationalThread thread = new OperationalThread<Object>(null, new Object[] {pGeometries}) {
            @Override
            public void run() {
                List<Geometry> geometries =(List<Geometry>)params[0];
                result = geometries.get(0);
                for (int i = 1; i < geometries.size(); i++)
                    result = result.intersection(geometries.get(i));
            }
        };

        thread.start();
        waitForThread(thread);

        return thread.getResult();
    }

    public synchronized void stopCurrentOperation() {
        this.cancelOperation = true;
        Log.d("STOP!!", "Set cancelOperation to " + cancelOperation);
    }

    private void waitForThread(OperationalThread thread) throws InterruptedException {
        this.cancelOperation = false;

        while (thread.isAlive()) {
            Thread.sleep(1000);
            Log.d("Looping", "Checking now...");
            synchronized (this) {
                if (this.cancelOperation) {
                    Log.d("Looping", "Stop!!");
                    thread.interrupt();
                    throw new InterruptedException();
                }
            }
        }
    }

    public boolean removeGeometry(Geometry pGeometry) {
        boolean result = this.geometries.remove(pGeometry);
        this.listener.onLayerChange();
        return result;
    }

    public void setListener(LayerListener listener) {
        if (listener != null)
            this.listener = listener;
        else
            this.listener = new VoidListener();
    }

    public void save(URL outputFilename) {

    }

    public interface LayerListener {
        public void onLayerChange();
    }

    private class VoidListener implements LayerListener {
        @Override
        public void onLayerChange() {
        }
    }
}

abstract class OperationalThread<Params> extends Thread {
    protected Params[] params;
    protected Geometry base;
    protected Geometry result;

    public OperationalThread(Geometry base, Params[] params) {
        this.params = params;
        this.base = base;
    }

    public abstract void run();

    public Geometry getResult() {
        return result;
    }
}