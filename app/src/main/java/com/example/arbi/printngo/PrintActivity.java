package com.example.arbi.printngo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.shockwave.pdfium.PdfiumCore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class PrintActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {


    private GoogleApiClient mGoogleApiClient;
    final int ACTIVITY_CHOOSE_FILE = 1;
    private static final String TAG = "PrintActivity";
    public final static String FOLDER = Environment.getExternalStorageDirectory() + "/PDF";

    private String[] labels_postavke = {"label_postavke","label_interval_ispisa","label_postavke_uveza"};
    private String[] layout_postavke = {"layout_postavke","layout_interval_ispisa","layout_postavke_uveza"};
    public List<String> spinner_array = new ArrayList<String>();
    private int cursor_postavke=0;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        final TextView aboutPrinting = (TextView) findViewById(R.id.textViewShowData);
        final CheckBox obostrano = (CheckBox) findViewById(R.id.checkBoxBothSides);
        final CheckBox uBoji = (CheckBox) findViewById(R.id.checkBoxInColor);
        final ImageView imageThumbnail = (ImageView) findViewById(R.id.imageViewShowPDF);
        final EditText brojKopija = (EditText) findViewById(R.id.editTextPaperCount);
        final RadioGroup uvez = (RadioGroup) findViewById(R.id.radioGroupUvez);
        final RadioButton bezUveza = (RadioButton) findViewById(R.id.radioButtonBezUveza);
        final RadioButton mekiUvez = (RadioButton) findViewById(R.id.radioButtonMekiUvez);
        final RadioButton tvrdiUvez = (RadioButton) findViewById(R.id.radioButtonTvrdiUvez);

        if(cursor_postavke==0){
            Button previous_button = (Button) findViewById(R.id.previous_postavke);
            previous_button.setVisibility(View.GONE);
        }

        //Set click listeners on buttons "Dalje" and "Natrag"
        findViewById(R.id.previous_postavke).setOnClickListener(this);
        findViewById(R.id.next_postavke).setOnClickListener(this);

        //Execute async task for fetching list of copy shops
        new JsonTask().execute("http://207.154.235.97/login/lista_kopirnica.php");


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        //getInfo();

        imageThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage();
            }
        });

        uBoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String trenutniText = (String) aboutPrinting.getText();
                if (uBoji.isChecked()) {
                    aboutPrinting.setText(trenutniText + "\n\tU boji");
                }
                else {
                    trenutniText = trenutniText.replaceAll("\\n\\tU boji", "");
                    aboutPrinting.setText(trenutniText);
                }
            }
        });

        obostrano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String trenutniText = (String) aboutPrinting.getText();
                if (obostrano.isChecked()) {
                    aboutPrinting.setText(trenutniText + "\n\tObostrano");
                }
                else {
                    trenutniText = trenutniText.replaceAll("\\n\\tObostrano", "");
                    aboutPrinting.setText(trenutniText);
                }
            }
        });

        brojKopija.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE){
                    String trenutniText = (String) aboutPrinting.getText();
                    aboutPrinting.setText(trenutniText + "\n\tBroj kopija: " + brojKopija.getText().toString());
                    InputMethodManager imm = (InputMethodManager) textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        uvez.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                String trenutniText = (String) aboutPrinting.getText();
                if (bezUveza.isChecked()){
                    trenutniText = trenutniText.replaceAll("\\n\\tMeki uveza", "");
                    trenutniText = trenutniText.replaceAll("\\n\\tTvrdi uvez", "");
                    aboutPrinting.setText(trenutniText + "\n\tBez uveza");
                }
                else if (mekiUvez.isChecked()){
                    trenutniText = trenutniText.replaceAll("\\n\\tBez uveza", "");
                    trenutniText = trenutniText.replaceAll("\\n\\tTvrdi uvez", "");
                    aboutPrinting.setText(trenutniText + "\n\tMeki uvez");
                }
                else {
                    trenutniText = trenutniText.replaceAll("\\n\\tBez uveza", "");
                    trenutniText = trenutniText.replaceAll("\\n\\tMeki uvez", "");
                    aboutPrinting.setText(trenutniText + "\n\tTvrdi uvez");
                }
            }
        });


