package com.example.wfsclient.layers;


import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Created by simone on 23/06/15.
 */
public class GMLWriter {
    private String refer;
    private WKTWriter writer;
    public GMLWriter(String pRef) {
        this.refer = pRef;
        this.writer = new WKTWriter();
    }

    public String write(Layer pLayer) {
        String layerId = pLayer.getName().replaceAll(" ", "_").replaceAll("[^A-Za-z_]", "");
        String result = "<wfs:FeatureCollection>\n";

        int id = 0;
        for (Geometry geometry : pLayer.getGeometries()) {
            result += "<gml:featureMember>\n";
            result += "<ms:" + layerId + " gml:id=\"" + layerId + "." + id + "\">\n";
            result += "<ms:msGeometry>\n";
            result += exportGeometry(geometry);
            result += "</ms:msGeometry>\n" +
                    "</gml:featureMember>\n";
            id++;
        }

        result += "</wfs:FeatureCollection>\n";



        return result;
    }

    private String exportGeometry(Geometry pGeometry) {
        if (pGeometry instanceof Polygon) {
            return exportPolygon((Polygon)pGeometry);
        } else if (pGeometry instanceof Point) {
            return exportPoint((Point) pGeometry);
        } else if (pGeometry instanceof LineString) {
            return exportLineString((LineString) pGeometry);
        } else if (pGeometry instanceof MultiPolygon) {
            String result = "";
            MultiPolygon mp = (MultiPolygon)pGeometry;

            for (int i = 0; i < mp.getNumGeometries(); i++) {

                if (pGeometry.getGeometryN(i) instanceof Polygon) {
                    Polygon polygon = (Polygon)pGeometry.getGeometryN(i);
                    result += exportPolygon(polygon);
                }
            }

            return result;
        } else if (pGeometry instanceof LinearRing) {
            return exportLinearRing((LinearRing) pGeometry);
        }

        return "";
    }

    private String exportPolygon(Polygon pPolygon) {
        String result = "<gml:Polygon srsName=\""+refer+"\">\n";
        result += "<gml:outerBoundaryIs>\n";
        result += "<gml:LinearRing>\n";
        result += "<gml:posList srsDimension=\"2\">";
        result += new WKTWriter().write(pPolygon.getExteriorRing());
        result += "</gml:posList>\n" +
                "</gml:LinearRing>\n" +
                "</gml:outerBoundaryIs>\n";
        for (int i = 0; i < pPolygon.getNumInteriorRing(); i++) {
            result += "<gml:innerBoundaryIs>\n";
            result += "<gml:LinearRing>\n";
            result += "<gml:posList srsDimension=\"2\">";
            result += new WKTWriter().write(pPolygon.getInteriorRingN(i));
            result += "</gml:posList>\n" +
                    "</gml:LinearRing>\n" +
                    "</gml:innerBoundaryIs>\n";
        }
        result += "</gml:Polygon>";

        return result;
    }

    private String exportLineString(LineString pLineString) {
        String result = "<gml:LineString srsName=\""+refer+"\">\n";
        result += "<gml:posList srsDimension=\"2\">";
        result += new WKTWriter().write(pLineString);
        result += "</gml:posList>\n";
        result += "</gml:LineString>\n";

        return result;
    }

    private String exportLinearRing(LinearRing pLienarRing) {
        String result = "<gml:LinearRing srsName=\""+refer+"\">\n";
        result += "<gml:posList srsDimension=\"2\">";
        result += new WKTWriter().write(pLienarRing);
        result += "</gml:posList>\n" +
                "</gml:LinearRing>\n";

        return result;
    }

    private String exportPoint(Point pPoint) {
        String result = "<gml:Point srsName=\""+refer+"\">\n";
        result += "<gml:pos>";
        result += new WKTWriter().write(pPoint);
        result += "</gml:pos>\n" +
                "</gml:Point>\n";

        return result;
    }
}
