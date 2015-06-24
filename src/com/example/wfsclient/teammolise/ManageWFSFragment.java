package com.example.wfsclient.teammolise;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.wfsclient.R;

import org.geotools.math.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by matt on 6/24/15.
 */
public class ManageWFSFragment extends Fragment {

    private ListView wfsListView;
    private Button addNewBtn;
    private WFSAdapter adapter;
    private List<WFSElement> allWfs;

    private ScrollView scrollView;

    public ManageWFSFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setScrollView(ScrollView scrollView) {
        this.scrollView = scrollView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wfs_management, container, false);

        this.wfsListView = (ListView) view.findViewById(R.id.wfsListView);

        this.allWfs = this.readWFSList();
        this.adapter = new WFSAdapter(getActivity(), this.allWfs);
        this.wfsListView.setAdapter(this.adapter);
        this.adapter.notifyDataSetChanged();
        final Fragment thisFragment = this;
        this.addNewBtn = (Button) view.findViewById(R.id.addNew);
        this.addNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText nameET = new EditText(getActivity());
                nameET.setHint("Nome WFS...");
                final EditText urlET = new EditText(getActivity());
                urlET.setHint("URL...");
                urlET.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(nameET);
                layout.addView(urlET);

                AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(layout).setTitle("Aggiungi WFS").setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = nameET.getText().toString();
                        String url = urlET.getText().toString();
                        if(saveWFSElement(name, url)){
                            dialog.dismiss();
                            scrollView.setVisibility(View.VISIBLE);
                            getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
                        }
                    }
                }).show();
            }
        });

        return view;
    }

    private List<WFSElement> readWFSList() {
        List<WFSElement> list = new ArrayList<WFSElement>();
        list.add(new WFSElement("Un WFS", "http://blog.matteomerola.me/ciao?belli=come+va"));
        list.add(new WFSElement("Un altro WFS", "http://blog.matteomerola.me/ciao?belli=come+va"));
        list.add(new WFSElement("L'ultimo WFS", "http://blog.matteomerola.me/ciao?belli=come+va"));
        return list;
    }

    private boolean saveWFSElement(String name, String url) {
        Pattern p = Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
        Matcher m = p.matcher(url);
        if (name.length() > 1 && m.matches()) {
            Toast.makeText(getActivity(), name + "->" + url, Toast.LENGTH_LONG).show();
            WFSElement wfs = new WFSElement(name, url);
            this.allWfs.add(wfs);
            // TODO inserire salvataggio in memoria persistente
            this.adapter.notifyDataSetChanged();
            return true;
        }
        Toast.makeText(getActivity(), "Controlla il nome e l'url inseriti", Toast.LENGTH_LONG).show();
        return false;
    }
}
