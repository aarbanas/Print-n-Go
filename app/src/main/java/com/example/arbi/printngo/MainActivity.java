package com.example.arbi.printngo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.arbi.printngo.MESSAGE";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user taps the Map button open Google maps to search for print shops in area*/
    public void openMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps the "Isprintaj" button. It lists all the copy shops, lets the user to
     * select file and other features. It shows all information about printing */
    public void openList(View view) {
        Intent intent = new Intent(this, PrintActivity.class);
        startActivity(intent);
    }
}
