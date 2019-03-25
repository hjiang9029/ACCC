package com.github.hjiang9029.accc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class AmenityDetailsActivity extends AppCompatActivity {

    private Park currentPark;
    private TextView name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amenity_details);
        Intent i = getIntent();
        String parkName = i.getStringExtra("parkName").split("\n")[0];

        currentPark = MainActivity.PARKS.get(parkName);
        name = (TextView) findViewById(R.id.detail_name);
        name.setText("Name: " + currentPark.getName() + "\n"
                + "Address: " + currentPark.getAddress() + "\n"
                + "Category: " + currentPark.getCategory() + "\n"
                + "Neighbourhood: " + currentPark.getNeighbourhood());
    }


}
