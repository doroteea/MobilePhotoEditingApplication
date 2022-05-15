package com.example.phoroeditorapplication2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phoroeditorapplication2.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Used to load the 'phoroeditorapplication2' library on application startup.
    static {
        System.loadLibrary("phoroeditorapplication2");
    }

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set layout
        setContentView(R.layout.activity_main);
        init();
    }

    //permissions
    private static final int REQUEST_PERMISSIONS = 1234;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSION_COUNT = 2;
    @SuppressLint("NewApi")
    private boolean notPermissions() {
        for (int i = 0; i < PERMISSION_COUNT; i++) {
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        //permission granted
        return false;
    }

    //black and white method
    private static native void grayscale(int pixels[], int width, int height);
    private static native void bw(int pixels[],int width,int height);
    private static native void sepia(int pixels[],int width,int height);
    private static native void pastels(int pixels[],int width,int height);
    private static native void pixelate(int pixels[],int width,int height);
    private static native void invert(int[] pixels, int width, int height);
    private static native void brightness_p(int[] pixels,int width,int height,int brightness);
    private static native void contrast_p(int[] pixels,int width,int height,int contrast);

    int brightness = 0;
    int contrast = 0;

    public int truncateBrightness(int brightness){
        if(brightness>100){
            return 100;
        }
        if(brightness<-100){
            return -100;
        }
        return brightness;
    }
    public int truncateContrast(int contrast){
        if(contrast>100){
            return 100;
        }
        if(contrast<-100){
            return -100;
        }
        return contrast;
    }
    @Override
    protected void onResume() {
        super.onResume();
        //if we don't have permissions,we ask for them
        if (notPermissions() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
        }
    }

    //check granted or denied
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSIONS && grantResults.length>0){
            if(notPermissions()){
                ((ActivityManager)this.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                recreate();
            }
        }
    }

    public void onBackPressed(){
        if(editMode){
            findViewById(R.id.editScreen).setVisibility(View.GONE);
            findViewById(R.id.welcomeScreen).setVisibility(View.VISIBLE);
            editMode = false;
        }
        else{
            //not in edit mode => exit the application
            super.onBackPressed();
        }
    }

    private ImageView imageView;
    private static final int REQUEST_PICK_IMAGE = 12345;

    private void init() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        imageView = findViewById(R.id.imageView);

        if (!MainActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            findViewById(R.id.takePhotoButton).setVisibility(View.GONE);
        }

        final Button selectPhotoButton = findViewById(R.id.selectPhotoButton);

        selectPhotoButton.setOnClickListener(view -> {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            final Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
            final Intent chooserIntent = Intent.createChooser(intent,"Select Photo");
            startActivityForResult(chooserIntent,REQUEST_PICK_IMAGE);
//            startActivity(chooserIntent);
            //registerForActivityResult(chooserIntent,REQUEST_PICK_IMAGE);

        });

        final Button takePhotoButton = findViewById(R.id.takePhotoButton);

        takePhotoButton.setOnClickListener(view -> {
            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(getApplicationContext().getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA)){
                //create a file for the taken photo
                final File photoFile = createImageFile();
                imageUri = Uri.fromFile(photoFile);
                final SharedPreferences myPref = getSharedPreferences(appID,0);
                myPref.edit().putString("path",photoFile.getAbsolutePath()).apply();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
//                startActivity(takePictureIntent);
//
            }else{
                Toast.makeText(MainActivity.this,"Your Camera App is not compatible",
                        Toast.LENGTH_SHORT).show();

            }
        });

        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Select Filter");
        categories.add("grayscale");
        categories.add("black and white");
        categories.add("sepia");
        categories.add("invert");
        categories.add("pixelate");
        categories.add("pastel");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        final Button brightnessPlus = findViewById(R.id.brightnessPlus);

        brightnessPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    public void run(){
                        pixelsCopy = pixels.clone();
                        brightness+=5;
                        brightness_p(pixelsCopy,width,height, truncateBrightness(brightness));
                        bitmap.setPixels(pixelsCopy,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
            }
        });

        final Button brightnessMinus = findViewById(R.id.brightnessMinus);
        brightnessMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    public void run(){
                        pixelsCopy = pixels.clone();
                        brightness-=5;
                        brightness_p(pixelsCopy,width,height, truncateBrightness(brightness));
                        bitmap.setPixels(pixelsCopy,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
            }
        });

        final Button contrastPlus = findViewById(R.id.contrastPlus);

        contrastPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    public void run(){
                        pixelsCopy = pixels.clone();
                        contrast+=5;
                        contrast_p(pixelsCopy,width,height,truncateContrast(contrast));
                        bitmap.setPixels(pixelsCopy,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
            }
        });

        final Button contrastMinus = findViewById(R.id.contrastMinus);
        contrastMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    public void run(){
                        pixelsCopy = pixels.clone();
                        contrast-=5;
                        contrast_p(pixelsCopy,width,height,truncateContrast(contrast));
                        bitmap.setPixels(pixelsCopy,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
            }
        });

        final Button saveButton = findViewById(R.id.saveImage);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if(which == DialogInterface.BUTTON_POSITIVE) {
                            final File outputFile = createImageFile();
                            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                imageUri = Uri.parse("file://" + outputFile.getAbsolutePath());
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
                                Toast.makeText(MainActivity.this, "Image Saved", Toast.LENGTH_SHORT).show();

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                builder.setMessage("Do you want to save the image?")
                        .setPositiveButton("Yes",dialogClickListener)
                        .setNegativeButton("No",dialogClickListener)
                        .show();
            }
        });

        final Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                findViewById(R.id.editScreen).setVisibility(View.GONE);
                findViewById(R.id.welcomeScreen).setVisibility(View.VISIBLE);
                editMode = false;
            }
        });
    }

    private static final String appID = "photoEditor";
    private Uri imageUri;
    private static final int REQUEST_IMAGE_CAPTURE = 1012;

    private File createImageFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String imageFileName = "/JPEG_" + timeStamp + ".jpg";
        final File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(storageDir+imageFileName);
    }

    private boolean editMode = false;
    private Bitmap bitmap ;
    private int width = 0;
    private int height = 0;
    private static final int MAX_PIXEL_COUNT = 2048;

    private int[] pixels;
    private int[] pixelsCopy;
    private int pixelCount = 0;

    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //here is where we select the image
        //check result is success
        if(resultCode != RESULT_OK){
            return ;
        }
        if(requestCode == REQUEST_IMAGE_CAPTURE) {
            if (imageUri == null) {
                final SharedPreferences p = getSharedPreferences(appID, 0);
                final String path = p.getString("path", "");
                if (path.length() < 1) {
                    recreate();
                    return;
                }
                imageUri = Uri.parse("file://" + path);
            }
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
        }else if(data == null){
            recreate();
            return;
        }
        else if(requestCode == REQUEST_PICK_IMAGE)
        {
            imageUri = data.getData();
        }

        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this,"Loading","Please Wait",true);

