package com.example.trojan52.mqttsmartdevicecontroller;

import java.io.Serializable;

public class AvailableDeviceInfo implements Serializable {
    private String Name;
    private String Feed;

    public AvailableDeviceInfo(String _name, String _feed) {
        this.Name = _name;
        this.Feed = _feed;
    }

    public void setName(String _name) {
        this.Name = _name;
    }

    public void setFeed(String _feed) {
        this.Feed = _feed;
    }

    public String getName() {
        return this.Name;
    }

    public String getFeed() {
        return this.Feed;
    }

}
