package com.example.tours;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.tours.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private boolean travelPathVisibility = true;
    private LatLng prevLatLng;
    private static final String TAG = "MapsActivity";

    private boolean fullScreen = true;
    private Marker carMarker;

    public static HashMap<String, Building> buildingMap = new HashMap<>();

    private final ArrayList<LatLng> latLngArrayList = new ArrayList<>();
    private final ArrayList<Circle> circleArrayList = new ArrayList<>();


    private Polyline llHistoryPolyline;

    private Polyline llPathPolyline;
    private final ArrayList<LatLng> latLonHistory = new ArrayList<>();
    private static final int LOCATION_REQUEST = 111;
    public static int screenHeight;
    public static int screenWidth;

    private final float zoomDefault = 16.0f;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private FenceMgr fenceMgr;

    private final List<PatternItem> pattern = Collections.singletonList(new Dot());

    boolean madeFences = false;
    boolean mapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        buildingMap.clear();
        FenceDownloader.downloadFences(this);

        hideSystemUI();
        getScreenDimensions();
        initMap();

    }

    public void updateBuildingMap(HashMap<String, Building> map){

        buildingMap.putAll(map);
        //Log.e(TAG, "updateBuildingMap: " + id + " " + buildingMap.get(id).getLatLng() );

        if(mapReady){
            makeFences();
        } else {
            Log.d(TAG, "updateBuildingMap: " + "Map was not ready before makefense call, will try againg when ready");
        }

    }

    public void initMap() {

        fenceMgr = new FenceMgr(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setBuildingsEnabled(true);

        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomDefault));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);

        if(checkLocationPermission()) {
            //makeFences();
            setupLocationListener();
        }

        // Added logic to avoid an error
        if(!madeFences){
            makeFences();
        }
        mapReady = true;
        makePath();
    }
    private void setupLocationListener() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocListener(this);

        //minTime	    long: minimum time interval between location updates, in milliseconds
        //minDistance	float: minimum distance between location updates, in meters
        if (checkLocationPermission() && locationManager != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission() && locationManager != null && locationListener != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);
    }

    public String doLatLon(LatLng loc) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses;

            //String loc = ((EditText) findViewById(R.id.editText)).getText().toString();
            if (loc == null) {
                Log.e(TAG, "Enter Lat & Lon coordinates first!");
                return null;
            }
            double lat = loc.latitude;
            double lon = loc.longitude;

            addresses = geocoder.getFromLocation(lat, lon, 10);

            //displayAddresses(addresses);

            //Log.e(TAG, "doLatLon: " + addresses.get(0).getAddressLine(0));
            return addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return "";
    }


    public void updateLocation(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLonHistory.add(latLng); // Add the LL to our location history

        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        binding.addressTV.setText(doLatLon(latLng));
        Log.d(TAG, "updateLocation: " + doLatLon(latLng));


        if (latLonHistory.size() == 1) { // First update
            mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomDefault));
            prevLatLng = latLng;
            return;
        }




        if (latLonHistory.size() > 1) { // Second (or more) update
            if(travelPathVisibility){
                PolylineOptions polylineOptions = new PolylineOptions();

                for (LatLng ll : latLonHistory) {
                    polylineOptions.add(ll);
                }
                llHistoryPolyline = mMap.addPolyline(polylineOptions);
                llHistoryPolyline.setEndCap(new RoundCap());
                llHistoryPolyline.setWidth(12);
                llHistoryPolyline.setColor(Color.parseColor("#02703D"));
            }

            float r = getRadius();
            if (r > 0) {
                double lonDiff = latLng.longitude - prevLatLng.longitude;
                double latDiff = latLng.latitude - prevLatLng.latitude;
                double angle = Math.atan2(lonDiff,latDiff);
                angle = Math.toDegrees(angle) + 90;
                Bitmap icon;
                if(angle > 45 && angle < 135){
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_up);
                } else if (angle > 135 && angle < 225){
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_right);
                } else  if (angle > 225 && angle < 315){
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_down);
                } else {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);

                }
                prevLatLng = latLng;
                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);
                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

