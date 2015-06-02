package com.example.wfsclient;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import android.util.Xml;

public class XMLParserDraw {

	private final static Logger LOGGER = Logger.getLogger(XMLParserDraw.class .getName());
	
	public static LinkedList<Object> parse(InputStream in) throws XmlPullParserException, IOException {
		
		LinkedList<Object> result;
	
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			result = readXML(parser);
		} finally {
			in.close();
		}
		return result;
	}

	private static LinkedList readXML(XmlPullParser parser) throws XmlPullParserException, IOException {
	
		int event;
		String stringaWKT="";
		Geometry g1;
		LinkedList l=new LinkedList();
		String coordinate;
		try {
			event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				String name=parser.getName();
				switch (event){
				case XmlPullParser.END_TAG:

					break;
				case XmlPullParser.TEXT:
					//string.append(parser.getText());
					break;

				case XmlPullParser.START_TAG:
					if(name.equals("gml:Point")){
						event=parser.nextTag();
						name=parser.getName();
						if(name.equals("gml:pos")){//CONTROLLA LA READ
							coordinate=parser.nextText();
							g1= new WKTReader().read("POINT("+coordinate+")");
							l.add(g1);
						}
					}
					else if(name.equals("ms:NAME")){
						Object userData =parser.nextText();
						g1=(Geometry)l.removeLast();
						g1.setUserData(userData);
						l.add(g1);
					}
					else if(name.equals("gml:LineString")){ 	
						event=parser.nextTag();
						name=parser.getName();
						if(name.equals("gml:posList")){//AGGIUNGERE CONTROLLO SULL' ATTR
							coordinate=parser.nextText();
							stringaWKT=convertiStringaPosList(coordinate);
							g1= new WKTReader().read("LINESTRING ("+stringaWKT+")");
							l.add(g1);
						}
					}
					else if(name.equals("gml:LinearRing")){
						event=parser.nextTag();
						name=parser.getName();
						if(name.equals("gml:posList")){//AGGIUNGERE CONTROLLO SULL' ATTR
							coordinate=parser.nextText();
							stringaWKT=convertiStringaPosList(coordinate);
							g1= new WKTReader().read("LINEARRING ("+stringaWKT+")");
							l.add(g1);
						}
					}
					else if(name.equals("gml:polygon")){
						event=parser.nextTag();
						name=parser.getName();
						if(name.equals("gml:outerBoundaryIs")){
							event=parser.nextTag();
							name=parser.getName();
							if(name.equals("gml:LinearRing")){
								event=parser.nextTag();
								name=parser.getName();
								if(name.equals("gml:posList")){
									coordinate=parser.nextText();
									stringaWKT=convertiStringaPosList(coordinate);
									g1= new WKTReader().read("POLYGON (("+stringaWKT+"))");
								}
							}
						}
					}
					else if(name.equals("gml:MultiPoint")){
					
					}
					break;
				}		 
				event = parser.next(); 
			}//end while

		} catch (Exception e) {
			e.printStackTrace();
		}
		return l;
	}//end readXML

	public static String convertiStringaPosList(String str){
		String[] strA=str.split(" ");
		String s="";
		int j=1;
		for(int i=0;i<strA.length;i++){
			if(j%2==0)
				s=s+" ";
			else
				s=s+", ";
			j++;
			s=s+strA[i];
		}
		s=s.replaceFirst(", ","");
		return s;
	}

}//end classe