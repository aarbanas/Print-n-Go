package com.example.arbi.printngo;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ListActivity extends AppCompatActivity {

    final int ACTIVITY_CHOOSE_FILE = 1;
    private static final String TAG = "ListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
/*When the button (. . . ) is pressed user can select file he wants to send for printing  */
        Button openFile = (Button) findViewById(R.id.buttonSendFile);
        openFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                String [] mimeTypes = {"text/csv", "application/pdf","text/plain","application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
                chooseFile.setType("*/*");
                chooseFile.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

                intent = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
            }
        });
    }
    /*The selected file is than written in string (its path, not whole file) */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        EditText editTextFile = (EditText) findViewById(R.id.editTextFilePath);

        switch(requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    String filePath = uri.toString();
                    File myFile = new File(filePath);
                    String displayName = null;
                    //editTextFile.setText(filePath, TextView.BufferType.EDITABLE);

                    if (filePath.startsWith("content://")) {
                    Cursor cursor = null;

                    try {
                        cursor = this.getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }

                    } finally {
                        cursor.close();
                    }
                } else if (filePath.startsWith("file://")) {
                    displayName = myFile.getName();
                }

                    editTextFile.setText(displayName, TextView.BufferType.EDITABLE);

                    Log.i(TAG, uri.getPath());
            }
            }
        }
    }
}