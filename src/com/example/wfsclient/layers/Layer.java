package com.example.wfsclient.layers;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simone on 04/06/15.
 */
public class Layer {
    private List<Geometry> geometries;

    public Layer(List<Geometry> pGeometries) {
        this.geometries = pGeometries;
    }

    public Layer() {
        this(new ArrayList<Geometry>());
    }

    public void addGeometry(Geometry pGeometry) {
        this.geometries.add(pGeometry);
    }

    public List<Geometry> getGeometries() {
        return this.geometries;
    }

    public Geometry applyBuffers(Geometry pGeometry, double pDistance) {
        Geometry buffer = pGeometry.buffer(pDistance);

        return buffer;
    }

    public Geometry applyBuffers(List<Geometry> pGeometries, double pDistance) {
        if (pGeometries.size() == 0)
            return new GeometryFactory().createPoint(new Coordinate(0, 0));

        Geometry currentGeometry = pGeometries.get(0);

        for (int i = 1; i < pGeometries.size(); i++)
            currentGeometry = currentGeometry.union(pGeometries.get(i));

        return currentGeometry.buffer(pDistance);
    }

    public Geometry applyIntersection(List<Geometry> pGeometries) {
        if (pGeometries.size() == 0)
            return new GeometryFactory().createPoint(new Coordinate(0, 0));

        Geometry currentGeometry = pGeometries.get(0);

        for (int i = 0; i < pGeometries.size(); i++)
            currentGeometry = currentGeometry.intersection(pGeometries.get(i));

        return currentGeometry;
    }
}
