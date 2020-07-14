package com.screenrecorder.screenrecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class PlayActivity extends AppCompatActivity {
    MediaController mediaController;
    VideoView vv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);


        vv = findViewById(R.id.vv);

        mediaController = new MediaController(this);
        mediaController.setAnchorView(vv);
        vv.setMediaController(mediaController);

        if(getIntent().getStringExtra("link")!=null){
            String videoLink = getIntent().getStringExtra("link");

            vv.setVideoURI(Uri.parse(videoLink));
            vv.start();
        }

        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //
            }
        });

        vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(PlayActivity.this, "Error", Toast.LENGTH_SHORT).show();
                return false;
            }
                    });

    }

}