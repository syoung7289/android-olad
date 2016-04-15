package com.scyoung.pandora;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;

public class ShowSelectionActivity extends AppCompatActivity {

    private static String SCREEN_ORIENTATION;
    private static MediaPlayer aMediaPlayer = null;
    private Uri imageLocation;
    private Uri audioLocation = null;
    private static boolean audioPlayed;
    RelativeLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int rotation = this.getWindow().getWindowManager().getDefaultDisplay().getRotation();
        SCREEN_ORIENTATION = ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) ? "LANDSCAPE" : "PORTRAIT");
        Log.d("CA", "onCreate started: " + SCREEN_ORIENTATION);
        setContentView(R.layout.activity_show_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.puzzleme_logo_no_background_wider);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        container = (RelativeLayout) findViewById(R.id.show_selection_container);

        // Set padding based on screen orientation
        ImageView selectedImage = (ImageView)findViewById(R.id.selectedImage);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) selectedImage.getLayoutParams();
        if (SCREEN_ORIENTATION == "PORTRAIT") {
            params.rightMargin = 0;
            params.bottomMargin = 200;
        }
        else {
            params.rightMargin = 200;
            params.bottomMargin = 0;
        }
        selectedImage.setLayoutParams(params);

        // Get the selected image from previous activity
        Bundle extras = getIntent().getExtras();
        imageLocation = Uri.parse(extras.getString("picSelected"));
        String audioString = extras.getString("assocAudio");
        audioPlayed = extras.getBoolean("audioPlayed", false);
        Log.d("SSAct", "audioPlayed pulled from bundle " + false);
        if (!audioString.isEmpty()) {
            audioLocation = Uri.parse(extras.getString("assocAudio"));
        }
        selectedImage.setImageBitmap(ImageUtil.getBitmap(imageLocation));
        selectedImage.setBackgroundColor(Color.TRANSPARENT);

        final ViewTreeObserver vto = container.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                public void onGlobalLayout() {
                    if (audioLocation != null && !audioPlayed) {
                        playAudioForButton(audioLocation);
                        audioPlayed = true;
                        Log.d("SSAct", "inside vto and audioPlayed set to " + true);
                        container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("audioPlayed", audioPlayed);
        Log.d("SSAct", "inside onSaveInstanceState and bundle updated with " + audioPlayed);
        super.onSaveInstanceState(outState);
    }

    private void playAudioForButton(Uri audioUri) {
        if (aMediaPlayer != null) {
            aMediaPlayer.reset();
            aMediaPlayer.release();
            aMediaPlayer = null;
        }
        try {
            aMediaPlayer = new MediaPlayer();
            aMediaPlayer.setDataSource(this, audioUri);
            aMediaPlayer.setOnPreparedListener(aPreparedListener);
            aMediaPlayer.setLooping(false);
            aMediaPlayer.setOnCompletionListener(aCompletionListener);
            playingIndicator(true);
            aMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    MediaPlayer.OnPreparedListener aPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            aMediaPlayer.start();
        }
    };

    MediaPlayer.OnCompletionListener aCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer arg0) {
            playingIndicator(false);
            if (aMediaPlayer != null) {
                aMediaPlayer.release();
                aMediaPlayer = null;
            }
        }
    };

    private void playingIndicator(boolean on) {
        ImageView indicator = (ImageView)findViewById(R.id.play_indicator);
        if (on) {
            indicator.setBackgroundResource(R.drawable.play_animation);
            AnimationDrawable background = (AnimationDrawable) indicator.getBackground();
            background.start();
        }
        else {
            indicator.setBackgroundResource(0);
        }
    }
}
