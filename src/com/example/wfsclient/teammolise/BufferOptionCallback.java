package com.example.wfsclient.teammolise;

import com.example.wfsclient.layers.Layer;
import com.vividsolutions.jts.geom.Geometry;

import java.util.List;

/**
 * Created by matt on 6/22/15.
 */
public interface BufferOptionCallback {

    public void setBufferingOptions(String nameTxt, Layer selected, List<Geometry> selectedGeometries, double distance, boolean dissolve, boolean save);

}
