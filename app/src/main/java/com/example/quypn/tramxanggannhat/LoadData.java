package com.example.quypn.tramxanggannhat;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


class LoadData extends AsyncTask<String, Void, String> {
    private ProgressDialog progressDialog;
    private FloatingActionButton fab_search;
    private Context context;
    private Geocoder geocoder;
    private ArrayList<Gas_Location> gas_Station_Data;
    private GoogleMap mMap;


    public LoadData(FloatingActionButton fab_search, Context context, Geocoder geocoder, ArrayList<Gas_Location> gas_Station_Data, GoogleMap mMap) {
        this.fab_search = fab_search;
        this.context = context;
        this.geocoder = geocoder;
        this.gas_Station_Data = gas_Station_Data;
        this.mMap = mMap;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Searching...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        fab_search.hide();
        Config.pTheard = true;
    }

    @Override
    protected String doInBackground(String... params) {

        String s = getDataFormServer(params[0]);

        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.optJSONObject(i);
                JSONObject obj_geometry = obj.getJSONObject("geometry");
                JSONObject obj_location = obj_geometry.getJSONObject("location");

                String lat = obj_location.getString("lat");
                String lng = obj_location.getString("lng");

                String url_icon = obj.getString("icon");
                String id = obj.getString("id");
                String name = obj.getString("name");

                String address = "";


                List<Address> list_bf = geocoder.getFromLocation(Double.valueOf(lat), Double.valueOf(lng), 1);

                String distance_url =
                        getDataFormServer("https://maps.googleapis.com/maps/api/directions/json?origin="
                                + Config.lat + "," + Config.lng +
                                "&destination=" +
                                lat +
                                "," +
                                lng +
                                "&sensor=false");
                JSONObject json = new JSONObject(distance_url);
                JSONArray array = json.getJSONArray("routes");

                JSONObject routes = array.getJSONObject(0);
                JSONArray legs = routes.getJSONArray("legs");
                JSONObject sublegs = legs.optJSONObject(0);
                JSONObject distance = sublegs.getJSONObject("distance");
                JSONObject duration = sublegs.getJSONObject("duration");
                String duration_klm = distance.getString("text");
                String duration_min = duration.getString("text");
                String distnce_number  = distance.getString("value");


                switch (list_bf.size()) {
                    case 0:
                        break;
                    case 1:
                        address += list_bf.get(0).getAddressLine(0);
                    case 2:
                        address += ", " + list_bf.get(0).getAddressLine(1);
                    case 3:
                        address += ", " + list_bf.get(0).getAddressLine(2);
                        break;

                }


                gas_Station_Data.add(new Gas_Location(url_icon, id, name, lat, lng, address, duration_klm, duration_min,Integer.valueOf(distnce_number)));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        int min_klm = 0;

        if (gas_Station_Data.size()>=1)
        {
             min_klm = gas_Station_Data.get(0).getDistance_number();
        }


        for (int i=0;i<gas_Station_Data.size();i++)
        {
            if (min_klm > gas_Station_Data.get(i).getDistance_number())
            {
                min_klm = gas_Station_Data.get(i).getDistance_number();
            }
        }

        for (int i = 0; i < gas_Station_Data.size(); i++) {
            LatLng latLng = new
                    LatLng(Double.valueOf(gas_Station_Data.get(i).getLocation_lat()),
                    Double.valueOf(gas_Station_Data.get(i).getLocation_lng()));

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.draggable(true);

            String klm = gas_Station_Data.get(i).getDistance();
            String min = gas_Station_Data.get(i).getDuration();


            if (gas_Station_Data.get(i).getDistance_number() == min_klm) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_gas_station_nearest));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_gas_station));
            }


            markerOptions.title(klm + "-" + min + "|" + gas_Station_Data.get(i).getGas_name()).snippet(gas_Station_Data.get(i).getAddresses());
            Marker marker = mMap.addMarker(markerOptions);
            marker.showInfoWindow();
        }

        progressDialog.dismiss();
        Config.pTheard = false;
        fab_search.show();

    }

    private String getDataFormServer(String link) {
        String json = "";
        try {
            URL url = new URL(link);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(12000);
            urlConnection.setDoInput(true);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    json += line;
                    line = bufferedReader.readLine();
                }
                inputStreamReader.close();
                bufferedReader.close();

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
}

