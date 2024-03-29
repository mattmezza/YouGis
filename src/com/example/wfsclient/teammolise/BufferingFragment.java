package com.example.wfsclient.teammolise;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
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
public class BufferingFragment extends Fragment {


    private EditText name;
    private Spinner spinner;
    private MultiSelectionSpinner objectsSpinner;
    private EditText distanceText;
    private EditText segmentsNumber;
    private CheckBox selectSpecificGeometries;
    private CheckBox dissolveCB;
    private CheckBox saveCB;
    private Button okBtn;
    private Button closeBtn;
    private Layer selected;
    private BufferOptionCallback callback;

    private LinearLayout singleGeometriesLayout;

    private DrawView drawView;

    public BufferingFragment() {
    }

    public void setDrawView(DrawView drawView) {
        this.drawView = drawView;
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
        View view = inflater.inflate(R.layout.buffering_fragment, container, false);

        name = (EditText) view.findViewById(R.id.name);
        spinner = (Spinner) view.findViewById(R.id.selectLayer);
        ArrayAdapter<Layer> layersAdapter = new ArrayAdapter<Layer>(getActivity(), android.R.layout.simple_spinner_item, drawView.getLayers());
        spinner.setAdapter(layersAdapter);
        layersAdapter.notifyDataSetChanged();
        objectsSpinner = (MultiSelectionSpinner) view.findViewById(R.id.selectGeometry);

        selectSpecificGeometries = (CheckBox) view.findViewById(R.id.selectAllObjects);
        singleGeometriesLayout = (LinearLayout) view.findViewById(R.id.singleGeometriesLayer);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = (Layer) spinner.getItemAtPosition(position);
                int geomSize = selected.getGeometries().size();
                String[] ids = new String[geomSize];
                for (int i = 0; i < geomSize; i++) {
                    ids[i] = String.valueOf(i);
                }
                objectsSpinner.setItems(ids);
                objectsSpinner.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                objectsSpinner.setEnabled(false);
            }
        });
        selectSpecificGeometries.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    singleGeometriesLayout.setVisibility(View.VISIBLE);
                else
                    singleGeometriesLayout.setVisibility(View.GONE);
            }
        });

        distanceText = (EditText) view.findViewById(R.id.bufferingDistance);
        segmentsNumber = (EditText) view.findViewById(R.id.segmentsNumber);
        dissolveCB = (CheckBox) view.findViewById(R.id.dissolve);
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

                List<Integer> selectedGeometryIndices = objectsSpinner.getSelectedIndicies();
                List<Geometry> selectedGeometries = new ArrayList<Geometry>();
                List<Geometry> allGeometries = selected.getGeometries();
                if(!selectSpecificGeometries.isChecked()) {
                    selectedGeometries.addAll(allGeometries);
                } else {
                    for (int index : selectedGeometryIndices) {
                        selectedGeometries.add(allGeometries.get(index));
                    }
                }
                int segments;
                double distance;
                try {
                    distance = Double.parseDouble(distanceText.getText().toString());
                    if (segmentsNumber.getText().toString().isEmpty())
                        segments = 8;
                    else
                        segments = Integer.parseInt(segmentsNumber.getText().toString());
                    if(callback!=null)
                        callback.setBufferingOptions(nameTxt, selected, selectedGeometries, distance, segments, dissolveCB.isChecked(), saveCB.isChecked());
                } catch(NumberFormatException e) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Attenzione")
                            .setMessage("Il formato della distanza non è corretto.")
                            .show();
                }
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

}
