package com.example.wfsclient.teammolise;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.wfsclient.DrawView;
import com.example.wfsclient.R;
import com.example.wfsclient.layers.Layer;
import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 6/22/15.
 */
public class IntersectionFragment extends Fragment {


    private EditText name;
    private Spinner spinner1;
    private MultiSelectionSpinner objectsSpinner1;
    private CheckBox selectAllCB1;

    private Spinner spinner2;
    private MultiSelectionSpinner objectsSpinner2;
    private CheckBox selectAllCB2;

    private CheckBox saveCB;
    private Button okBtn;
    private Button closeBtn;

    private Layer selected1;
    private Layer selected2;
    private IntersectionOptionCallback callback;

    private DrawView drawView;

    public IntersectionFragment() {
    }

    public void setDrawView(DrawView drawView) {
        this.drawView = drawView;
    }

    public void setBufferOptionCallback(IntersectionOptionCallback bocb) {
        this.callback = bocb;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.intersection_fragment, container, false);

        name = (EditText) view.findViewById(R.id.name);
        spinner1 = (Spinner) view.findViewById(R.id.selectLayer1);
        spinner2 = (Spinner) view.findViewById(R.id.selectLayer2);

        ArrayAdapter<Layer> layersAdapter1 = new ArrayAdapter<Layer>(getActivity(), android.R.layout.simple_spinner_item, drawView.getLayers());
        spinner1.setAdapter(layersAdapter1);

        ArrayAdapter<Layer> layersAdapter2 = new ArrayAdapter<Layer>(getActivity(), android.R.layout.simple_spinner_item, drawView.getLayers());
        spinner2.setAdapter(layersAdapter2);

        layersAdapter1.notifyDataSetChanged();
        layersAdapter2.notifyDataSetChanged();

        objectsSpinner1 = (MultiSelectionSpinner) view.findViewById(R.id.selectGeometry1);
        objectsSpinner2 = (MultiSelectionSpinner) view.findViewById(R.id.selectGeometry2);

        selectAllCB1 = (CheckBox) view.findViewById(R.id.selectAllObjects1);
        selectAllCB2 = (CheckBox) view.findViewById(R.id.selectAllObjects2);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected1 = (Layer) spinner1.getItemAtPosition(position);
                int geomSize = selected1.getGeometries().size();
                String[] ids = new String[geomSize];
                for (int i = 0; i < geomSize; i++) {
                    ids[i] = String.valueOf(i);
                }
                objectsSpinner1.setItems(ids);
                objectsSpinner1.setEnabled(true);
                selectAllCB1.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                objectsSpinner1.setEnabled(false);
                selectAllCB1.setEnabled(false);
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected2 = (Layer) spinner2.getItemAtPosition(position);
                int geomSize = selected2.getGeometries().size();
                String[] ids = new String[geomSize];
                for (int i = 0; i < geomSize; i++) {
                    ids[i] = String.valueOf(i);
                }
                objectsSpinner2.setItems(ids);
                objectsSpinner2.setEnabled(true);
                selectAllCB2.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                objectsSpinner2.setEnabled(false);
                selectAllCB2.setEnabled(false);
            }
        });

        selectAllCB1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                objectsSpinner1.setEnabled(!isChecked);
            }
        });
        selectAllCB2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                objectsSpinner2.setEnabled(!isChecked);
            }
        });


        saveCB = (CheckBox) view.findViewById(R.id.save);

        okBtn = (Button) view.findViewById(R.id.okBtn);
        closeBtn = (Button) view.findViewById(R.id.closeBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameTxt = name.getText().toString();
                if (nameTxt.length()<2) {
                    AlertDialog dialog = new AlertDialog.Builder(getActivity())
                            .setTitle("Attenzione")
                            .setMessage("Inserisci un nome per il nuovo layer (almeno due caratteri).")
                            .show();
                    return;
                }

                List<Geometry> selectedGeometries1 = getGeometries(objectsSpinner1, selected1, selectAllCB1.isChecked());
                List<Geometry> selectedGeometries2 = getGeometries(objectsSpinner2, selected2, selectAllCB2.isChecked());

                if(callback!=null)
                    callback.setIntersectionOptions(nameTxt,
                            selected1, selectedGeometries1,
                            selected2, selectedGeometries2,
                            saveCB.isChecked());
                else
                    Log.d("Error", "No callback!!!");
            }
        });

        final Fragment thisInstance = this;
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(thisInstance).commit();
            }
        });

        return view;
    }

    private List<Geometry> getGeometries(MultiSelectionSpinner spinner, Layer currentLayer, boolean selectAll) {
        List<Integer> selectedGeometryIndices = spinner.getSelectedIndicies();
        List<Geometry> selectedGeometries = new ArrayList<Geometry>();
        List<Geometry> allGeometries = currentLayer.getGeometries();
        if(selectAll) {
            selectedGeometries.addAll(allGeometries);
        } else {
            for (int index : selectedGeometryIndices) {
                selectedGeometries.add(allGeometries.get(index));
            }
        }

        return selectedGeometries;
    }

}
