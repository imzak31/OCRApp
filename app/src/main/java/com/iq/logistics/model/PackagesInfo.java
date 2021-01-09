package com.iq.logistics.model;

import java.util.ArrayList;

public class PackagesInfo {
    public String locker;
    public String tracking;
    public String description;
    public ArrayList<String> base64img = new ArrayList<>();

    public PackagesInfo(String locker, String tracking, String description, ArrayList<String> base64img) {
        this.locker = locker;
        this.tracking = tracking;
        this.description = description;
        this.base64img = base64img;
    }

    public PackagesInfo(String locker, String tracking, String description) {
        this.locker = locker;
        this.tracking = tracking;
        this.description = description;
    }

    public String getLocker() {
        return locker;
    }

    public String getTracking() {
        return tracking;
    }

    public String getDescription() { return description; }

    public ArrayList<String> getBase64img() {
        return base64img;
    }
}
