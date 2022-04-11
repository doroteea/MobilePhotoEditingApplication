package com.example.phoroeditorapplication2;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.phoroeditorapplication2.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'phoroeditorapplication2' library on application startup.
    static {
        System.loadLibrary("phoroeditorapplication2");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        init();
    }

    /**
     * A native method that is implemented by the 'phoroeditorapplication2' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

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


    private static native void blackAndWhite(int pixels[],int width,int height);
    @Override
    protected void onResume() {
        super.onResume();
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
        final Button blackAndWhiteButton = findViewById(R.id.blackAndWhite);
        blackAndWhiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    public void run(){
//                        for(int i = 0; i<pixelCount;i++){
//                            //create an array on bytes
//                            pixels[i]/=2;
//                        }
                        blackAndWhite(pixels,width,height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
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
                bitmap.getPixels(pixels,0,width,0,0,width,height);


            }
        }.start();

    }
}