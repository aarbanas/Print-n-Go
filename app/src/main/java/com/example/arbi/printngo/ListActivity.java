package com.example.arbi.printngo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ListActivity extends AppCompatActivity {

    private Intent loadIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Button openFile = (Button)findViewById(R.id.buttonSendFile);
        openFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadIntent = new Intent(Intent.ACTION_GET_CONTENT);
                loadIntent.setType("*/*");
                loadIntent.addCategory(Intent.CATEGORY_OPENABLE);

                try {

                    startActivityForResult(Intent.createChooser(loadIntent, "Select your phrases .txt document"), 0);

                } catch (android.content.ActivityNotFoundException e) {

                    //if the user doesen't have file explorer installed
                    Toast.makeText(ListActivity.this, "Please install a file manager!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
