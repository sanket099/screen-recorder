package com.screenrecorder.screenrecorder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.provider.MediaStore.MediaColumns.ORIENTATION;

public class MainActivity extends AppCompatActivity {

    //day 2 : added recycler view

    //things to add :
    /*
    * 1. play pause button and progress //
    * 2. notification controls start rec and stop rec
    * 3. background problem
    * 4. design : delete ,share
    * 5. intro page and misc */


    recyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    public static final int REQUEST_CODE = 1000;
    File mydir;
    public static final int WIDTH = 720;
    public static final int HEIGHT = 1280;
    private MediaProjectionManager mediaProjectionManager;

    File mediaStorage;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;

    private MediaRecorder mediaRecorder;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private MediaProjectionCallback mediacallback;


    private String videoLink = "";

    private int ScreenDensity;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private ToggleButton toggleButton;
    private VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
       // mydir = this.getDir("mydir", Context.MODE_PRIVATE);
        mediaStorage = new File(Environment.getExternalStorageDirectory(), "screenrecorder");


        System.out.println("mediaStorage.mkdir() = " + mediaStorage.mkdir());

        ScreenDensity = metrics.densityDpi;

        mediaRecorder = new MediaRecorder();
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        videoView = findViewById(R.id.video_view);
        toggleButton = findViewById(R.id.toggle_button);
        recyclerView = findViewById(R.id.recycler_view);

        requestPermissions();

        fillRecycler();

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermissions()) {


                        toggleButton.setChecked(false);

                        //request permissions

                        requestPermissions();




                }
                else{

                    toggleScreen(v);

                   /* Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
*/
                }
            }
        });
    }

    private void fillRecycler() {
        ArrayList<String> arrayList = new ArrayList<>();
        if(mediaStorage.listFiles() == null){
            Toast.makeText(this, "empty", Toast.LENGTH_SHORT).show();
        }
        else {
            File[] files = mediaStorage.listFiles();
            for (File file : files) {
                arrayList.add(file.getName());

            }
        }

        recyclerAdapter = new recyclerAdapter(MainActivity.this,arrayList);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));





    }

    private class AsynTask extends AsyncTask<View,Void,Void>{

        @Override
        protected Void doInBackground(View... views) {
            return null;
        }
    }
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO , Manifest.permission.READ_EXTERNAL_STORAGE},
                1
        );
    }


    private void toggleScreen(View v) {
        if(((ToggleButton)v).isChecked()){
            initRecorder();

            recordScreen();
        }
        else{
            mediaRecorder.stop();
            mediaRecorder.reset();
            stopRecordScreen();

           // videoView.setVisibility(View.VISIBLE);

            startActivity(new Intent(MainActivity.this,PlayActivity.class).putExtra("link",videoLink));

           /* videoView.setVideoURI(Uri.parse(videoLink));
            videoView.start();*/
        }
    }

    private void initRecorder(){

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        if(mediaStorage.exists()){
            System.out.println("exists");

        }

        ; //Creating an internal dir;
       /* File fileWithinMyDir = new File(mydir, "myfile"); //Getting a file within the dir.
        FileOutputStream out = new FileOutputStream(fileWithinMyDir); //Use the stream as usual to write into the file.
        s*/
        //recycler view
        videoLink = mediaStorage
                + new StringBuilder("/Screen_Record_").append(new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss")
        .format(new Date())).append(".mp4").toString();

        System.out.println("video link = " + videoLink) ;

        mediaRecorder.setOutputFile(videoLink);
        mediaRecorder.setVideoSize(WIDTH, HEIGHT);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncodingBitRate(512*1000);
        mediaRecorder.setVideoFrameRate(30);

        int rotation =getWindowManager().getDefaultDisplay().getRotation();
        int orientation =ORIENTATIONS.get(rotation+90);
        mediaRecorder.setOrientationHint(orientation);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private void recordScreen(){

        if(mediaProjection == null){
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),REQUEST_CODE);
            return;
        }
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();


    }

    private VirtualDisplay createVirtualDisplay() {

        return mediaProjection.createVirtualDisplay("MainActivity",WIDTH,HEIGHT,ScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
        , mediaRecorder.getSurface(),null,null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode != REQUEST_CODE){
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            return;
        }
        if(resultCode != RESULT_OK){
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            toggleButton.setChecked(false);
            return;


        }

        mediacallback = new MediaProjectionCallback();
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode,data);
        mediaProjection.registerCallback(mediacallback,null);
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();

    }

    private class MediaProjectionCallback extends MediaProjection.Callback{

        @Override
        public void onStop() {
            if(toggleButton.isChecked()){
                toggleButton.setChecked(false);
                mediaRecorder.stop();
                mediaRecorder.reset();
            }
            mediaProjection = null;
            stopRecordScreen();

            super.onStop();
        }
    }

    private void stopRecordScreen() {

        if(virtualDisplay == null){
             return;

        }
        virtualDisplay.release();
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {

        if(mediaProjection != null){
            mediaProjection.unregisterCallback(mediacallback);
            mediaProjection.stop();
            mediaProjection = null;

        }


    }

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/
}