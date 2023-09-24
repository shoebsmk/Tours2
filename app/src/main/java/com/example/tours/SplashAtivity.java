package com.example.tours;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class SplashAtivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 1600;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final int BACKGROUND_LOCATION_REQUEST = 222;

    private static final int LOCATION_REQUEST = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setTitle("Walking Tours");

        // Possibly chck perm's here
        checkPermAndLoadMaps();
        // Possibly load required resources here

        // Handler is used to execute something in the future

    }

    private void checkPermAndLoadMaps() {
        if (checkPermission()) {
                       //setupLocationListener();
            loadMapsActivity();
        } else {
            //showRationaleAlert();
        }
    }

    private void loadMapsActivity() {
        new Handler().postDelayed(() -> {
            // This method will be executed once the timer is over
            // Start your app main activity

            Intent i =
                    new Intent(SplashAtivity.this, MapsActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out); // new act, old act
            // close this activity
            finish();
        }, SPLASH_TIME_OUT);
    }

//    private boolean checkPermission() {
//        if (ContextCompat.checkSelfPermission(this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
//                PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{
//                            Manifest.permission.ACCESS_FINE_LOCATION
//                    }, LOCATION_REQUEST);
//            return false;
//        }
//        return true;
//    }
    private boolean checkPermission() {
        ArrayList<String> perms = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            perms.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                perms.add(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!perms.isEmpty()) {
            String[] array = perms.toArray(new String[0]);
            ActivityCompat.requestPermissions(this,
                    array, LOCATION_REQUEST);
            return false;
        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        } else{
            showRationaleAlertForBackgroundLoc();
        };

        return false;
    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == LOCATION_REQUEST) {
//            for (int i = 0; i < permissions.length; i++) {
//                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                        loadMapsActivity();
//                        return;
//                        //setupLocationListener();
//                    } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        // In an educational UI, explain to the user why your app requires this
//                        // permission for a specific feature to behave as expected, and what
//                        // features are disabled if it's declined. In this UI, include a
//                        // "cancel" or "no thanks" button that lets the user continue
//                        // using your app without granting the permission.
//                        showRationaleAlert();
//                    } else {
//                        showTryAgainAlert();
//                    }
//                }
//            }
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int permNum = permissions.length + 1;
        int permCount = 0;

        if (requestCode == LOCATION_REQUEST) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
                permCount++;

            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getBackgroundLocPerm();
                permCount++;
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // In an educational UI, explain to the user why your app requires this
                        // permission for a specific feature to behave as expected, and what
                        // features are disabled if it's declined. In this UI, include a
                        // "cancel" or "no thanks" button that lets the user continue
                        // using your app without granting the permission.
                        showRationaleAlert();
            } else {
                showRationaleAlert();
                //showTryAgainAlert();
            }

            if (permissions.length == 2) {
                if (permissions[1].equals(Manifest.permission.POST_NOTIFICATIONS) &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    permCount++;
                }
            }

            if (permCount == permNum) {
                loadMapsActivity();

            }

        }
        else if (requestCode == BACKGROUND_LOCATION_REQUEST) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                loadMapsActivity();
            } else {
                showRationaleAlertForBackgroundLoc();
            }
        }
    }


    private void showRationaleAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Fine Accuracy Needed");
        alertDialogBuilder.setMessage(
                "This application needs Fine Accuracy permission in order to determine the closest stops bus to your location. It will not function properly without it. Will you allow it?");
        alertDialogBuilder.setPositiveButton("Yes", (arg0, arg1) -> checkPermAndLoadMaps());
        alertDialogBuilder.setNegativeButton("No Thanks", (dialog, which) -> showTryAgainAlert());
        alertDialogBuilder.setIcon(R.mipmap.ic_launcher_round);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.show();
    }
    private void showRationaleAlertForBackgroundLoc() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Background Location Needed");
        alertDialogBuilder.setMessage(
                "This application needs \"Allow all the time\" Background location permission in order to determine the location in background. It will not function properly without it. Will you allow it?");
        alertDialogBuilder.setPositiveButton("Yes", (arg0, arg1) -> getBackgroundLocPerm());
        alertDialogBuilder.setNegativeButton("No Thanks", (dialog, which) -> showTryAgainAlertForBackgroundLoc());
        alertDialogBuilder.setIcon(R.mipmap.ic_launcher_round);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.show();
    }

    private void showTryAgainAlertForBackgroundLoc() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Background Location Needed");
        alertDialogBuilder.setMessage(
                "This application needs Background Location permission. It will not function properly without it. Please start the application again and allow this permission.");
        alertDialogBuilder.setPositiveButton("Ok", (arg0, arg1) -> finish());
        alertDialogBuilder.setIcon(R.mipmap.ic_launcher_round);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    private void showTryAgainAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Fine Accuracy Needed");
        alertDialogBuilder.setMessage(
                "This application needs Fine Accuracy permission in order to determine the closest stops bus to your location. It will not function properly without it. Please start the application again and allow this permission.");
        alertDialogBuilder.setPositiveButton("Ok", (arg0, arg1) -> finish());
        alertDialogBuilder.setIcon(R.mipmap.ic_launcher_round);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    private void getBackgroundLocPerm() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            return;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "NEED BASIC PERMS FIRST!", Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_REQUEST);
        } else {
            Toast.makeText(this, "ALREADY HAS BACKGROUND LOC PERMS", Toast.LENGTH_LONG).show();
        }
    }


}