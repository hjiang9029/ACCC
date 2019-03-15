package com.github.hjiang9029.accc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class Settings extends AppCompatActivity {

    private CheckBox parkBox;
    private CheckBox washroomBox;
    private CheckBox fountainBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    private void saveButton(View view) {
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("parks", parkBox.isChecked());
        i.putExtra("washrooms", washroomBox.isChecked());
        i.putExtra("fountains", fountainBox.isChecked());
        startActivity(i);
        // Not sure if using an intent here is the right way to do this, as if we press "back" it'd redirect back to settings
        // But i'll leave the intent function here just in case.
        // TODO: Maybe set static variables in MapsActivity? Not sure.
        // MapsActivity.parkSetting = parkBox.isChecked();
        // And so on..
    }
}
