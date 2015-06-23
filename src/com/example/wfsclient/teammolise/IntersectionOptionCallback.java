package com.example.wfsclient.teammolise;

import com.example.wfsclient.layers.Layer;
import com.vividsolutions.jts.geom.Geometry;

import java.util.List;

/**
 * Created by matt on 6/22/15.
 */
public interface IntersectionOptionCallback {

    public void setIntersectionOptions(String nameTxt,
                                       Layer selected1,
                                       List<Geometry> selectedGeometries1,

                                       Layer selected2,
                                       List<Geometry> selectedGeometries2,

                                       boolean save);

}
