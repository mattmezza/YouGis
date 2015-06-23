package com.example.wfsclient.layers;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Created by simone on 23/06/15.
 */
public class GMLWriter {
    private String refer;
    public GMLWriter(String pRef) {
        this.refer = pRef;
    }

    public String write(Layer pLayer) {
        String layerId = pLayer.getName().replaceAll(" ", "_").replaceAll("[^A-Za-z_]", "");
        String result = "";
        result += "<?xml version='1.0' encoding=\"ISO-8859-1\" ?>\n";
        result += "<wfs:FeatureCollection\n"+
            "xmlns:ms=\"http://mapserver.gis.umn.edu/mapserver\"\n" +
            "xmlns:gml=\"http://www.opengis.net/gml\"\n" +
            "xmlns:wfs=\"http://www.opengis.net/wfs\"\n" +
            "xmlns:ogc=\"http://www.opengis.net/ogc\"\n" +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";

        if (pLayer.getGeometries().size() > 0) {
            Geometry geometry1 = pLayer.getGeometries().get(0);
            result += writeBound(geometry1);
        }

        int id = 0;
        for (Geometry geometry : pLayer.getGeometries()) {
            result += "<gml:featureMember>\n";
            result += "<ms:" + layerId + " gml:id=\"" + layerId + "." + id + "\">\n";

            result += writeBound(geometry);

            result += "<ms:msGeometry>\n";
            result += exportGeometry(geometry);
            result += "</ms:msGeometry>\n" +
                    "</ms:"+layerId+">\n" +
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
        result += writeAllCoordinates(pPolygon.getExteriorRing());
        result += "</gml:posList>\n" +
                "</gml:LinearRing>\n" +
                "</gml:outerBoundaryIs>\n";
        for (int i = 0; i < pPolygon.getNumInteriorRing(); i++) {
            result += "<gml:innerBoundaryIs>\n";
            result += "<gml:LinearRing>\n";
            result += "<gml:posList srsDimension=\"2\">";
            result += writeAllCoordinates(pPolygon.getInteriorRingN(i));
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
        result += writeAllCoordinates(pLineString);
        result += "</gml:posList>\n";
        result += "</gml:LineString>\n";

        return result;
    }

    private String exportLinearRing(LinearRing pLinearRing) {
        String result = "<gml:LinearRing srsName=\""+refer+"\">\n";
        result += "<gml:posList srsDimension=\"2\">";
        result += writeAllCoordinates(pLinearRing);
        result += "</gml:posList>\n" +
                "</gml:LinearRing>\n";

        return result;
    }

    private String exportPoint(Point pPoint) {
        String result = "<gml:Point srsName=\""+refer+"\">\n";
        result += "<gml:pos>";
        result += writeAllCoordinates(pPoint);
        result += "</gml:pos>\n" +
                "</gml:Point>\n";

        return result;
    }

    private String writeAllCoordinates(Geometry geometry) {
        String result = "";

        Coordinate[] coordinates = geometry.getCoordinates();
        for (Coordinate coordinate : coordinates) {
            result += coordinate.x + " " + coordinate.y + " ";
        }

        return result;
    }

    private String writeBound(Geometry pGeometry) {
        Coordinate[] coordinates = pGeometry.getCoordinates();
        Coordinate upperLeft;
        Coordinate lowerRight;
        upperLeft = coordinates[0];
        if (coordinates.length > 3)
            lowerRight = coordinates[3];
        else
            lowerRight = coordinates[0];

        return "<gml:boundedBy>\n" +
                "<gml:Envelope srsName=\"" + this.refer + "\">\n" +
                "<gml:lowerCorner>" + lowerRight.x + " " + lowerRight.y + "</gml:lowerCorner>\n" +
                "<gml:upperCorner>" + upperLeft.x + " " + upperLeft.y + "</gml:upperCorner>\n" +
                "</gml:Envelope>\n" +
                "</gml:boundedBy>\n";
    }
}
