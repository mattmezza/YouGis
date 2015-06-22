package com.example.wfsclient.teammolise;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.wfsclient.R;
import com.example.wfsclient.WFSClientMainActivity;
import com.example.wfsclient.layers.Layer;
import com.vividsolutions.jts.geom.Geometry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 6/22/15.
 */
public class BufferingFragment extends Fragment {


    private EditText name;
    private Spinner spinner;
    private MultiSelectionSpinner objectsSpinner;
    private EditText distanceText;
    private CheckBox selectAllCB;
    private CheckBox dissolveCB;
    private CheckBox saveCB;
    private Button okBtn;
    private Layer selected;
    private BufferOptionCallback callback;

    private List<Layer> layers;

    private static final String LAYERS_KEY = "LAYERS_KEY";


    public BufferingFragment() {
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public void setBufferOptionCallback(BufferOptionCallback bocb) {
        this.callback = bocb;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_buffering_new, container, false);

        name = (EditText) view.findViewById(R.id.name);
        spinner = (Spinner) view.findViewById(R.id.selectLayer);
        ArrayAdapter<Layer> layersAdapter = new ArrayAdapter<Layer>(getActivity(), android.R.layout.simple_spinner_item, layers);
        spinner.setAdapter(layersAdapter);
        layersAdapter.notifyDataSetChanged();
        objectsSpinner = (MultiSelectionSpinner) view.findViewById(R.id.selectGeometry);
        selectAllCB = (CheckBox) view.findViewById(R.id.selectAllObjects);
        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected = (Layer) spinner.getItemAtPosition(position);
                ArrayAdapter<Geometry> adapter = new ArrayAdapter<Geometry>(getActivity(), android.R.layout.simple_spinner_item, selected.getGeometries());
                objectsSpinner.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                objectsSpinner.setVisibility(View.VISIBLE);
                selectAllCB.setVisibility(View.VISIBLE);
            }
        });



        distanceText = (EditText) view.findViewById(R.id.bufferingDistance);
        dissolveCB = (CheckBox) view.findViewById(R.id.dissolve);
        saveCB = (CheckBox) view.findViewById(R.id.save);

        okBtn = (Button) view.findViewById(R.id.okBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameTxt = name.getText().toString();
                List<Integer> selectedGeometryIndices = objectsSpinner.getSelectedIndicies();
                List<Geometry> selectedGeometries = new ArrayList<Geometry>();
                List<Geometry> allGeometries = selected.getGeometries();
                double distance = Double.parseDouble(distanceText.getText().toString());
                for (int index : selectedGeometryIndices) {
                    selectedGeometries.add(allGeometries.get(index));
                }
                if(callback!=null)
                    callback.setBufferingOptions(nameTxt, selected, selectedGeometries, distance, dissolveCB.isSelected(), saveCB.isSelected());
            }
        });

        return view;
    }

}
