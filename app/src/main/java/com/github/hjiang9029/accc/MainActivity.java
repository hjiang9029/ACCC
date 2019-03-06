package com.github.hjiang9029.accc;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.*;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static HashMap<String, Park> parks = new HashMap<>();
    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    // URL to get contacts JSON
    private static String SERVICE_URL = "http://opendata.newwestcity.ca/downloads/parks/PARKS.json";
    ArrayList<String> parkNames = new ArrayList<String>();
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.nameList);
        new GetContacts().execute();
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
                        double latitude = latlongArray.getDouble(0);
                        double longitude = latlongArray.getDouble(1);


                        parkNames.add(name);
                        Park parkObject = new Park(name, address, category, neighbourhood, latitude, longitude);
                        parks.put(parkObject.getName(), parkObject);

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

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            //Toon[] toonArray = toonList.toArray(new Toon[toonList.size()]);

            ArrayAdapter<String> strAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, parkNames);

            // Attach the adapter to a ListView
            lv.setAdapter(strAdapter);
        }
    }


    public void createMap(View view) {
        if (isServicesOK()) {
            Intent i = new Intent(this, MapsActivity.class);
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
