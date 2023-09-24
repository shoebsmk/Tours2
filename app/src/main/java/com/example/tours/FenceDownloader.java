package com.example.tours;
import android.net.Uri;
import android.util.Log;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public class FenceDownloader {
    private static final String url = "https://www.christopherhield.com/data/WalkingTourContent.json";

    private static final String TAG = "FenceDownloader";

    private static HashMap<String, Building> buildingMap = new HashMap<>();;

    public static void downloadFences(MapsActivity mapsActivity){
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
            if(response.has("fences")){
                //Log.e(TAG, "onResponse: " + responseJSONObject.getJSONArray("fences") );
                JSONArray fences = responseJSONObject.getJSONArray("fences");
                for (int i = 0; i< fences.length();i++ ){
                    JSONObject fence = fences.getJSONObject(i);
                    String id = fence.getString("id");
                    String address = fence.getString("address");
                    String latitude = fence.getString("latitude");
                    String longitude = fence.getString("longitude");
                    String radius = fence.getString("radius");
                    String description = fence.getString("description");
                    String fenceColor = fence.getString("fenceColor");
                    String image = fence.getString("image");
//                    Log.d(TAG, "handleResults: id: " + id);
//                    Log.d(TAG, "handleResults: address: " + address);
//                    Log.d(TAG, "handleResults: latitude: " + latitude);
//                    Log.d(TAG, "handleResults: longitude: " + longitude);
//                    Log.d(TAG, "handleResults: radius: " + radius);
//                    Log.d(TAG, "handleResults: description: " + description);
//                    Log.d(TAG, "handleResults: fenceColor: " + fenceColor);
//                    Log.d(TAG, "handleResults: image URL: " + image);
//                    Log.d(TAG, "handleResults: " + "-----");


                    LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble( longitude));
                    Building building = new Building(id, latLng, Double.parseDouble(radius), address, description, fenceColor, image);
                    buildingMap.put(id, building);


                    //mapsActivity.updateBuildingMap(id, building);
//                    Log.d(TAG, "handleResults: data: " + buildingMap.get(id));
//                    Log.d(TAG, "handleResults: address: " + building.getAddress());
//                    Log.d(TAG, "handleResults: latitude, Longitute: " + building.getLatLng());
//                    Log.d(TAG, "handleResults: radius: " + building.getRadius());
//                    Log.d(TAG, "handleResults: description: " + building.getDescription());
//                    Log.d(TAG, "handleResults: fenceColor: " + building.getFenceColor());
//                    Log.d(TAG, "handleResults: imageURL: " + building.getImageURL());
                }
//                Set<String> mapSet = buildingMap.keySet();
//                for (String s : mapSet) {
//                    Log.e(TAG, "handleResults: " +  s + " -- "
//                            + buildingMap.get(s).getLatLng());
//                }
                mapsActivity.updateBuildingMap(buildingMap);


            }


        } catch (Exception e) {
            //responseTV.setText(MessageFormat.format("Response: {0}", e.getMessage()));
            Log.e(TAG, "onResponse: " + MessageFormat.format("Response: {0}", e.getMessage()) );
        }
    }

}
