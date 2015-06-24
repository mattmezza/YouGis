package com.example.wfsclient.teammolise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wfsclient.R;

import java.util.List;

/**
 * Created by matt on 6/24/15.
 */
public class WFSAdapter extends BaseAdapter {

    private List<WFSElement> allWFS;

    private LayoutInflater inflater;
    private Context ctx;

    private TextView nameTV;
    private TextView urlTV;

    public WFSAdapter(Context ctx, List<WFSElement> allWFS) {
        this.allWFS = allWFS;
        this.ctx = ctx;
        this.inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return this.allWFS.size();
    }

    @Override
    public Object getItem(int position) {
        return this.allWFS.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        view = this.inflater.inflate(R.layout.single_wfs_element, parent, false);
        this.nameTV = (TextView) view.findViewById(R.id.wfsName);
        this.urlTV = (TextView) view.findViewById(R.id.wfsUrl);
        final WFSElement wfs = this.allWFS.get(position);
        if(wfs!=null) {
            this.nameTV.setText(wfs.getName());
            this.urlTV.setText(wfs.getUrl());
        }
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(ctx)
                        .setTitle("Conferma")
                        .setMessage("Vuoi veramente cancellare il WFS "+wfs+"?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Sì", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeWFSElement(wfs);
                            }
                        }).show();
                return true;
            }
        });
        return view;
    }

    @Override
    public boolean isEmpty() {
        return this.allWFS.isEmpty();
    }

    public List<WFSElement> getAllWFS() {
        return allWFS;
    }

    private void removeWFSElement(WFSElement wfs) {
        Toast.makeText(this.ctx, "Removed "+wfs, Toast.LENGTH_LONG).show();
    }
}
