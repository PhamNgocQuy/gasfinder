package com.example.quypn.tramxanggannhat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.NativeAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.M;
import static com.example.quypn.tramxanggannhat.R.id.map;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;


    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 101;
    private ArrayList<Gas_Location> gas_Station_Data;
    private Geocoder geocoder;
    private FloatingActionButton fab;
    private AdView adView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CheckPermission();

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.adViewContainer);
        adView = new AdView(this, "282331202206878_337239126716085", AdSize.BANNER_320_50);
        relativeLayout.addView(adView);
        adView.loadAd();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(Search_OnClick);
        fab.hide();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        drawer.setClickable(false);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        init();


    }

    public void init() {

        if (!isNetworkConected(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Has no internet connected");
            builder.setNegativeButton("Connect to internet", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                    finish();
                }

            });
            builder.create().show();
        }

        SupportMapFragment mapFragment;
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.find_3km:
                Config.find_Km = "3000";
                Search_Station();

                break;
            case R.id.find_5km:
                Config.find_Km = "5000";
                Search_Station();

                break;
            case R.id.find_10km:
                Config.find_Km = "10000";
                Search_Station();

                break;
            case R.id.find_20km:
                Config.find_Km = "20000";
                Search_Station();

                break;
            case R.id.nav_send:
                Send_My_Location();
                break;
            case R.id.nav_share:

                break;
            case R.id.map_type_hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.map_type_none:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.map_type_normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.map_type_satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.map_type_terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {


        gas_Station_Data = new ArrayList<>();
        mMap = googleMap;
        mMap.clear();
        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            CheckPermission();
            return;
        }


        mMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (googleMap.getMyLocation() != null) {
                    Config.lat = googleMap.getMyLocation().getLatitude();
                    Config.lng = googleMap.getMyLocation().getLongitude();
                    fab.show();
                } else {
                    Toast.makeText(MainActivity.this, "Connection is slow, please wait", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
        mMap.setOnMarkerClickListener(DrawPath_Onclick);
    }

    public void CheckPermission() {
        if (Build.VERSION.SDK_INT >= M) {
            if (!HasPermission()) {
                RequestPermission();
            }
        }
    }

    private boolean HasPermission() {
        int res;
        String[] permission = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        for (String i : permission) {
            res = checkCallingOrSelfPermission(i);
            if (res != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void RequestPermission() {
        String[] permission = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        if (Build.VERSION.SDK_INT >= M) {
            requestPermissions(permission, PERMISSION_REQUEST_CODE);
        }
    }

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

    View.OnClickListener Search_OnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Search_Station();
        }
    };

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
            if (Config.polyline != null) Config.polyline.remove();
            new ParserTask(MainActivity.this, mMap).execute(url_jon);
            return false;
        }
    };

    public void Search_Station() {

        if (!Config.pTheard && mMap.getMyLocation() != null) {
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                    mMap.getMyLocation().getLatitude() +
                    "," +
                    mMap.getMyLocation().getLongitude() +
                    "&radius=" +
                    Config.find_Km +
                    "&types=gas_station&key=AIzaSyBYMKeItRZIn_OoQGTp09L1i6ZumrY0LvM";

            mMap.clear();
            gas_Station_Data = new ArrayList<>();
            new LoadData(fab, MainActivity.this, geocoder, gas_Station_Data, mMap).execute(url);
        } else {
            Toast.makeText(this, "Connection is slow, please wait", Toast.LENGTH_SHORT).show();
        }

    }

    public void Send_My_Location() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, Config.lat.toString() + " , " + Config.lng.toString());
        startActivity(Intent.createChooser(intent, "Send to:"));
    }

    public boolean isNetworkConected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    private void TuiDangODau() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            CheckPermission();
        }

        mMap.setMyLocationEnabled(true);
        LatLng latLng = new LatLng(Config.lat, Config.lng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to location user
                .zoom(15)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        fab.show();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {


        if (ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            CheckPermission();
            return;
        }

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (lastLocation != null) {

            Config.lat = lastLocation.getLatitude();
            Config.lng = lastLocation.getLongitude();
            TuiDangODau();

        } else {

            SettingRequest();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w("MapError", "MappError");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "lÕI KẾT NỐI", Toast.LENGTH_SHORT).show();
        Log.w("MapError", "MappError");
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void SettingRequest() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);


        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates state = locationSettingsResult.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mGoogleApiClient.connect();
                        break;
                    case Activity.RESULT_CANCELED:
                        SettingRequest();
                        break;
                }
                break;
        }
    }
}
