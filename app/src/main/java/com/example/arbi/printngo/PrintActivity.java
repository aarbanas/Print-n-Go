package com.example.arbi.printngo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shockwave.pdfium.PdfiumCore;



public class PrintActivity extends AppCompatActivity {

    final int ACTIVITY_CHOOSE_FILE = 1;
    private static final String TAG = "PrintActivity";
    public final static String FOLDER = Environment.getExternalStorageDirectory() + "/PDF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
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
            int pageCount = pdfiumCore.getPageCount(pdfDocument);
            textViewShowData.setText("Broj stranica: " + Integer.toString(pageCount));
            pdfiumCore.closeDocument(pdfDocument); // important!
        } catch(Exception e) {
            //todo with exception
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
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.print_activity_menu, menu);

        return true;
    }


}