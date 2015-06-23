package com.example.wfsclient.teammolise;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.wfsclient.R;
import com.example.wfsclient.layers.Layer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InfoFragment extends Fragment {
    private List<Layer> layers;

    public InfoFragment() {
        this.layers = new ArrayList<Layer>();
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.info_fragment, container, false);

        final Fragment thisInstance = this;

        Button closeBtn = (Button) view.findViewById(R.id.closeBtn);
        LegendView legend = (LegendView) view.findViewById(R.id.legendView);
        legend.setLayers(this.layers);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(thisInstance).commit();
            }
        });

        return view;
    }


}
