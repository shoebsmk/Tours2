package com.example.tours;

import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;

public class PathDownloader {

    private static final String url = "https://www.christopherhield.com/data/WalkingTourContent.json";

    private static final String TAG = "PathDownloader";

    private static ArrayList<LatLng> latLngArrayList = new ArrayList<>();

    public static void downloadPath(MapsActivity mapsActivity){
        RequestQueue queue = Volley.newRequestQueue(mapsActivity);

        Uri.Builder buildURL = Uri.parse(url).buildUpon();
        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONObject> listener =
                response -> handleResults(mapsActivity,response, urlToUse);
        Response.ErrorListener error = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(error.networkResponse.data));
                    //responseTV.setText(MessageFormat.format("Error: {0}", jsonObject.toString()));
                    Log.e(TAG, "onErrorResponse: " + MessageFormat.format("Error: {0}", jsonObject.toString()) );

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private static void handleResults(MapsActivity mapsActivity, JSONObject response, String urlToUse) {
        try {


            JSONObject responseJSONObject = response;
            if(response.has("path")){
                //Log.e(TAG, "onResponse: " + responseJSONObject.getJSONArray("fences") );
                JSONArray paths = responseJSONObject.getJSONArray("path");
                Log.d(TAG, "handleResults: Paths: " +  paths);
                for (int i = 0; i< paths.length();i++ ){


                    //String path = paths.getString(i);
                    String path =  paths.getString(i);
                    if (path.trim().isEmpty()) {
                        Log.e(TAG, "handleResults: " + "Enter Lat & Lon coordinates first!");
                        return;
                    }
                    String[] latLon = path.split(",");
                    double lat = Double.parseDouble(latLon[0]);
                    double lon = Double.parseDouble(latLon[1]);
//                    Log.d(TAG, "handleResults: path: " + lat + "----" + lon);
                    LatLng latLng = new LatLng(lon,lat);
                    latLngArrayList.add(latLng);

                }
                mapsActivity.updateLatLngDownload(latLngArrayList);
//                for (int i = 0; i< paths.length();i++ ) {
//                    Log.d(TAG, "handleResults: path: " + latLngArrayList.get(i));
//                }
            }


        } catch (Exception e) {
            //responseTV.setText(MessageFormat.format("Response: {0}", e.getMessage()));
            Log.e(TAG, "onResponse: " + MessageFormat.format("Response: {0}", e.getMessage()) );
        }
    }

}
