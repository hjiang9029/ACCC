package com.github.hjiang9029.accc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static HashMap<String, Park> PARKS = new HashMap<>();
    public static HashMap<String, Washroom> WASHROOMS = new HashMap<>();
    public static HashMap<String, DrinkingFountain> DRINKINGFOUNTAINS = new HashMap<>();
    public static HashMap<String, ParkStructure> PARKSTRUCTURES = new HashMap<>();

    private static boolean redirect = true;
    private double SEARCHED_LAT = 0.0;
    private double SEARCHED_LONG = 0.0;
    private boolean route;
    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    // URL to get contacts JSON
    private static String SERVICE_URL = "http://opendata.newwestcity.ca/downloads/parks/PARKS.json";
    private static String WASHROOM_URL = "http://opendata.newwestcity.ca/downloads/accessible-public-washrooms/WASHROOMS.json";
    private static String DRINKING_FOUNTAINS_URL = "http://opendata.newwestcity.ca/downloads/drinking-fountains/DRINKING_FOUNTAINS.json";
    private static String PARK_STRUCTURES_URL = "http://opendata.newwestcity.ca/downloads/park-structures/PARK_STRUCTURES.json";
    public static ArrayList<String> parkNames = new ArrayList<String>();
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (redirect) {
            Intent i = new Intent(MainActivity.this, SplashScreenActivity.class);
            startActivity(i);
            redirect = false;
        }

        //lv = (ListView) findViewById(R.id.nameList);
        new GetContacts().execute();

        // Initialize Places.
        Places.initialize(getApplicationContext(), getString(R.string.api_key));

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "YOUR_API_KEY");
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));
        autocompleteFragment.setCountry("CA");
        autocompleteFragment.getView().setBackgroundColor(Color.WHITE);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                SEARCHED_LAT = place.getLatLng().latitude;
                SEARCHED_LONG = place.getLatLng().longitude;

            }

            @Override
            public void onError(Status status) {
            }
        });
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            parseParks(sh);
            parseWashrooms(sh);
            parseFountains(sh);
            parseParkStructures(sh);
            return null;
        }

        private void parseParks(HttpHandler sh) {
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(SERVICE_URL);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    //JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONObject parkJsonObj = new JSONObject(jsonStr);
                    JSONArray parkJsonArray = parkJsonObj.getJSONArray("features");
                    // looping through All Contacts
                    for (int i = 0; i < parkJsonArray.length(); i++) {

                        if (i == 12) {
                            i = 13;
                        }
                        JSONObject c = parkJsonArray.getJSONObject(i);
                        JSONObject props = c.getJSONObject("properties");
                        JSONObject geometry = c.getJSONObject("geometry");
                        JSONArray coordArray = geometry.getJSONArray("coordinates");
                        JSONArray coordArray1 = coordArray.getJSONArray(0);
                        JSONArray latlongArray = coordArray1.getJSONArray(0);
                        String name = props.getString("Name");
                        String street = props.getString("StrNum");
                        String avenue = props.getString("StrName");
                        String address = street + " " + avenue;
                        String category = props.getString("Category");
                        String neighbourhood = props.getString("Neighbourhood");
                        //String[] strLatLong = latlongArray.getString(0).split(",");
                        //double latitude = Double.parseDouble(latlongArray.getString(0));
                        //double longitude = Double.parseDouble(latlongArray.getString(1));
                        double latitude = latlongArray.getDouble(1);
                        double longitude = latlongArray.getDouble(0);


                        parkNames.add(name);
                        Park parkObject = new Park(name, address, category, neighbourhood, latitude, longitude);
                        PARKS.put(parkObject.getName(), parkObject);

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());


                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");


            }
        }

        private void parseWashrooms(HttpHandler sh) {
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(WASHROOM_URL);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    //JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONObject washroomJsonObj = new JSONObject(jsonStr);
                    JSONArray washroomJsonArray = washroomJsonObj.getJSONArray("features");
                    // looping through All Contacts
                    for (int i = 0; i < washroomJsonArray.length(); i++) {

                        JSONObject c = washroomJsonArray.getJSONObject(i);
                        JSONObject props = c.getJSONObject("properties");
                        JSONObject geometry = c.getJSONObject("geometry");
                        JSONArray coordArray = geometry.getJSONArray("coordinates");
                        String name = props.getString("Name");
                        String address = props.getString("Address");
                        String category = props.getString("Category");
                        String neighbourhood = props.getString("Neighbourhood");
                        String hours = props.getString("Hours");
                        double latitude = coordArray.getDouble(1);
                        double longitude = coordArray.getDouble(0);

                        Washroom newWashroom = new Washroom(name, address, category, neighbourhood, hours, latitude, longitude);
                        WASHROOMS.put(newWashroom.getName(), newWashroom);

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }
        }


        private void parseFountains(HttpHandler sh) {
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(DRINKING_FOUNTAINS_URL);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    //JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONObject fountainJsonObj = new JSONObject(jsonStr);
                    JSONArray fountainJsonArray = fountainJsonObj.getJSONArray("features");
                    // looping through All Contacts
                    for (int i = 0; i < fountainJsonArray.length(); i++) {

                        JSONObject c = fountainJsonArray.getJSONObject(i);
                        JSONObject props = c.getJSONObject("properties");
                        JSONObject geometry = c.getJSONObject("geometry");
                        JSONArray coordArray = geometry.getJSONArray("coordinates");
                        String parkName = props.getString("ParkName");
                        double latitude = coordArray.getDouble(1);
                        double longitude = coordArray.getDouble(0);

                        DrinkingFountain newWashroom = new DrinkingFountain(parkName, latitude, longitude);
                        DRINKINGFOUNTAINS.put(newWashroom.getParkName(), newWashroom);

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }
        }


        private void parseParkStructures(HttpHandler sh) {
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(PARK_STRUCTURES_URL);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    //JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONObject parkStructureJsonObj = new JSONObject(jsonStr);
                    JSONArray parkStructureJsonArray = parkStructureJsonObj.getJSONArray("features");
                    // looping through All Contacts
                    for (int i = 0; i < parkStructureJsonArray.length(); i++) {

                        JSONObject c = parkStructureJsonArray.getJSONObject(i);
                        JSONObject props = c.getJSONObject("properties");
                        JSONObject geometry = c.getJSONObject("geometry");
                        JSONArray coordArray = geometry.getJSONArray("coordinates");
                        String type = props.getString("Type");
                        String parkName = props.getString("ParkName");
                        double latitude = coordArray.getDouble(1);
                        double longitude = coordArray.getDouble(0);

                        ParkStructure newParkStructure = new ParkStructure(type, parkName, latitude, longitude);
                        PARKSTRUCTURES.put(newParkStructure.getParkName(), newParkStructure);

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

        }
    }


    public void createMap(View view) {
        if (isServicesOK()) {
            Intent i = new Intent(this, MapsActivity.class);
            i.putExtra("SEARCHED_LAT", SEARCHED_LAT);
            i.putExtra("SEARCHED_LONG", SEARCHED_LONG);
            i.putExtra("Route", route = true);
            startActivity(i);
        }
    }


    public void createMapExplore(View view) {
        if (isServicesOK()) {
            Intent i = new Intent(this, MapsActivity.class);
            i.putExtra("SEARCHED_LAT", SEARCHED_LAT);
            i.putExtra("SEARCHED_LONG", SEARCHED_LONG);
            i.putExtra("Route", route = false);
            startActivity(i);
        }
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make app requests
            Log.d(TAG, "isServiceOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred be we can resolve it
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_LONG).show();
        }
        return false;
    } //Checking if the user has the correct Play Services version

}
