package com.example.wfsclient.teammolise;

import java.net.URL;

/**
 * Created by matt on 6/24/15.
 */
public class WFSElement {
    private String name;
    private String url;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WFSElement that = (WFSElement) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(url != null ? !url.equals(that.url) : that.url != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WFSElement() {

    }

    public WFSElement(String name, String url) {

        this.name = name;
        this.url = url;
    }
}
