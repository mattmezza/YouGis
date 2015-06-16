package com.example.wfsclient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import android.util.Log;
import android.util.Xml;

public class XMLParserDraw {

	private final static Logger LOGGER = Logger.getLogger(XMLParserDraw.class .getName());

	private XmlPullParser countingParser;
	private XmlPullParser parser;
	private InputStream counting;
	private InputStream parsing;
	private WFSClientMainActivity.ParserProgress progressDelegate;
	private int totalTags;

	public XMLParserDraw(InputStream in, WFSClientMainActivity.ParserProgress progressDelegate) throws IOException, XmlPullParserException {
		this.progressDelegate = progressDelegate;
		// cloning input stream
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) > -1 ) {
			baos.write(buffer, 0, len);
		}
		baos.flush();

		this.counting = new ByteArrayInputStream(baos.toByteArray());
		this.parsing = new ByteArrayInputStream(baos.toByteArray());
		this.countingParser = Xml.newPullParser();
		this.countingParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		this.countingParser.setInput(this.counting, null);
		this.countingParser.nextTag();

		this.parser = Xml.newPullParser();
		this.parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		this.parser.setInput(this.parsing, null);
		this.parser.nextTag();

		this.totalTags = this.countObject(this.countingParser);
		Log.d("Matt", "number of objects: " + this.totalTags);
	}

	public int getTotalTags() {
		return this.totalTags;
	}
	
	public LinkedList<Object> parse() {
		int event;
		int currentTag = 0;
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
						currentTag++;
						if(progressDelegate!=null)
							progressDelegate.updateDialog(currentTag, this.totalTags);
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
	}

	private int countObject(XmlPullParser parser) throws XmlPullParserException, IOException {
		int event;
		int counter = 0;
		event = parser.getEventType();
		while(event!=XmlPullParser.END_DOCUMENT) {
			switch (event) {
				case XmlPullParser.START_TAG:
					counter++;
					break;
			}
			event = parser.next();
		}
		return counter;
	}

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