package com.github.hjiang9029.accc;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG = "MapsActivity";

    // Switches for whether the respective locations are marked
    // add as needed
    public static boolean parkSetting = true;
    public static boolean washroomSetting = true;
    public static boolean parkStructuresSetting = true;
    public static boolean waterFountainSetting = true;

    public static ArrayList<Marker> parkMarkers = new ArrayList<>();
    public static ArrayList<Marker> washroomsMarkers = new ArrayList<>();
    public static ArrayList<String> filteredParks = new ArrayList<>();
    public static ArrayList<Marker> parkStructureMarkers = new ArrayList<>();
    public static ArrayList<Marker> waterFountainMarkers = new ArrayList<>();

    //#region Permissions
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    //#endregion
    //#region Data variables
    private GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 15f;
    private static double SEARCHED_LAT;
    private static double SEARCHED_LONG;
    //#endregion

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Side bar initialization
        DrawerLayout drawer = findViewById(R.id.drawer_layout1);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, myToolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        // Assign the extra variables
        SEARCHED_LAT = getIntent().getDoubleExtra("SEARCHED_LAT", 0.0);
        SEARCHED_LONG = getIntent().getDoubleExtra("SEARCHED_LONG", 0.0);

        getLocationPermission();
        initializeAutoCompleteSearch();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent h = new Intent(getBaseContext(), Settings.class);
            h.putExtra("parks", parkSetting);
            h.putExtra("washrooms", washroomSetting);
            h.putExtra("fountains", waterFountainSetting);
            h.putExtra("parkstruct", parkStructuresSetting);
            startActivity(h);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this,
                    permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION);
            }
        }
    }

    private String createRoute(LatLng src, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + src.latitude + "," + src.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=bicycling";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getResources().getString(R.string.api_key);;

        return url;
    }



    private void getDeviceLocation() {
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = null;
                            //Location currentLocation = (Location) task.getResult();
                            LatLng markerLatlng;
                            if (currentLocation != null) {
                                markerLatlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(markerLatlng).title("Marker"));
                                addCloseMarkers(markerLatlng);
                                //mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLatlng));
                                CameraPosition cam = new CameraPosition.Builder()
                                        .target(markerLatlng)
                                        .zoom(DEFAULT_ZOOM)
                                        .tilt(45)
                                        .bearing(0)
                                        .build();
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cam));
                            } else {
                                markerLatlng = new LatLng(49.201354, -122.912716);
                                mMap.addMarker(new MarkerOptions().position(markerLatlng).title("Marker"));
                                addCloseMarkers(markerLatlng);
                                //mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLatlng));
                                CameraPosition cam = new CameraPosition.Builder()
                                        .target(markerLatlng)
                                        .zoom(DEFAULT_ZOOM)
                                        .tilt(45)
                                        .bearing(0)
                                        .build();
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cam));
                            }
                            // Code to create a hardcoded route, only uncomment to test when necessary $$$

                            if (SEARCHED_LAT != 0.0 && SEARCHED_LONG != 0.0) {
                                LatLng origin = markerLatlng;
                                LatLng dest = new LatLng(SEARCHED_LAT, SEARCHED_LONG);

                                double distance = haversine(origin.latitude, dest.latitude, origin.longitude, dest.longitude);
                                MarkerOptions endMarker = new MarkerOptions();
                                endMarker.position(dest);
                                endMarker.title("" + (int) distance + "m away");
                                mMap.addMarker(endMarker);

                                // Getting URL to the Google Directions API
                                String url = createRoute(origin, dest);

                                DownloadTask downloadTask = new DownloadTask();

                                // Start downloading json data from Google Directions API
                                downloadTask.execute(url);
                            }

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Security exception: " + e.getMessage());
        }
    }


    private void addCloseMarkers(LatLng origin) {

        // Adding to the sidebar
         ListView lv = findViewById(R.id.list_drawer);
         double distance = 0;

        for (Park p : MainActivity.PARKS.values()) {

            distance = haversine(origin.latitude, p.latitude, origin.longitude, p.longitude);
            DecimalFormat dec = new DecimalFormat("0");

            if (distance < (double) 1000) {
                filteredParks.add(p.getName() + "\n" + dec.format(distance) + " m");
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                MapsActivity.filteredParks);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedParkName = filteredParks.get(position);
                Intent i = new Intent(getApplicationContext(), AmenityDetailsActivity.class);
                i.putExtra("parkName", selectedParkName);
                startActivity(i);
            }
        });

        if (parkSetting) {
            for (Park p : MainActivity.PARKS.values()) {
                distance = haversine(origin.latitude, p.latitude, origin.longitude, p.longitude);
                if (distance < (double) 1000) {
                    LatLng parkLatLng = new LatLng(p.getLatitude(), p.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(parkLatLng);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.tree));
                    parkMarkers.add(mMap.addMarker(markerOptions.title("" + (int)distance + "m away")));
                }
            }
        }
        if (washroomSetting) {
            for (Washroom w : MainActivity.WASHROOMS.values()) {
                distance = haversine(origin.latitude, w.latitude, origin.longitude, w.longitude);
                if (distance < (double) 1000) {
                    LatLng washroomLatLng = new LatLng(w.getLatitude(), w.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(washroomLatLng);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.restroom));
                    markerOptions.title("" + (int)distance + "m away");
                    washroomsMarkers.add(mMap.addMarker(markerOptions));
                }
            }
        }
        if (parkStructuresSetting) {
            for (ParkStructure ps : MainActivity.PARKSTRUCTURES.values()) {
                distance = haversine(origin.latitude, ps.latitude, origin.longitude, ps.longitude);
                if (distance < (double) 1000) {
                    LatLng washroomLatLng = new LatLng(ps.getLatitude(), ps.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(washroomLatLng);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.playground));
                    parkStructureMarkers.add(mMap.addMarker(markerOptions.title("" + (int)distance + "m away")));
                }
            }
        }
        if (waterFountainSetting) {
            for (DrinkingFountain df : MainActivity.DRINKINGFOUNTAINS.values()) {
                distance = haversine(origin.latitude, df.latitude, origin.longitude, df.longitude);
                if (haversine(origin.latitude, df.latitude, origin.longitude, df.longitude) < (double) 1000) {
                    LatLng washroomLatLng = new LatLng(df.getLatitude(), df.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(washroomLatLng);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.water));
                    waterFountainMarkers.add(mMap.addMarker(markerOptions.title("" + (int) distance + "m away")));
                }
            }
        }
    }

    private void initializeAutoCompleteSearch() {

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.api_key));
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        }
        if (autocompleteFragment != null) {
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.i(TAG, "An error occurred: " + status);
                }
            });
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(MapsActivity.this);
        }
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }

    // Creates a string from the result from the passed in string URL
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            Objects.requireNonNull(iStream).close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Private inner class that parses the JSON data from the route URL
    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String,String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String,String>>> result) {
            ArrayList points;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String,String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap point = path.get(j);

                    double lat = Double.parseDouble((String) point.get("lat"));
                    double lng = Double.parseDouble((String) point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

    /**
     * Calculate distance between two points in latitude and longitude
     * Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point
     * @returns Distance in Meters
     */
    public static double haversine(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }
}
