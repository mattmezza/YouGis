package com.example.wfsclient;
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
import java.util.Random;
import java.util.logging.Logger;

import com.example.wfsclient.layers.Layer;
import com.example.wfsclient.teammolise.BufferOptionCallback;
import com.example.wfsclient.teammolise.BufferingFragment;
import com.example.wfsclient.teammolise.IntersectionFragment;
import com.example.wfsclient.teammolise.IntersectionOptionCallback;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WFSClientMainActivity extends Activity implements BufferOptionCallback, IntersectionOptionCallback {

	private final static Logger LOGGER = Logger.getLogger(WFSClientMainActivity.class.getName());
	
	private boolean wifiConnected=false;
	private boolean mobileConnected=false;
	private LinkedList<Object> listaOggetti=new LinkedList<Object>();
	private boolean disegna=false;
    private boolean aggiorna=false;
    private boolean addLayer=false;
    private List<Layer> currentLayers;
	private ProgressDialog progressDialog;
    private ProgressDialog dlProgressDialog;

	private String defaultwfs = null;
    private Map<String, String> wfsList;

	private static String wfsVersion = "1.1.0";
	private List<String> feature;//l'index 0 contiene l'ind del wfs
	private String request="";
    private String requestName="";
	private boolean requestBoolean=false;
    private boolean inDrawView;
    private DrawView drawView;
    private ProgressDialog opProgressDialog;

    private void createMenuEntries() {
        wfsList = new HashMap<String, String>();
        wfsList.put("North atlas", "http://nsidc.org/cgi-bin/atlas_north?service=WFS&request=GetCapabilities");
        wfsList.put("Sardegna", "http://webgis.regione.sardegna.it/geoserver/wfs?service=WFS&request=GetCapabilities");
        wfsList.put("Atlanta", "http://www.geoportal.rgurs.org/erdas-apollo/vector/ATLGML3_SHAPE?service=WFS&request=GetCapabilities");

        wfsList.put("Torino - Azzonamenti sanitari", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc35_azzonamenti_sanitari?service=WFS&request=getCapabilities");
        wfsList.put("Torino - Carceri", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc122_carceri?service=WFS&request=getCapabilities");
        wfsList.put("Torino - Polizia amministrativa", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc48_polizia_amm?service=WFS&request=getCapabilities");
        wfsList.put("Torino - Religione", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc116_chiese?service=WFS&request=getCapabilities");
        wfsList.put("Torino - Ospedali", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc112_ospedali?service=WFS&request=getCapabilities");
        wfsList.put("Torino - Cartografia", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc01_dati_di_base?service=WFS&request=getCapabilities");
        wfsList.put("Torino - Farmacie", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc102_farmacie?service=WFS&request=getCapabilities");
        wfsList.put("Torino - ASL", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc106_sedi_asl?service=WFS&request=getCapabilities");
        wfsList.put("Torino - zone censuarie", "http://geomap.reteunitaria.piemonte.it/ws/siccms/coto-01/wfsg01/wfs_sicc18_microzone_censuarie?service=WFS&request=getCapabilities");

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

        this.currentLayers = new ArrayList<Layer>();
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
				new DownloadXmlTask().execute(request, requestName);
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
                stream = downloadUrl(urlString);
                feature = ParserCapabilities.parse(stream);

                return true;
            } catch (SocketTimeoutException e) {
                showError("Errore", "Connessione scaduta. Riprovare più tardi.");
                return false;
            }catch(Exception e){
                showError("Errore", "Errore sconosciuto. Controllare il log per ulteriori dettagli.");
                e.printStackTrace();
                return false;
            }finally{
                // Chiusura dell'INPUT STREAM
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        LOGGER.info(e.getMessage());
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
                showError("Errore", "Nessuna feature disponibile per questo WFS.");
                return;
            }

            showFeatureSelector();
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

        String name;

		@Override
		protected Boolean doInBackground(String... urls) {
            String urlString = urls[0];
            name = urls[1];
            InputStream stream = null;

			try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dlProgressDialog.setMessage("Download delle feature in corso...");
                        dlProgressDialog.show();
                    }
                });
                stream=downloadUrl(urlString);
                if(!disegna)
                    XMLParser.parse(stream);
                else{

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dlProgressDialog.dismiss();
                            dlProgressDialog.setMessage("Analisi del file in corso...");
                            dlProgressDialog.show();
                        }
                    });
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
                showError("Error", "Errore di connessione.");
                return false;
            }catch(Exception e){
                requestBoolean = false;
                showError("Error", "Errore sconosciuto. Controllare il log per ulteriori dettagli.");
                e.printStackTrace();
                return false;
            }finally{
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        LOGGER.info(e.getMessage());
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

            try {
                LOGGER.info("INVOCO LA VIEW");
                disegnaOnView(listaOggetti, name);
            } catch (ParseException e) {
                showError("Error", "Errore sconosciuto. Controllare il log per ulteriori dettagli.");
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            LOGGER.info(Arrays.toString(values));
            super.onProgressUpdate(values);
        }

    }

    public void setBufferingOptions(String nameTxt, Layer selected, List<Geometry> selectedGeometries, double distance, int segments, boolean dissolve, boolean save) {
        if(!isUniqueLayerName(nameTxt))
            return;
        new BufferingTask().execute(
                distance,
                selectedGeometries,
                selected,
                dissolve, save, nameTxt, segments);
    }
    
    private class BufferingTask extends MyAsyncTask <Object,Integer,Layer>{
        private Layer layer;
        @Override
        protected Layer doInBackground(Object... params) {
            Double distance;
            List<Geometry> geometries;
            boolean save;
            boolean dissolve;
            int segments;
            String name;

            if (params[0] instanceof Double && params[1] instanceof List && params[2] instanceof Layer && params[3] instanceof Boolean && params[4] instanceof Boolean && params[5] instanceof String && params[6]!=null) {
                distance = (Double) params[0];
                geometries = (List<Geometry>) params[1];
                layer = (Layer) params[2];
                save = (Boolean) params[4];
                dissolve = (Boolean) params[3];
                name = (String) params[5];
                segments = (Integer) params[6];
            } else {
                LOGGER.info("Error - incorrect parameters!");
                return null;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    opProgressDialog.setMessage("Buffering in corso...");

                    opProgressDialog.show();
                }
            });

            Layer buffer;
            try {
                buffer = layer.applyBuffer(geometries, distance, dissolve, segments);
                buffer.setName(name);
                if(save) {
                    try {
                        buffer.save(buffer.getName() + new Random().nextInt(100));
                    } catch (IOException e) {
                        showError("Attenzione", "Non è stato possibile salvare il file.");
                        e.printStackTrace();
                    }
                }
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
        protected void onPostExecute(Layer layer) {
            if (layer != null) {
                drawView.addLayer(layer);
                Toast.makeText(getApplicationContext(), "Buffering completato. " ,Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            showError("Interrotto", "Operazione interrotta correttamente");
        }
    }

    private boolean isUniqueLayerName(String layerName) {
        for(Layer l : this.drawView.getLayers()) {
            if(l.getName().equals(layerName)) {
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Errore")
                        .setMessage("Specificare un nome univoco per il nuovo livello")
                        .show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void setIntersectionOptions(String nameTxt,
                                       Layer selected1, List<Geometry> selectedGeometries1,
                                       Layer selected2, List<Geometry> selectedGeometries2,
                                       boolean save) {
        if(!isUniqueLayerName(nameTxt))
            return;
        new IntersectionTask().execute(nameTxt,
                selected1, selectedGeometries1,
                selected2, selectedGeometries2,
                save);
    }

    private class IntersectionTask extends MyAsyncTask <Object,Integer,Layer>{
        @Override
        protected Layer doInBackground(Object... params) {
            String layerName;
            Layer selected1;
            Layer selected2;
            List<Geometry> selectedGeometries1;
            List<Geometry> selectedGeometries2;
            boolean save;

            if (params[0] instanceof String &&
                    params[1] instanceof Layer && params[2] instanceof List &&
                    params[3] instanceof Layer && params[4] instanceof List &&
                    params[5] instanceof Boolean) {
                layerName = (String)params[0];
                selected1 = (Layer)params[1];
                selectedGeometries1 = (List<Geometry>)params[2];
                selected2 = (Layer)params[3];
                selectedGeometries2 = (List<Geometry>)params[4];
                save = (Boolean)params[5];
            } else {
                LOGGER.info("Error - incorrect parameters!");
                return null;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    opProgressDialog.setMessage("Intersezione in corso...");
                    opProgressDialog.show();
                }
            });

            Layer intersection;
            try {
                intersection = selected1.applyIntersection(selectedGeometries1, selectedGeometries2);
            } catch (InterruptedException e) {
                return null;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    opProgressDialog.dismiss();
                }
            });

            intersection.setName(layerName);
            if(save) {
                try {
                    intersection.save(intersection.getName() + new Random().nextInt(100));
                } catch (IOException e) {
                    showError("Attenzione", "Non è stato possibile salvare il file.");
                    e.printStackTrace();
                }
            }
            return intersection;
        }

        @Override
        protected void onPostExecute(Layer layer) {
            if (layer != null) {
                drawView.addLayer(layer);
                Toast.makeText(getApplicationContext(), "Intersezione completata." ,Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            showError("Interrotto", "Operazione interrotta correttamente");
        }
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

        return conn.getInputStream();
	}
	/**Flag per distinguere quale bottone � stato utilizzato*/
	public void disegna(View view){
		this.disegna=true;
        this.aggiorna = false;
        this.requestBoolean = false;
        if (view.getId() == R.id.addLayer) {
            this.addLayer = true;
            this.aggiorna = true;
        } else
            this.addLayer = false;

		startConnection();
	}

	/**Invoca la View per disegnare la Feature*/
	public void disegnaOnView(LinkedList<Object> l, String name) throws ParseException{
        final Layer currentLayer = new Layer();
        for (Object o : l)
            if (o instanceof Geometry)
                currentLayer.addGeometry((Geometry)o);

        if (!this.addLayer)
            this.currentLayers.clear();

        currentLayer.setName(name);
        this.currentLayers.add(currentLayer);

        if (!this.aggiorna) {
            initializeDrawView();
        } else
            this.drawView.addLayer(currentLayer);

        this.inDrawView = true;

	}

    private void initializeDrawView() {
        setContentView(R.layout.drawing_layout);
        this.drawView = (DrawView) findViewById(R.id.drawView);
        this.drawView.setLayers(this.currentLayers);

        Button addLayer = (Button) findViewById(R.id.addLayer);
        Button removeLayer = (Button) findViewById(R.id.removeLayer);
        addLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                requestBoolean = false;
                showWFSSelector(new Runnable() {
                    @Override
                    public void run() {
                        disegna(v);
                    }
                });
            }
        });
        final Activity act = this;
        removeLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawView.getLayers().size()<=1) {
                    Toast.makeText(act, "Non puoi rimuovere l'ultimo layer", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String[] options = new String[drawView.getLayers().size()];
                for (int i = 0; i < drawView.getLayers().size(); i++)
                    options[i] = drawView.getLayers().get(i).getName();
                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                builder.setTitle("Seleziona un layer");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String name = options[item];
                        List<Layer> tempLayers = new ArrayList<Layer>(drawView.getLayers());
                        for(Layer l : tempLayers) {
                            if(l.getName().equals(name)&&drawView.getLayers().size()>1) {
                                if(drawView.removeLayer(l))
                                    Toast.makeText(act, "Layer "+name+" rimosso con successo!", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(act, "Non è possibile rimuovere il layer "+name+"...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).show();
            }
        });
    }

    public void disegnaRequest(final View pView) {
        showWFSSelector(new Runnable() {
            @Override
            public void run() {
                disegna(pView);
            }
        });
    }

    public void showFeatureSelector() {
        final String baseUrl = feature.get(0);

        final String[] options = new String[feature.size()-1];

        for (int i = 1; i < feature.size(); i++)
            options[i-1] = feature.get(i);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selezionare una feature");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                request = baseUrl +
                        (baseUrl.endsWith("?") ? "" : "?") +
                        "service=WFS&version=" + wfsVersion +
                        "&request=GetFeature&typeName=" + options[item];
                requestName = options[item];
                LOGGER.info("REQUEST " + item + feature.toString());
                requestBoolean=true;
                startConnection();//Ricontrolla la connessione e avvia il download della feature
            }
        }).show();
    }

    private boolean showWFSSelector(final Runnable runnable) {
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
                Toast.makeText(getApplicationContext(), "Selezionato il WFS "+items[item], Toast.LENGTH_SHORT).show();
                if(runnable!=null)
                    runOnUiThread(runnable);
            }
        }).show();

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!inDrawView)
            return false;
        
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction;
        switch (item.getItemId()) {
            case R.id.action_Buffer:

                BufferingFragment bufferingFragment = new BufferingFragment();
                bufferingFragment.setDrawView(drawView);
                bufferingFragment.setBufferOptionCallback(this);

                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.fragmentContainer, bufferingFragment, "BUFFERING_FRAGMENT");
                fragmentTransaction.commit();
                return true;
            case R.id.action_Intersection:
                IntersectionFragment intersectionFragment = new IntersectionFragment();
                intersectionFragment.setDrawView(drawView);
                intersectionFragment.setBufferOptionCallback(this);

                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.fragmentContainer, intersectionFragment, "INTERSECTION_FRAGMENT");
                fragmentTransaction.commit();

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