//        editMode = true;
//
        findViewById(R.id.welcomeScreen).setVisibility(View.GONE);
        findViewById(R.id.editScreen).setVisibility(View.VISIBLE);

        new Thread(){
            public void run(){
                bitmap = null;
                final BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
                bmpOptions.inBitmap = bitmap;
                bmpOptions.inJustDecodeBounds = true;
                try(InputStream input = getContentResolver().openInputStream(imageUri)){
                    bitmap = BitmapFactory.decodeStream(input,null,bmpOptions);
                }catch(IOException e){
                    e.printStackTrace();
                }
                bmpOptions.inJustDecodeBounds = false;
                width = bmpOptions.outWidth;
                height = bmpOptions.outHeight;
                int resizeScale = 1;
                if(width> MAX_PIXEL_COUNT){
                    resizeScale=width/MAX_PIXEL_COUNT;
                }else if(height>MAX_PIXEL_COUNT){
                    resizeScale=height/MAX_PIXEL_COUNT;
                }
                if(width/resizeScale>MAX_PIXEL_COUNT || height/resizeScale > MAX_PIXEL_COUNT){
                    resizeScale++;
                }
                bmpOptions.inSampleSize = resizeScale;
                InputStream input = null;
                try{
                    input = getContentResolver().openInputStream(imageUri);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                    recreate();
                    return;
                }
                bitmap = BitmapFactory.decodeStream(input,null,bmpOptions);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                        dialog.cancel();
                    }
                });
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);

                pixelCount = width*height;
                pixels = new int[pixelCount];
                pixelsCopy = new int[pixelCount];
                bitmap.getPixels(pixels,0,width,0,0,width,height);
                bitmap.getPixels(pixelsCopy,0,width,0,0,width,height);


            }
        }.start();

    }

    public void setFilter(int filter_no){
        //Bitmap newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        new Thread(){
            public void run(){
                pixelsCopy = pixels.clone();
                switch (filter_no){
                    case 1:
                        grayscale(pixelsCopy,width,height);
                        break;
                    case 2:
                        bw(pixelsCopy,width,height);
                        break;
                    case 3:
                        sepia(pixelsCopy,width,height);
                        break;
                    case 4:
                        invert(pixelsCopy,width,height);
                        break;
                    case 5:
                        pixelate(pixelsCopy,width,height);
                        break;
                    case 6:
                        pastels(pixelsCopy,width,height);
                        break;
                }
                bitmap.setPixels(pixelsCopy,0,width,0,0,width,height);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        }.start();
    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // On selecting a spinner item
        String item = adapterView.getItemAtPosition(i).toString();
        bitmap.setPixels(pixels,0,width,0,0,width,height);
        switch (i){
            case 1:
                setFilter(1);
                break;
            case 2:
                setFilter(2);
                break;
            case 3:
                setFilter(3);
                break;
            case 4:
                setFilter(4);
                break;
            case 5:
                setFilter(5);
                break;
            case 6:
                setFilter(6);
                break;
        }
        // Showing selected spinner item
        if(i!=0)
            Toast.makeText(adapterView.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}