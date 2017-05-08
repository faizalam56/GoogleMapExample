package com.example.zmq162.googlemapexample;

import android.app.Activity;
import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    GoogleMap mMap;
    GPSTracker gpsTracker;
    double longitude,latitude;
    EditText editText;
    Button btn;
    String location;
    Marker marker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().hide();

        if(googleSeviceAvailable()){
            Toast.makeText(this,"fine googleSeviceAvailable",Toast.LENGTH_SHORT).show();
            initMap();
        }

        editText = (EditText) findViewById(R.id.edittext);
        btn = (Button) findViewById(R.id.btn);

        btn.setOnClickListener(this);

    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public boolean googleSeviceAvailable(){
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int isAvailable = googleApi.isGooglePlayServicesAvailable(this);
        if(isAvailable== ConnectionResult.SUCCESS){
            return true;
        } else if(googleApi.isUserResolvableError(isAvailable)){
            Dialog dialog = googleApi.getErrorDialog(this,isAvailable,0);
            dialog.show();
        }else {
            Toast.makeText(this,"cannot find google play service",Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        findGpsLocation();
        gotoGeocodeZoom(latitude, longitude, 15);

        //through this we find our current location on click the location logo. First comment gotoGeocodeZoom(latitude,longitude,15) method;
//        mMap.setMyLocationEnabled(true);
        if(mMap!=null) {
            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    LatLng ll = marker.getPosition();
                    double lat = ll.latitude;
                    double lng = ll.longitude;
                    List<Address> list = null;
                    try {
                        list = geocoder.getFromLocation(lat,lng,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = list.get(0);
                    marker.setTitle(address.getLocality());
                }
            });
        }
    }

    private void gotoGeocodeZoom(double lat,double log,float zoom){
        LatLng ll = new LatLng(lat, log);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,zoom);
        mMap.moveCamera(update);
    }
    private void findGpsLocation(){
        gpsTracker = new GPSTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            longitude = gpsTracker.getLongitude();
            latitude = gpsTracker.getLatitude();
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        }else{
            gpsTracker.showSettingsAlert();
        }
    }

    private void geoLocate(String location) throws IOException {
        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(location, 1);

        Address address = list.get(0);
        String locality = address.getLocality();

        Toast.makeText(this,locality,Toast.LENGTH_SHORT).show();
        double lat = address.getLatitude();
        double log = address.getLongitude();

        gotoGeocodeZoom(lat, log, 15);

        setMarker(locality, lat, log);
    }

    private void setMarker(String locality, double lat, double log) {
        if(marker!=null){
            marker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                                    .title(locality)
                                    .draggable(true)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                    .position(new LatLng(lat, log))
                                    .snippet("I am here");
        marker = mMap.addMarker(options);
    }

    @Override
    public void onClick(View v) {
        String location = editText.getText().toString();
        hideKeypad();
        try {
            geoLocate(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void hideKeypad(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if(view==null){
            return;
        }else{
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
