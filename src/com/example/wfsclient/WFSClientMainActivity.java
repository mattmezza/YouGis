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
import com.vividsolutions.jts.operation.distance.DistanceOp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
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
    private boolean inDrawView;
    private DrawView drawView;
    private ProgressDialog opProgressDialog;

    private void createMenuEntries() {
        wfsList = new HashMap<String, String>();
        wfsList.put("North atlas", "http://nsidc.org/cgi-bin/atlas_north?service=WFS&request=GetCapabilities");
        wfsList.put("Torino - Azzonamenti sanitari", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc35_azzonamenti_sanitari?service=WFS&request=getCapabilities");
        wfsList.put("Torino - Carceri", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc122_carceri?service=WFS&request=getCapabilities");
        wfsList.put("Torino - Polizia amministrativa", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc48_polizia_amm?service=WFS&request=getCapabilities");
        wfsList.put("Torino", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc116_chiese?service=WFS&request=getCapabilities");
        wfsList.put("Sardegna", "http://webgis.regione.sardegna.it/geoserver/wfs?service=WFS&request=GetCapabilities");
        wfsList.put("Atlanta", "http://www.geoportal.rgurs.org/erdas-apollo/vector/ATLGML3_SHAPE?service=WFS&request=GetCapabilities");
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.inDrawView = false;
        this.drawView = null;
		setContentView(R.layout.activity_wfsclient_main);
        createMenuEntries();
		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setCancelable(false);
		this.progressDialog.setMax(100);
		this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		this.progressDialog.setTitle("Attendere");
		this.progressDialog.setMessage("Effettuando il parsing...");

        this.dlProgressDialog = new ProgressDialog(this);
        this.dlProgressDialog.setCancelable(false);
        this.dlProgressDialog.setTitle("Attendere");
        this.dlProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.dlProgressDialog.setIndeterminate(true);

        this.opProgressDialog = new ProgressDialog(this);
        this.opProgressDialog.setMessage("");
        this.opProgressDialog.setTitle("Attendere");
        this.opProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.opProgressDialog.setCancelable(false);
        this.opProgressDialog.setIndeterminate(true);
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
	private class ConnectToWFS extends MyAsyncTask <String,Integer,Boolean>{

		protected Boolean doInBackground(String... urls) {
            String urlString = urls[0];
            InputStream stream = null;
            String result ="";
            //aggiungo la versione del wfs all'url
            urlString = urlString+"&version="+wfsVersion;
			try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dlProgressDialog.setMessage("Connessione al WFS in corso...");
                        dlProgressDialog.show();
                    }
                });
                stream = downloadUrl(urlString, this);
                feature = ParserCapabilities.parse(stream);

                return true;
            } catch (SocketTimeoutException e) {
                showError("Error", "Connection timeout. Please, try later.");
                return false;
            }catch(Exception e){
                showError("Error", "An unknown error occurred. Please, check the log.");
                e.printStackTrace();
                return false;
            }finally{
                // Chiusura dell'INPUT STREAM
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {

                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dlProgressDialog.isShowing())
                            dlProgressDialog.dismiss();
                    }
                });
            }
		}

		protected void onPostExecute(Boolean result) {
            if (!result)
                return;

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
	private class DownloadXmlTask extends MyAsyncTask <String,Integer,Boolean>{

		@Override
		protected Boolean doInBackground(String... urls) {
            String urlString = urls[0];
            InputStream stream = null;
            String result ="";

			try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dlProgressDialog.setMessage("Download delle feature in corso...");
                        dlProgressDialog.show();
                    }
                });
                stream=downloadUrl(urlString, this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dlProgressDialog.dismiss();
                    }
                });
                if(!disegna)
                    result=XMLParser.parse(stream);
                else{

                    final XMLParserDraw xmlParserDraw = new XMLParserDraw(stream, new ParserProgress() {
                        @Override
                        public void updateDialog(final int current, int total) {
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
                }

                return true;
            } catch (IOException e) {
                requestBoolean = false;
                showError("Error", "A connection error occurred.");
                return false;
            }catch(Exception e){
                requestBoolean = false;
                showError("Error", "An unknown error occurred. Please, check the log.");
                e.printStackTrace();
                return false;
            }finally{
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dlProgressDialog.isShowing())
                            dlProgressDialog.dismiss();
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });
            }
		}

		//Verr� eseguito al completamento di loadXmlFronNetwork
		//per mostare all�utente il file scaricato.
		protected void onPostExecute(Boolean result) {
            if (!result)
                return;
			setContentView(R.layout.activity_wfsclient_main);
            try {
                LOGGER.info("INVOCO LA VIEW");
                disegnaOnView(listaOggetti);
            } catch (ParseException e) {
                showError("Error", "An unknown error occurred. Please, check the log.");
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            LOGGER.info(Arrays.toString(values));
            super.onProgressUpdate(values);
        }

    }

    private class BufferingTask extends MyAsyncTask <Object,Integer,Geometry>{
        private Layer layer;
        @Override
        protected Geometry doInBackground(Object... params) {
            String distanceText;
            String idsText;

            if (params[0] instanceof String && params[1] instanceof String && params[2] instanceof Layer) {
                distanceText = (String) params[0];
                idsText = (String) params[1];
                layer = (Layer) params[2];
            } else {
                LOGGER.info("Error - incorrect parameters!");
                return null;
            }

            int distance;
            try {
                distance = Integer.parseInt(distanceText);
            } catch (NumberFormatException e) {
                showError("Error", "Distance must be a number!");
                return null;
            }

            String[] parts = idsText.split("\\,");
            List<Geometry> elements;
            try {
                elements = getGeometriesFromLayer(layer, parts);
            } catch (NumberFormatException e) {
                showError("Error", "Wrong format for ID list!");
                return null;
            } catch (ArrayIndexOutOfBoundsException e) {
                showError("Error", "ID not valid!");
                return null;
            } catch (IndexOutOfBoundsException e) {
                showError("Error", "ID not valid!");
                return null;
            } catch (Exception e) {
                showError("Error", "Unknown error. Message: " + e.getMessage());
                return null;
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    opProgressDialog.setMessage("Buffering in corso...");

                    opProgressDialog.show();
                }
            });

            Geometry buffer;
            try {
                buffer = layer.applyBuffers(elements, distance);
            } catch (InterruptedException e) {
                return null;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    opProgressDialog.dismiss();
                }
            });

            return buffer;
        }

        @Override
        protected void onPostExecute(Geometry geometry) {
            if (geometry != null)
                layer.addGeometry(geometry);
        }

        @Override
        protected void onCancelled() {
            showError("Interrotto", "Operazione interrotta correttamente");
        }
    }

    private class IntersectionTask extends MyAsyncTask <Object,Integer,Geometry>{
        private Layer layer;
        @Override
        protected Geometry doInBackground(Object... params) {
            String idsText;

            if (params[0] instanceof String && params[1] instanceof Layer) {
                idsText = (String) params[0];
                layer = (Layer) params[1];
            } else {
                LOGGER.info("Error - incorrect parameters!");
                return null;
            }

            String[] parts = idsText.split("\\,");
            List<Geometry> elements;
            try {
                elements = getGeometriesFromLayer(layer, parts);
            } catch (NumberFormatException e) {
                showError("Error", "Wrong format for ID list!");
                return null;
            } catch (ArrayIndexOutOfBoundsException e) {
                showError("Error", "ID not valid!");
                return null;
            } catch (IndexOutOfBoundsException e) {
                showError("Error", "ID not valid!");
                return null;
            } catch (Exception e) {
                showError("Error", "Unknown error. Message: " + e.getMessage());
                return null;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    opProgressDialog.setMessage("Intersezione in corso...");

                    opProgressDialog.show();
                }
            });

            Geometry intersection = null;
            try {
                intersection = layer.applyIntersection(elements);
            } catch (InterruptedException e) {
                return null;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    opProgressDialog.dismiss();
                }
            });

            for (Geometry toRemove : elements)
                layer.removeGeometry(toRemove);

            return intersection;
        }

        @Override
        protected void onPostExecute(Geometry geometry) {
            if (geometry != null) {
                layer.addGeometry(geometry);
            }
        }

        @Override
        protected void onCancelled() {
            showError("Interrotto", "Operazione interrotta correttamente");
        }
    }

	/**Si collega all'indirizzo dell'url*/
	private InputStream downloadUrl(String urlString, MyAsyncTask<String, Integer, Boolean> pAsyncTask) throws IOException {
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

        this.drawView = new DrawView(this, layers);
		setContentView(this.drawView);
        this.inDrawView = true;
	}


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!this.inDrawView) {
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
                        new BufferingTask().execute(
                                distanceText.getText().toString(),
                                idsText.getText().toString(),
                                layer);
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
                        new IntersectionTask().execute(
                                idsText.getText().toString(),
                                layer);
                    }
                });

                alert.show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (this.inDrawView) {
            setContentView(R.layout.activity_wfsclient_main);
            this.inDrawView = false;
            this.drawView = null;
            this.requestBoolean = false;
        } else
            super.onBackPressed();
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

    private void showError(final String pTitle, final String pMessage) {
        final Activity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(context)
                        .setTitle(pTitle)
                        .setMessage(pMessage)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }
}
