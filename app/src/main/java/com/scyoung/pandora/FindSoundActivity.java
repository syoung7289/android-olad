package com.scyoung.pandora;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class FindSoundActivity extends AppCompatActivity {

    private final int SELECT_AUDIO = 1;
    private String INTENT_BUTTON_NAME;
    private int INTENT_BUTTON_ID;
    private SharedPreferences prefs;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_sound);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);
        res = getResources();
        prepareButtons();
    }

    private void prepareButtons() {
        Button soundButton = (Button) findViewById(R.id.findSound);
        String buttonSoundKey = res.getResourceName(soundButton.getId());
        boolean isUserSelected = prefs.getString(buttonSoundKey, null) != null;
        if (isUserSelected) {
            soundButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playSound(v);
                }
            });
        }
    }

    public void findSound(View view) {
        Intent soundPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        soundPickerIntent.setType("audio/*");
        INTENT_BUTTON_ID = view.getId();
        INTENT_BUTTON_NAME = res.getResourceName(INTENT_BUTTON_ID);
        startActivityForResult(soundPickerIntent, SELECT_AUDIO);
    }

    public void playSound(final View view) {
        final Button playingButton = (Button) view;
        final String buttonSoundLocation = prefs.getString(res.getResourceName(view.getId()), null);
        if (buttonSoundLocation != null) {
            playingButton.setText("Playing...");
            Uri buttonSoundUri = Uri.parse(buttonSoundLocation);
            MediaPlayer pMediaPlayer = MediaPlayer.create(this, buttonSoundUri);
            pMediaPlayer.setVolume(100, 100);
            pMediaPlayer.setLooping(false);
            pMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer arg0) {
                    playingButton.setText(getResources().getString(R.string.play_sound));
                }
            });
            pMediaPlayer.start();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent soundReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, soundReturnedIntent);
        switch (requestCode){
            case SELECT_AUDIO:
                if (resultCode == RESULT_OK) {
                    final Button findSoundButton = (Button) findViewById(R.id.findSound);
                    final Uri audioUri = soundReturnedIntent.getData();
                    findSoundButton.setText("Playing...");
                    MediaPlayer sMediaPlayer = MediaPlayer.create(this, audioUri);
                    if (sMediaPlayer != null) {
                        sMediaPlayer.setVolume(100, 100);
                        sMediaPlayer.setLooping(false);
                        sMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer arg0) {
                                findSoundButton.setText(getResources().getString(R.string.play_sound));
                            }
                        });
                        sMediaPlayer.start();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(INTENT_BUTTON_NAME, audioUri.toString());
                        editor.commit();
                        findSoundButton.setOnClickListener(new Button.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                playSound(v);
                            }
                        });
                    }
                }
        }
    }

}
