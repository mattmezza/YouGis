package com.example.wfsclient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.example.wfsclient.layers.Layer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
	private ProgressDialog progressDialog;
    private ProgressDialog dlProgressDialog;

	//WFS NSIDC
    //GOOD BUFFER: 10000
	private String defaultwfs = "http://nsidc.org/cgi-bin/atlas_north?service=WFS&request=GetCapabilities";
    private Map<String, String> wfsList;

    String featureName = null;

	private static String wfsVersion = "1.1.0";
	private URL serviceURL = null;
	private List<String> feature;//l'index 0 contiene l'ind del wfs
	private String request="";
	private boolean requestBoolean=false;

    private void createMenuEntries() {
        wfsList = new HashMap<String, String>();
        wfsList.put("North atlas", "http://nsidc.org/cgi-bin/atlas_north?service=WFS&request=GetCapabilities");
        wfsList.put("Zone sismiche", "http://wms.pcn.minambiente.it/ogc?map=/ms_ogc/wfs/Zone_sismogenetiche_ZS9.map");
        wfsList.put("Torino", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc116_chiese?service=WFS&request=getCapabilities");
        wfsList.put("Sardegna", "http://webgis.regione.sardegna.it/geoserver/wfs?service=WFS&request=GetCapabilities");
        wfsList.put("Torino - Votazioni", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc107_bagni?service=WFS&request=getCapabilities");
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wfsclient_main);
        createMenuEntries();
		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setCancelable(false);
		this.progressDialog.setMax(100);
		this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		this.progressDialog.setTitle("Attendere");
		this.progressDialog.setMessage("Effettuando il parsing...");
        this.dlProgressDialog = new ProgressDialog(this);
        this.dlProgressDialog.setMessage("Scaricando i dati...");
        this.dlProgressDialog.setTitle("Attendere");
        this.dlProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.dlProgressDialog.setIndeterminate(true);
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
	 * Classe asincrona per la connesione al WFS. Ha il compito di scaricare le capabilities del WFS.
	 *
	 */
	private class ConnectToWFS extends MyAsyncTask <String,Integer,String>{

		protected String doInBackground(String... urls) {
			try {
                String urlString = urls[0];
                InputStream stream = null;
                String result ="";
                //aggiungo la versione del wfs all'url
                urlString = urlString+"&version="+wfsVersion;
                try {
                    stream = downloadUrl(urlString, this);

                    feature = ParserCapabilities.parse(stream);
                } catch (SocketTimeoutException e) {
                    throw new IOException(e.getMessage());
                }catch(Exception e){
                    throw new IOException(e.getMessage());
                }finally{
                    // Chiusura dell'INPUT STREAM
                    if (stream != null) {
                        stream.close();
                    }
                }
                return result;
			} catch (IOException e) {
                showError("Error", "A connection error occurred.");
			} catch (Exception e) {
                showError("Error", "An unknown error occurred.");
                e.printStackTrace();
            }

            return "";
		}

		protected void onPostExecute(String result) {
            if (feature == null || feature.size() == 0) {
                showError("Error", "An error occurred when trying to load the capabilities. No capability available.");
                return;
            }
            String baseUrl = feature.get(0);

			request = baseUrl +
                    (baseUrl.endsWith("?") ? "" : "?") +
                    "service=WFS&version=" + wfsVersion +
                    "&request=GetFeature&typeName=" + feature.get(1);
			LOGGER.info("REQUEST 1 " + feature.toString());
			requestBoolean=true;
			startConnection();//Ricontrolla la connessione e avvia il download della feature
		}

        protected void onProgressUpdate(Integer... values) {
            LOGGER.info(Arrays.toString(values));
            super.onProgressUpdate(values);
        }


    }//end classe task WFS

	/**
	 * Classe asincrona per effettuare il parser del gml. Ha il compito di disegnare una singola capability.
	 */
	private class DownloadXmlTask extends MyAsyncTask <String,Integer,String>{

		@Override
		protected String doInBackground(String... urls) {
			try {
                String urlString = urls[0];
                InputStream stream = null;
                String result ="";

                try{

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dlProgressDialog.show();
                        }
                    });
                    stream=downloadUrl(urlString, this);
                    if(!disegna)
                        result=XMLParser.parse(stream);
                    else{

                        final XMLParserDraw xmlParserDraw = new XMLParserDraw(stream, new ParserProgress() {
                            @Override
                            public void updateDialog(final int current, int total) {
//                                int h = current*100;
//                                final int percent =  (int) Math.round(h/total);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.setProgress(current);
                                    }
                                });
                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dlProgressDialog.dismiss();
                                progressDialog.setMax(xmlParserDraw.getTotalTags());
                                progressDialog.show();
                            }
                        });
                        listaOggetti=xmlParserDraw.parse();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    if (stream != null) {
                        stream.close();
                    }
                }
                return result;
			} catch (IOException e) {
                showError("Error", "A connection error occurred.");
				return getResources().getString(R.string.connection_error);
			} catch (Exception e) {
				showError("Error", "An unknown error occurred.");
                e.printStackTrace();
			} finally {

            }

            return "";
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

        @Override
        protected void onProgressUpdate(Integer... values) {
            LOGGER.info(Arrays.toString(values));
            super.onProgressUpdate(values);
        }

    }

	/**Si collega all'indirizzo dell'url*/
	private InputStream downloadUrl(String urlString, MyAsyncTask<String, Integer, String> pAsyncTask) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();

		InputStream stream = conn.getInputStream();

        if (conn.getContentLength() == -1) {
            //TODO: Set indeterminate

            return stream;
        }

        int currentByte = 0;
        byte[] result = new byte[conn.getContentLength()];
        int i = 0;
        while ((currentByte = stream.read()) != -1) {
            result[i] = (byte)currentByte;
            pAsyncTask.publish((int) (((float) i / result.length) * 100));
            i++;
        }

        InputStream newStream = new ByteArrayInputStream(result);
		return newStream;
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
        if (this.currentLayers == null) {
            if (item.getItemId() == R.id.action_settings) {
                final String[] items = new String[wfsList.keySet().size()];

                int i = 0;
                for (String currentSelectionItem : wfsList.keySet()) {
                    items[i] = currentSelectionItem;
                    i++;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select a WFS");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        defaultwfs = wfsList.get(items[item]);
                        Toast.makeText(getApplicationContext(), "Selected WFS "+items[item], Toast.LENGTH_SHORT).show();
                    }
                }).show();

                return true;
            }
            return false;
        }

        //Creates the alert box
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //Adds a "Cancel" button
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        final Layer layer = this.currentLayers.get(0);

        LayoutInflater inflater;
        View alertView;
        final EditText idsText;

        switch (item.getItemId()) {
            case R.id.action_Buffer:
                alert.setTitle("Buffering (0 to " + (layer.getGeometries().size()-1) +")");

                //Sets the layout
                inflater = this.getLayoutInflater();
                alertView = inflater.inflate(R.layout.dialog_buffering, null);
                alert.setView(alertView);

                idsText = (EditText)alertView.findViewById(R.id.bufferingIds);
                final EditText distanceText = (EditText)alertView.findViewById(R.id.bufferingDistance);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int distance;
                        try {
                            distance = Integer.parseInt(distanceText.getText().toString());
                        } catch (NumberFormatException e) {
                            showError("Error", "Distance must be a number!");
                            return;
                        }

                        String[] parts = idsText.getText().toString().split("\\,");
                        List<Geometry> elements;
                        try {
                            elements = getGeometriesFromLayer(layer, parts);
                        } catch (NumberFormatException e) {
                            showError("Error", "Wrong format for ID list!");
                            return;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            showError("Error", "ID not valid!");
                            return;
                        }

                        layer.addGeometry(layer.applyBuffers(elements, distance));
                    }
                });

                alert.show();
                return true;
            case R.id.action_Intersection:
                alert.setTitle("Intersection (0 to " + (layer.getGeometries().size()-1) +")");

                //Sets the layout
                inflater = this.getLayoutInflater();
                alertView = inflater.inflate(R.layout.dialog_intersect, null);
                alert.setView(alertView);

                idsText = (EditText)alertView.findViewById(R.id.intersectIds);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String[] parts = idsText.getText().toString().split("\\,");
                        List<Geometry> elements;
                        try {
                            elements = getGeometriesFromLayer(layer, parts);
                        } catch (NumberFormatException e) {
                            showError("Error", "Wrong format for ID list!");
                            return;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            showError("Error", "ID not valid!");
                            return;
                        }

                        layer.addGeometry(layer.applyIntersection(elements));

                        for (Geometry toRemove : elements)
                            layer.removeGeometry(toRemove);
                    }
                });

                alert.show();
                return true;
            default:
                return false;
        }
    }

    public interface ParserProgress {
        public void updateDialog(int current, int total);
    }

    private List<Geometry> getGeometriesFromLayer(Layer pLayer, String[] pIds) {
        List<Geometry> elements = new ArrayList<Geometry>();
        for (String part : pIds) {
            int currentId = Integer.parseInt(part);
            elements.add(pLayer.getGeometries().get(currentId));
        }

        return elements;
    }

    private void showError(String pTitle, String pMessage) {
        new AlertDialog.Builder(this)
                .setTitle(pTitle)
                .setMessage(pMessage)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
