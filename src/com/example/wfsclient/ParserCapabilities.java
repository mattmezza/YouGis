package com.example.wfsclient;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;

public class ParserCapabilities {

	public static List<String> parse(InputStream in) throws XmlPullParserException, IOException {
		
		List<String> result;
		
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

	private static List<String> readXML(XmlPullParser parser) throws XmlPullParserException, IOException {
		
		StringBuilder string = new StringBuilder();
		int event;
		List<String> feature= new ArrayList<String>();
		String attributeName;
		String linkWFS;
		String nameFeatureType;
		
		try {
			event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				String name=parser.getName();
				switch (event){
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.TEXT:
					break;
				case XmlPullParser.START_TAG:
					if(name.equals("ows:Operation")){
						attributeName= parser.getAttributeValue(0);
						if(attributeName.equals("GetFeature")){
							string.append("<br>Operation: GetFeature");
							event=parser.nextTag();
							name=parser.getName();
							if(name.equals("ows:DCP")){
								event=parser.nextTag();
								name=parser.getName();
								if(name.equals("ows:HTTP")){
									event=parser.nextTag();
									name=parser.getName();
									if(name.equals("ows:Get")){
										linkWFS= parser.getAttributeValue(null,"xlink:href");
										feature.add(linkWFS);
										string.append("<br> ecco il link del server:<br>" + linkWFS);
									}
								}
							}
						}		
					}//end if Operation
					else if(name.equals("FeatureType")){	                     
						event=parser.nextTag();
						nameFeatureType=parser.nextText();
						feature.add(nameFeatureType);
						string.append("<br> Ho aggiunto: " + nameFeatureType);
					}
					break;
				}		 
				event = parser.next(); 

			}//end while

		} catch (Exception e) {
			e.printStackTrace();
		}
		return feature;
	}//end readXML
}
