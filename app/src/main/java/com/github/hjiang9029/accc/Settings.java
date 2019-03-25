package com.github.hjiang9029.accc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;

public class Settings extends AppCompatActivity {

    private CheckBox parkBox;
    private CheckBox washroomBox;
    private CheckBox fountainBox;
    private CheckBox parkStructureBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Intent i = getIntent();
        parkBox = (CheckBox) findViewById(R.id.chkPark);
        washroomBox = (CheckBox) findViewById(R.id.washrooms);
        fountainBox = (CheckBox) findViewById(R.id.chkWindows);
        parkStructureBox = (CheckBox) findViewById(R.id.chkParkStructure);
        parkBox.setChecked(i.getBooleanExtra("parks", true));
        washroomBox.setChecked(i.getBooleanExtra("washrooms", true));
        fountainBox.setChecked(i.getBooleanExtra("fountains", true));
        parkStructureBox.setChecked(i.getBooleanExtra("parkstruct", true));
    }

    public void saveButton(View view) {
        MapsActivity.parkSetting = parkBox.isChecked();
        for (Marker m : MapsActivity.parkMarkers) {
            m.setVisible(parkBox.isChecked());
        }
        for (Marker m : MapsActivity.washroomsMarkers) {
            m.setVisible(washroomBox.isChecked());
        }
        MapsActivity.parkSetting = parkBox.isChecked();
        MapsActivity.washroomSetting = washroomBox.isChecked();
        MapsActivity.waterFountainSetting = fountainBox.isChecked();
        MapsActivity.parkStructuresSetting = parkStructureBox.isChecked();

        Toast.makeText(this.getBaseContext(),"Saved",
                Toast.LENGTH_LONG).show();

        finish();
    }
}
