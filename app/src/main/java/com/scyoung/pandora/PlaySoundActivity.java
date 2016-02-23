package com.scyoung.pandora;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class PlaySoundActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_sound);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void playSound(View view) {
        final Button playSoundButton = (Button) findViewById(R.id.playSound);
        playSoundButton.setText("Playing...");
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.tada);
        mediaPlayer.setVolume(100, 100);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                playSoundButton.setText(getResources().getString(R.string.play_sound));
            }
        });
        mediaPlayer.start();
    }

    private void replaceText(Button button) {
        button.setText(getResources().getString(R.string.play_sound));
    }

}
