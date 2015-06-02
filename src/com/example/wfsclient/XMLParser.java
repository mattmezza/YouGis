package com.example.wfsclient;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;

public class XMLParser {
	public static String parse(InputStream in) throws XmlPullParserException, IOException {
		String result;
		try {
			// inizializzo interfaccia
			XmlPullParser parser = Xml.newPullParser();
			// specifica all'interfaccia che non saranno usati namespaces
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			// imposta l'input passandogli il relativo input stream
			parser.setInput(in, null);
			// richie il prossimo evento tag
			parser.nextTag();
			result = readXML(parser);
		} finally {
			in.close();
		}
		return result;
	}

	private static String readXML(XmlPullParser parser) throws XmlPullParserException, IOException {
		//inizializzo la stringa che conterrà il risultato
		StringBuilder string = new StringBuilder();
		string.append("START DOCUMENT<br>");
		int event;
		try {
			event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				String name=parser.getName();
				switch (event){
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.TEXT:
					string.append(parser.getText());
					break;

				case XmlPullParser.START_TAG:
					if(name.equals("gml:Point")){
						string.append("<br>" + parser.getName()+"<br>");
						name=parser.getName();
						if(name.equals("gml:pos")){
							string.append("<br>"+parser.nextText()+"<br>");
						}
					}
					else if(name.equals("gml:LineString")){ 	
						string.append("<br>" + parser.getName()+"<br>");
						event=parser.nextTag();
						name=parser.getName();
						string.append(parser.getName());
						if(name.equals("gml:posList")){
							string.append("<br>"+parser.nextText()+"<br>");
						}
					}
					else if(name.equals("gml:Linearring")){
						string.append("<br>" + parser.getName()+"<br>");
						event=parser.nextTag();
						name=parser.getName();
						string.append(parser.getName());
						if(name.equals("gml:posList")){
							string.append("<br>"+parser.nextText()+"<br>");
						}
					}
					else if(name.equals("gml:polygon")){	         
						string.append("<br>" + parser.getName()+"<br>");
						event=parser.nextTag();
						name=parser.getName();
						if(name.equals("gml:outerBoundaryIs")){
							event=parser.nextTag();
							name=parser.getName();
							if(name.equals("gml:LinearRing")){
								event=parser.nextTag();
								name=parser.getName();
								if(name.equals("gml:posList")){
									string.append("<br>"+parser.nextText()+"<br>");
								}
							}	
						}
					}
					//else if(name.equals("")){	                     
					//}
					break;
				}		 
				event = parser.next(); 

			}//end while

		} catch (Exception e) {
			e.printStackTrace();
		}
		string.append("END");
		return string.toString();
	}//end readXML
}//end classe

