package com.example.wfsclient;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParserException;

import com.example.wfsclient.layers.Layer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class WFSClientMainActivity extends Activity {

	private final static Logger LOGGER = Logger.getLogger(WFSClientMainActivity.class.getName());
	
	private boolean wifiConnected=false;
	private boolean mobileConnected=false;
	private LinkedList<Object> listaOggetti=new LinkedList<Object>();
	private boolean disegna=false;
    private List<Layer> currentLayers;

	//WFS NSIDC
    //GOOD BUFFER: 10000
	private String defaultwfs = "http://nsidc.org/cgi-bin/atlas_north?service=WFS&request=GetCapabilities";
	//Piemonte
	//private String defaultwfs = "http://geomap.reteunitaria.piemonte.it/ws/gsareprot/rp-01/areeprotwfs/wfs_gsareprot_1?service=WFS&request=getCapabilities";
	//Sardegna
	//private String defaultwfs = "http://webgis.regione.sardegna.it/geoserver/wfs?service=WFS&request=GetCapabilities";

    String featureName = null;

	private static String wfsVersion = "1.1.0";
	private URL serviceURL = null;
	private List<String> feature;//l'index 0 contiene l'ind del wfs
	private String request="";
	private boolean requestBoolean=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wfsclient_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wfsclient_main, menu);
		return true;
	}

	public void onStart(){
		super.onStart();
		//startConnection();
	}

	/**Inizia la connessione ad internet*/
	private void startConnection(){
		ConnectivityManager connMan =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeInfo=connMan.getActiveNetworkInfo();
		//Controlla il tipo di connessione, wifi o mobile
		if(activeInfo !=null && activeInfo.isConnected()){
			wifiConnected= activeInfo.getType()==ConnectivityManager.TYPE_WIFI;
			mobileConnected= activeInfo.getType()==ConnectivityManager.TYPE_MOBILE;
		}else{
			wifiConnected=false;
			mobileConnected=false;
		}
		loadPage();
	}

	/**Se c'è una connessione disponibile avvia il carimento della pagina xml in background
	 * controllando se deve effettuare una request o un getcapabilities*/
	private void loadPage() {
		//se la connessione è presente avvia il caricamento
		if (wifiConnected || mobileConnected) {
			// AsyncTask subclass
			if(!requestBoolean)
				new ConnectToWFS().execute(defaultwfs);
			else
				new DownloadXmlTask().execute(request);
		} else {
			showErrorPage();
		}
	}

	/**Visualizza un messaggio se non c'� connessione*/
	public void showErrorPage(){
		Toast toast = Toast.makeText(this,"Connessione assente",Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/**Starta una nuova connessione*/
	public void resetConnection(View v){
		startConnection();
	}
	/**
	 * Classe asincrona per la connesione al WFS
	 *
	 */
	private class ConnectToWFS extends AsyncTask <String,Void,String>{

		protected String doInBackground(String... urls) {
			try {
				return loadCapabilitiDocument(urls[0]);
			} catch (IOException e) {
				return getResources().getString(R.string.connection_error);
			} catch (XmlPullParserException e) {
				return getResources().getString(R.string.xml_error);
			}
		}

		protected void onPostExecute(String result) {
			request=feature.get(0)+"service=WFS&version="+wfsVersion+"&request=GetFeature&typeName="+feature.get(1);
			LOGGER.info("REQUEST 1 " + feature.toString());
			requestBoolean=true;
			startConnection();//Ricontrolla la connessione e avvia il download della feature
		}

	}//end classe task WFS

	/**
	 * Classe asincrona per effettuare il parser del gml
	 */
	private class DownloadXmlTask extends AsyncTask <String,Void,String>{

		@Override
		protected String doInBackground(String... urls) {
			try {
				return loadXmlFromNetwork(urls[0]);
			} catch (IOException e) {
				return getResources().getString(R.string.connection_error);
			} catch (XmlPullParserException e) {
				return getResources().getString(R.string.xml_error);
			}
		}
		//Verr� eseguito al completamento di loadXmlFronNetwork
		//per mostare all�utente il file scaricato.
		protected void onPostExecute(String result) {
			setContentView(R.layout.activity_wfsclient_main);
				try {
					LOGGER.info("INVOCO LA VIEW");
					disegnaOnView(listaOggetti);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

	}

	/**Effettua il parser del capabilitie document utilizzando la classe ParseCapabilities */
	private String loadCapabilitiDocument(String urlString) throws XmlPullParserException, IOException{

		InputStream stream = null;
		String result ="";
		//aggiungo la versione del wfs all'url
		urlString=urlString+"&version="+wfsVersion;
		try{
			stream=downloadUrl(urlString);
			feature=ParserCapabilities.parse(stream);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			// Chiusura dell'INPUT STREAM
			if (stream != null) {
				stream.close();
			}
		}
		return result;
	}

	/**Scarica il file gml dall'url*/
	private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {

		InputStream stream = null;
		String result ="";

		try{
			stream=downloadUrl(urlString);
			if(!disegna)
				result=XMLParser.parse(stream);
			else{
				listaOggetti=XMLParserDraw.parse(stream);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (stream != null) {
				stream.close();
			}
		}
		return result;
	}

	/**Si collega all'indirizzo dell'url*/
	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		InputStream stream = conn.getInputStream();
		return stream;
	}
	/**Flag per distinguere quale bottone � stato utilizzato*/
	public void disegna(View view){
		disegna=true;
		startConnection();
	}

	/**Invoca la View per disegnare la Feature*/
	public void disegnaOnView(LinkedList<Object> l) throws ParseException{
        Layer standard = new Layer();
        for (Object o : l)
            if (o instanceof Geometry)
                standard.addGeometry((Geometry)o);
        List<Layer> layers = new ArrayList<Layer>();
        layers.add(standard);

        this.currentLayers = layers;
		setContentView(new DrawView(this, layers));
	}


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.currentLayers == null)
            return false;

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText rangeInput = new EditText(this);

        alert.setView(rangeInput);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });

        final Layer layer = this.currentLayers.get(0);

        switch (item.getItemId()) {
            case R.id.action_Buffer:
                alert.setTitle("Buffering");
                alert.setMessage("Which geometries id(s) do you want to buffer (input: X:distance or X-Y:distance)\n" +
                        "Max: " + layer.getGeometries().size());

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = rangeInput.getText().toString();
                        String[] wholeParts = text.split("\\:");
                        int distance = 20000;
                        String mainPart = wholeParts[0];

                        if (wholeParts.length == 2)
                            distance = Integer.parseInt(wholeParts[1]);

                        String[] parts = mainPart.split("\\-");
                        int from = Integer.parseInt(parts[0]);
                        int to = from+1;
                        if (parts.length == 2)
                            to = Integer.parseInt(parts[1]);
                        layer.addGeometry(layer.applyBuffers(layer.getGeometries().subList(from, to), distance));
                    }
                });

                alert.show();
                return true;
            case R.id.action_Intersection:
                alert.setTitle("Intersection");
                alert.setMessage("Which geometries ids do you want to intersect (input: X-Y).\n" +
                        "Max: " + layer.getGeometries().size());

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = rangeInput.getText().toString();
                        String[] parts = text.split("\\-");
                        int from = Integer.parseInt(parts[0]);
                        int to = Integer.parseInt(parts[1])+1;
                        layer.addGeometry(layer.applyIntersection(layer.getGeometries().subList(from, to)));
                    }
                });

                alert.show();
                return true;
            default:
                return false;
        }
    }
}