//                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_right);
//                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);
//                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);

                options.icon(iconBitmap);
                //options.rotation(location.getBearing() + 90 );
                options.anchor(0.6f,1.0f);

                if (carMarker != null) {
                    carMarker.remove();
                }

                carMarker = mMap.addMarker(options);
            }
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        sumIt();
    }
    private void sumIt() {
        double sum = 0;
        LatLng last = latLonHistory.get(0);
        for (int i = 1; i < latLonHistory.size(); i++) {
            LatLng current = latLonHistory.get(i);
            sum += SphericalUtil.computeDistanceBetween(current, last);
            last = current;
        }
        Log.d(TAG, "sumIt: " + String.format("%.3f km", sum/1000.0));

    }

    private void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, LOCATION_REQUEST);
            return false;
        }
        return true;
    }


    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        float factor = (float) ((35.0 / 2.0 * z) - (355.0 / 2.0));
        float multiplier = ((7.0f / 7200.0f) * screenWidth) - (1.0f / 20.0f);
        float radius = factor * multiplier;
        return radius;
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (fullScreen) {
                hideSystemUI();
            } else {
                showSystemUI();
            }
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void makeFences() {

        Set<String> mapSet = buildingMap.keySet();
        for (String s : mapSet) {
            try {
//                Log.e(TAG, "MakeFences: " +  s + " -- "
//                        + buildingMap.get(s).getLatLng());
//                LatLng latLng = buildingMap.get(s).getLatLng();
//                double radius = buildingMap.get(s).getRadius();
                addFence(buildingMap.get(s));
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        Log.d(TAG, "makeFences: " + buildingMap.size() );
        // Hard-coded test fences
//        LatLng ll = new LatLng(41.8754, -87.6242);
//        addFence(ll, 100.0);
//
//        LatLng ll2 = new LatLng(41.8794, -87.6242);
//        addFence(ll2, 80.0);
        madeFences = true;

    }

    private void addFence(Building building) {
//        String id = UUID.randomUUID().toString();
//        Building fd = new Building(id, latLng, radius, null, null, null, null);
//        fenceMgr.addFence(fd);

        fenceMgr.addFence(building);
//        Log.e(TAG, "addFence: " + building.getLatLng() );
//        Log.e(TAG, "addFence: " + Color.parseColor(building.getFenceColor()) );

        // Just to see the fence
        int line = Color.parseColor(building.getFenceColor());
        int fill = ColorUtils.setAlphaComponent(line, 50);

        Circle c =  mMap.addCircle(new CircleOptions()
                .center(building.getLatLng())
                .radius(building.getRadius())
                .strokePattern(pattern)
                .strokeColor(line)
                .fillColor(fill));

        circleArrayList.add(c);

    }

    public void updateLatLngDownload(ArrayList<LatLng> _latLngArrayList) {
        latLngArrayList.addAll(_latLngArrayList);
//        for (int i = 0; i< latLngArrayList.size(); i++ ) {
//            Log.d(TAG, "MapsActivity CallBack: path: " + latLngArrayList.get(i));
//        }

        if(mapReady){
            makePath();
        } else {
            Log.d(TAG, "updateLatLngDownload: " + "Map was not ready before path call, will try againg when ready");
        }



    }

    private void makePath() {
        if (latLngArrayList.size() == 0 || !mapReady){
            return;
        }
        PolylineOptions options = new PolylineOptions();

        Log.d(TAG, "makePath: LLAL size" + latLngArrayList.size() );
        for (LatLng ll : latLngArrayList) {
            options.add(ll);
        }
        llPathPolyline = mMap.addPolyline(options);
        llPathPolyline.setEndCap(new RoundCap());
        llPathPolyline.setWidth(12);
        llPathPolyline.setColor(Color.rgb(255,187,0));
        Log.d(TAG, "makePath: " + " --- Done --" );

    }
    public void showAddress(View v){

        if (binding.addressCheckBox.isChecked()) {
            binding.addressTV.setVisibility(View.VISIBLE);
        } else if(!binding.addressCheckBox.isChecked()){
            binding.addressTV.setVisibility(View.GONE);
        }
    }
    public void showGeofence(View v){

        if (binding.geofencesCheckBox.isChecked()) {
            for(Circle c : circleArrayList){c.setVisible(true);}
        } else if(!binding.geofencesCheckBox.isChecked()){
            for(Circle c : circleArrayList){c.setVisible(false);}
        }
    }

    public void showTourPath(View v){
        if (binding.tourPathCheckBox.isChecked()) {
            llPathPolyline.setVisible(true);
        } else if(!binding.tourPathCheckBox.isChecked()){
            llPathPolyline.setVisible(false);
        }
    }
    public void showTravelPath(View v){
        if (llHistoryPolyline == null){
            return;
        }
        if (binding.travelPathCheckBox.isChecked()) {
            llHistoryPolyline.setVisible(true);
            travelPathVisibility = true;
        } else if(!binding.travelPathCheckBox.isChecked()){
            llHistoryPolyline.setVisible(false);
            travelPathVisibility = false;
        }
    }

    //Intruction suggest onStop but onStop erases it when we're back from Building info
    @Override
    protected void onStop() {
        //GeofenceBroadcastReceiver.doClearAll();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        GeofenceBroadcastReceiver.doClearAll();
        super.onDestroy();
    }
}