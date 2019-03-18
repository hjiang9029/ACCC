package com.github.hjiang9029.accc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.gms.maps.model.Marker;

public class Settings extends AppCompatActivity {

    private CheckBox parkBox;
    private CheckBox washroomBox;
    private CheckBox fountainBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Intent i = getIntent();
        parkBox = (CheckBox) findViewById(R.id.chkPark);
        washroomBox = (CheckBox) findViewById(R.id.washrooms);
        parkBox.setChecked(i.getBooleanExtra("parks", true));
        washroomBox.setChecked(i.getBooleanExtra("washrooms", true));
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
        finish();
    }
}
