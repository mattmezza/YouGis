package com.example.wfsclient.teammolise;

/**
 * Created by simone on 24/06/15.
 */
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by matt on 4/23/15.
 */
public class OurSharedPreferences  {
    private static final String SHARED_PREFERENCES_CACHE_NAME = "MAL_SEBI";

    private SharedPreferences sp;

    public OurSharedPreferences(Context ctx) {
        this.sp = ctx.getSharedPreferences(SHARED_PREFERENCES_CACHE_NAME, Context.MODE_PRIVATE);
    }

    public boolean addWFS(WFSElement element) {
        try {
            if (!this.sp.getString(element.getName(), "").equalsIgnoreCase("")) {
                return false;
            }

            SharedPreferences.Editor editor = this.sp.edit();
            editor.putString(element.getName(), element.getUrl());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<WFSElement> getWFSs() {
        List<WFSElement> elements = new ArrayList<WFSElement>();
        Map<String, ?> all = this.sp.getAll();

        for (String label : all.keySet()) {
            WFSElement element = new WFSElement(label, "");
            Object url = all.get(label);
            if (url instanceof String) {
                String realUrl = (String)url;
                element.setUrl(realUrl);
            }

            elements.add(element);
        }

        return elements;
    }

    public boolean removeWFS(String pLabel) {
        try {
            SharedPreferences.Editor editor = this.sp.edit();

            editor.remove(pLabel);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}