/*When the button (. . . ) is pressed user can select file he wants to send for printing. It is allowed only to pick
 * .pdf, .csv, .txt, .xml, .docx files */
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
    /*The selected file's name is written in string. First page of selected PDF file is shown on image view, and pages of PDF file are counted */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        EditText editTextFile = (EditText) findViewById(R.id.editTextFilePath);
        TextView textViewShowData = (TextView) findViewById(R.id.textViewShowData);
        switch(requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    String filePath = uri.toString();
                    File myFile = new File(filePath);
                    String displayName = null;
                    //editTextFile.setText(filePath, TextView.BufferType.EDITABLE);
                    int count = 0;
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
                    //Generating image and counting page numbers of PDF file
                    generateImageFromPdf(uri);
                    editTextFile.setText(displayName, TextView.BufferType.EDITABLE);
                    Log.i(TAG, uri.getPath());
                }
            }
        }

    }

    //PdfiumAndroid (https://github.com/barteksc/PdfiumAndroid)
    //https://github.com/barteksc/AndroidPdfViewer/issues/49
    //Generate image from first page, and count number of pages (only for PDF files)
    void generateImageFromPdf(Uri pdfUri) {
        ImageView showPdf = (ImageView) findViewById(R.id.imageViewShowPDF);
        TextView textViewShowData = (TextView) findViewById(R.id.textViewShowData);
        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(this);
        SharedPreferences settings = getSharedPreferences("image_URI", 0);
        SharedPreferences.Editor editor = settings.edit();
        try {
            //http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
            ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(pdfUri, "r");
            com.shockwave.pdfium.PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);

            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
            saveImage(bmp);
            showPdf.setImageBitmap(bmp);
            Uri uri = getImageUri(this, bmp);
            editor.putString("imageURI", uri.toString());
            editor.commit();
            int pageCount = pdfiumCore.getPageCount(pdfDocument);
            String trenutniText = (String) textViewShowData.getText();
            textViewShowData.setText(trenutniText + "\n\tBroj stranica: " + Integer.toString(pageCount));
            pdfiumCore.closeDocument(pdfDocument); // important!
        } catch(Exception e) {
            //todo with exception
        }
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        //On pre execute show progress dialog with message "Učitavanje"
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(PrintActivity.this);
            pd.setMessage("Učitavanje...");
            pd.setCancelable(false);
            pd.show();
        }
        //Make connection to server and fetch server response in background
        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        //On post execute decode JSON array and put all entries into List of strings
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            if (result != null) {

                // ...
                JSONArray json = null;
                try {
                    json = new JSONArray(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                for(int i=0;i<json.length();i++){
                    JSONObject e = null;
                    try {
                        e = json.getJSONObject(i);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                    try {

                        spinner_array.add(e.getString("naziv")+" "+ e.getString("adresa"));


                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }


                }

                //Put contents of List<Strings> inside a spinner through ArrayAdapter
                Spinner spinner = (Spinner) findViewById(R.id.spinnerPrintList);

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(PrintActivity.this,   android.R.layout.simple_spinner_item, spinner_array);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);


            }


        }
    }

    private void saveImage(Bitmap bmp) {
        FileOutputStream out = null;
        try {
            File folder = new File(FOLDER);
            if(!folder.exists())
                folder.mkdirs();
            File file = new File(folder, "PDF.png");
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            //todo with exception
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
                //todo with exception
            }
        }
    }



    /*Icon send file to copy shop is set in the menu */

    private void getInfo(){

        SharedPreferences pref_print=this.getSharedPreferences("Login",0);

        TextView info = (TextView)findViewById(R.id.textViewShowData);

        info.setText("\n\tEMAIL= "+pref_print.getString("email", null)+"\n\tPASS= "+pref_print.getString("password", null)+
                "\n\tID= "+pref_print.getString("id", null)+"\n\tIME: "+pref_print.getString("ime", null)+"\n\tPREZIME= "+
                pref_print.getString("prezime", null)+"\n\tTEL= "+pref_print.getString("tel", null));

    }


    /*Icon send file to copy shop is set in the menu */
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.print_activity_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.print_logout:

                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();

                Intent intent = new Intent(this, LoginActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void viewMap(View view) {

        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }

    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.previous_postavke:

                LinearLayout layout_animation = (LinearLayout)findViewById(R.id.master_layout);

//enable animation
                layout_animation.setLayoutTransition(null);

//disable animation



                int resID = getResources().getIdentifier(labels_postavke[cursor_postavke], "id", getPackageName());
                TextView label = (TextView)findViewById(resID);
                label.setVisibility(View.GONE);

                resID = getResources().getIdentifier(layout_postavke[cursor_postavke], "id", getPackageName());
                LinearLayout layout = (LinearLayout) findViewById(resID);
                layout.setVisibility(View.GONE);

                cursor_postavke=cursor_postavke-1;

                LayoutTransition layoutTransition = new LayoutTransition();
                layout_animation.setLayoutTransition(layoutTransition);

                resID = getResources().getIdentifier(labels_postavke[cursor_postavke], "id", getPackageName());
                label = (TextView)findViewById(resID);
                label.setVisibility(View.VISIBLE);

                resID = getResources().getIdentifier(layout_postavke[cursor_postavke], "id", getPackageName());
                layout = (LinearLayout) findViewById(resID);
                layout.setVisibility(View.VISIBLE);

                Button button_next = (Button)findViewById(R.id.next_postavke);
                Button button_previous = (Button)findViewById(R.id.previous_postavke);

                if(cursor_postavke==0){
                    button_previous.setVisibility(View.GONE);
                }else if(cursor_postavke==1 && button_next.getVisibility()==View.GONE){
                    button_next.setVisibility(View.VISIBLE);
                }


                break;
            case R.id.next_postavke:

                layout_animation = (LinearLayout)findViewById(R.id.master_layout);
                layout_animation.setLayoutTransition(null);

                resID = getResources().getIdentifier(labels_postavke[cursor_postavke], "id", getPackageName());
                label = (TextView)findViewById(resID);
                label.setVisibility(View.GONE);

                resID = getResources().getIdentifier(layout_postavke[cursor_postavke], "id", getPackageName());
                layout = (LinearLayout) findViewById(resID);
                layout.setVisibility(View.GONE);

                cursor_postavke=cursor_postavke+1;

                layoutTransition = new LayoutTransition();
                layout_animation.setLayoutTransition(layoutTransition);

                resID = getResources().getIdentifier(labels_postavke[cursor_postavke], "id", getPackageName());
                label = (TextView)findViewById(resID);
                label.setVisibility(View.VISIBLE);

                resID = getResources().getIdentifier(layout_postavke[cursor_postavke], "id", getPackageName());
                layout = (LinearLayout) findViewById(resID);
                layout.setVisibility(View.VISIBLE);
                button_next = (Button)findViewById(R.id.next_postavke);
                button_previous = (Button)findViewById(R.id.previous_postavke);

                if(cursor_postavke==2){
                    button_next.setVisibility(View.GONE);
                } else if(cursor_postavke==1 && button_next.getVisibility()==View.GONE){
                    button_next.setVisibility(View.VISIBLE);
                } else if(cursor_postavke==1 && button_previous.getVisibility()==View.GONE){
                    button_previous.setVisibility(View.VISIBLE);
                }




                break;
        }
    }
    public void showImage() {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        SharedPreferences settings = this.getSharedPreferences("image_URI", 0);
        String imageUriString = settings.getString("imageURI", null);
        Uri imageUri = Uri.parse(imageUriString);

        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                1200,
                1200));
        builder.show();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}