package com.qixupeng.cameraworkshop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_TAKE_PHOTO = 0;
    public static final int REQUEST_TAKE_VIDEO = 1;
    public static final int REQUEST_PICK_PHOTO = 2;
    public static final int REQUEST_PICK_VIDEO = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    private Uri mMediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        //imageview button
        findViewById(R.id.single_touchimageview_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SingleTouchImageViewActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_PICK_PHOTO) {
                if (data != null) {
                    mMediaUri = data.getData();
                }

                Intent intent = new Intent(this, ViewImageActivity.class);
                intent.setData(mMediaUri);
                startActivity(intent);
            }
            else if (requestCode == REQUEST_TAKE_VIDEO) {
                Intent intent = new Intent(Intent.ACTION_VIEW, mMediaUri);
                intent.setDataAndType(mMediaUri, "video/*");
                startActivity(intent);
            }
            else if (requestCode == REQUEST_PICK_VIDEO) {
                if (data != null) {
                    Log.i(TAG, "Video content URI: " + data.getData());
                    Toast.makeText(this, "Video content URI: " + data.getData(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.takePhoto)
    void takePhoto() {
        mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        if (mMediaUri == null) {
            Toast.makeText(this,
                    "There was a problem accessing your device's external storage.",
                    Toast.LENGTH_LONG).show();
        }
        else {
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
            startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @OnClick(R.id.takeVideo)
    void takeVideo() {
        mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        if (mMediaUri == null) {
            Toast.makeText(this,
                    "There was a problem accessing your device's external storage.",
                    Toast.LENGTH_LONG).show();
        }
        else {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
            startActivityForResult(takeVideoIntent, REQUEST_TAKE_VIDEO);
        }
    }

    @OnClick(R.id.pickPhoto)
    void pickPhoto() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickPhotoIntent.setType("image/*");
        startActivityForResult(pickPhotoIntent, REQUEST_PICK_PHOTO);
    }

    @OnClick(R.id.pickVideo)
    void pickVideo() {
        Intent pickVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickVideoIntent.setType("video/*");
        startActivityForResult(pickVideoIntent, REQUEST_PICK_VIDEO);
    }

    String mCurrentPhotoPath;

    private Uri getOutputMediaFileUri(int mediaType) {
        // check for external storage
        if (isExternalStorageAvailable()) {
            // get the URI

            // 1. Get the external storage directory  PUBLIC state
           // File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File mediaStorageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);

            // 2. Create a unique file name
            String fileName = "";
            String fileType = "";
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            if (mediaType == MEDIA_TYPE_IMAGE){
                fileName = "IMG_"+ timeStamp;
                fileType = ".jpg";
            } else if (mediaType == MEDIA_TYPE_VIDEO) {
                fileName = "VID_"+ timeStamp;
                fileType = ".mp4";
            } else {
                return null;
            }

            // 3. Create the file
            File mediaFile;
            try {
                //creatTempFile
                mediaFile = File.createTempFile(fileName, fileType, mediaStorageDir);
                Log.i(TAG, "File: " + Uri.fromFile(mediaFile));

                mCurrentPhotoPath = "File: " + mediaFile.getAbsolutePath();

                //gallery addpic
                galleryAddPic();
                // 4. Return the file's URI
                return Uri.fromFile(mediaFile);
            }
            catch (IOException e) {
                Log.e(TAG, "Error creating file: " +
                        mediaStorageDir.getAbsolutePath() + fileName + fileType);
            }
        }

        // something went wrong
        return null;
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        else {
            return false;
        }
    }

    //problems here
    private void galleryAddPic() {

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}














