package com.example.quypn.tramxanggannhat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.example.quypn.tramxanggannhat.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ArrayList<Gas_Location> gas_Station_Data;
    private Geocoder geocoder;
    private FloatingActionButton fab_search;
    private Toolbar toolbar;
    private Polyline polyline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        CheckPermission();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Tìm cây xăng gần nhất");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(16);
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        fab_search = (FloatingActionButton) findViewById(R.id.btn_search);
        fab_search.hide();
        fab_search.setOnClickListener(Onclick_search);
        mapFragment.getMapAsync(this);

    }

    public void CheckPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!HasPermission()) {
                RequestPermission();
            }
        }
    }

    private boolean HasPermission() {
        int res = 0;
        String[] permission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        for (String i : permission) {
            res = checkCallingOrSelfPermission(i);
            if (res != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void RequestPermission() {
        String[] permission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permission, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        gas_Station_Data = new ArrayList<>();
        mMap = googleMap;
        mMap.clear();

        geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);


        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                fab_search.show();
                Config.lat = googleMap.getMyLocation().getLatitude();
                Config.lng = googleMap.getMyLocation().getLongitude();
                return false;
            }
        });

        mMap.setOnMarkerClickListener(DrawPath_Onclick);


    }

    GoogleMap.OnMarkerClickListener DrawPath_Onclick = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            String url_jon = "https://maps.googleapis.com/maps/api/directions/json?origin="
                            + Config.lat + "," + Config.lng +
                            "&destination=" +
                            marker.getPosition().latitude +
                            "," +
                            marker.getPosition().longitude +
                            "&sensor=false";
            if (polyline!=null) polyline.remove();

            new ParserTask().execute(url_jon);
            return false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean allowed = true;
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                for (int res : grantResults) {
                    allowed = (allowed && (res == PackageManager.PERMISSION_GRANTED));
                }
                break;
            default:
                allowed = false;
                break;
        }
        if (!allowed) {
            finish();
        }


    }

    View.OnClickListener Onclick_search = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                    mMap.getMyLocation().getLatitude() +
                    "," +
                    mMap.getMyLocation().getLongitude() +
                    "&radius=5000&types=gas_station&key=AIzaSyCULWp8li4v8NuFykhfLWQvaVr6QlJjbtI";

            mMap.clear();
            gas_Station_Data = new ArrayList<>();
            new LoadData(fab_search, MapsActivity.this, geocoder, gas_Station_Data, mMap).execute(url);
        }
    };


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {


        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(getDataFormServer(jsonData[0]));
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.geodesic(true);
            }
            lineOptions.color(getResources().getColor(R.color.colorPrimaryDark));
            polyline = mMap.addPolyline(lineOptions);
        }
    }

    private String getDataFormServer(String link) {
        String json = "";
        try {
            URL url = new URL(link);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(600000);
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
