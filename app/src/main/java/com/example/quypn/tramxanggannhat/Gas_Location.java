package com.example.quypn.tramxanggannhat;

/**
 * Created by QuyPN on 4/12/2017.
 */

public class Gas_Location {

    private String gas_url_icon;
    private String gas_id;
    private String gas_name;
    private String location_lat;
    private String location_lng;
    private String addresses;
    private String distance;
    private String duration;
    private int distance_number;

    public Gas_Location(String gas_url_icon, String gas_id, String gas_name, String location_lat, String location_lng,String addresses) {
        this.gas_url_icon = gas_url_icon;
        this.gas_id = gas_id;
        this.gas_name = gas_name;
        this.location_lat = location_lat;
        this.location_lng = location_lng;
        this.addresses = addresses;

    }

    public Gas_Location(String gas_url_icon, String gas_id, String gas_name, String location_lat,
                        String location_lng, String addresses, String distance, String duration, int distance_number) {
        this.gas_url_icon = gas_url_icon;
        this.gas_id = gas_id;
        this.gas_name = gas_name;
        this.location_lat = location_lat;
        this.location_lng = location_lng;
        this.addresses = addresses;
        this.distance = distance;
        this.duration = duration;
        this.distance_number = distance_number;
    }

    public int getDistance_number() {
        return distance_number;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public String getGas_url_icon() {
        return gas_url_icon;
    }

    public String getGas_id() {
        return gas_id;
    }

    public String getGas_name() {
        return gas_name;
    }

    public String getLocation_lat() {
        return location_lat;
    }

    public String getLocation_lng() {
        return location_lng;
    }
}
