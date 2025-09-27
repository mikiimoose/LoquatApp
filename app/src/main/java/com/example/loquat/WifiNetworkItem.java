package com.example.loquat;

public class WifiNetworkItem {
    private String ssid;
    private int bars; // Represents signal strength, e.g., 0-4
    private String security; // e.g., "OPEN", "WEP", "WPA2", "WPA3"

    public WifiNetworkItem(String ssid, int bars, String security) {
        this.ssid = ssid;
        this.bars = bars;
        this.security = security;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getBars() {
        return bars;
    }

    public void setBars(int bars) {
        this.bars = bars;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    // Optional: Override toString() for easy logging
    @Override
    public String toString() {
        return "WifiNetworkItem{" +
                "ssid='" + ssid + '\'' +
                ", bars=" + bars +
                ", security='" + security + '\'' +
                '}';
    }
}