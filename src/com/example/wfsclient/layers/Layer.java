package com.example.wfsclient.layers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.RadialGradient;
import android.os.Environment;
import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollectionIterator;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SimpleFeatureType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by simone on 04/06/15.
 */
public class Layer {
    private List<Geometry> geometries;
    private boolean cancelOperation;
    private LayerListener listener;
    private String name;
    private int color;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return this.color;
    }

    public Layer(List<Geometry> pGeometries) {
        this.geometries = pGeometries;
        this.listener = new VoidListener();

        Random random = new Random();
        this.color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
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

    public Layer applyBuffer(List<Geometry> pGeometries, final double pDistance, final boolean pDissolve) throws InterruptedException {
        if (pGeometries.size() == 0)
            return new Layer();
        List<Geometry> result = new ArrayList<Geometry>();

        if (pDissolve) {
            Geometry currentGeometry = pGeometries.get(0);

            for (int i = 1; i < pGeometries.size(); i++)
                currentGeometry = currentGeometry.union(pGeometries.get(i));

            result.add(currentGeometry.buffer(pDistance));
        } else {
            for (Geometry geometry : pGeometries)
                result.add(geometry.buffer(pDistance));
        }

        return new Layer(result);
    }

    public Layer applyIntersection(List<Geometry> pGeometries1, List<Geometry> pGeometries2) throws InterruptedException {
        if (pGeometries1.size() == 0 || pGeometries2.size() == 0)
            return new Layer();

        Geometry intersectionGeometry = this.union(pGeometries2);
        List<Geometry> result = new ArrayList<Geometry>();

        for (Geometry geometry : pGeometries1) {
            Geometry intersection = geometry.intersection(intersectionGeometry);
            if (!intersection.isEmpty())
                result.add(geometry.intersection(intersectionGeometry));
        }

        return new Layer(result);
    }

    private Geometry union(List<Geometry> pGeometries) {
        Geometry currentGeometry = pGeometries.get(0);
        for (int i = 1; i < pGeometries.size(); i++)
            currentGeometry = currentGeometry.union(pGeometries.get(i));

        return currentGeometry;
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

    public void save(String fileName) throws IOException {
        GMLWriter writer = new GMLWriter("EPSG:3003");
        String serialized = writer.write(this);

        String realFileName = fileName.replaceAll(" ", "_").replaceAll("[^A-Za-z_]", "");

        FileOutputStream outputStream;

        File file = new File(Environment.getExternalStorageDirectory(), "WFSClient/"+realFileName+".gml");
        file.getParentFile().mkdirs();
        file.createNewFile();
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(serialized.getBytes());
            outputStream.close();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return this.name;
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