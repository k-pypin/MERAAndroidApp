package com.example.trojan52.mqttsmartdevicecontroller;

import android.content.SharedPreferences;

public class ConnectionData {
    private String Host;
    private String HostPref;
    private String Port;
    private String PortPref;
    private String UserName;
    private String UserPref;
    private String Password;
    private String PassPref;
    private SharedPreferences prefs;
    public ConnectionData(SharedPreferences _preferences,String _hostPref, String _portPref, String _userPref, String _passPref) {
        prefs = _preferences;
        this.HostPref = _hostPref;
        this.PortPref = _portPref;
        this.UserPref = _userPref;
        this.PassPref = _passPref;
        updateValues();
    }

    public void updateValues() {
        this.Host = prefs.getString(HostPref, "");
        this.Port = prefs.getString(PortPref, "");
        this.UserName = prefs.getString(UserPref, "");
        this.Password = prefs.getString(PassPref, "");
    }

    public boolean hasEmptyValues() {
        return this.Host.trim().isEmpty() || this.Port.trim().isEmpty() || this.UserName.trim().isEmpty() || this.Password.trim().isEmpty();
    }

    public void setHost(String _host) {
        this.Host = _host;
    }

    public String getHost() {
        return this.Host;
    }

    public void setPort(String _port) {
        this.Port = _port;
    }

    public String getPort() {
        return this.Port;
    }

    public void setUserName(String _user) {
        this.UserName = _user;
    }

    public String getUserName() {
        return this.UserName;
    }

    public void setPassword(String password) {
        this.Password = password;
    }

    public String getPassword() {
        return this.Password;
    }
}
