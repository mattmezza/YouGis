package com.example.wfsclient;

import android.os.AsyncTask;

/**
 * Created by simone on 16/06/15.
 */
public abstract class MyAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    public void publish(Progress pProgress) {
        this.publishProgress(pProgress);
    }
}
