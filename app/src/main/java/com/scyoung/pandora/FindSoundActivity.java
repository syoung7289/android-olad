package com.scyoung.pandora;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FindSoundActivity extends AppCompatActivity {

    private final int SELECT_AUDIO = 1;
    private String INTENT_BUTTON_NAME;
    private int INTENT_BUTTON_ID;
    private Button CURRENTLY_PLAYING = null;
    private SharedPreferences prefs;
    private Resources res;
    private static MediaPlayer aMediaPlayer = null;

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

    private void playAudioForButton(Uri audioUri, Button activeButton) {
        if (aMediaPlayer != null) {
            aMediaPlayer.reset();
            aMediaPlayer.release();
            aMediaPlayer = null;
        }
        try {
            aMediaPlayer = new MediaPlayer();
            aMediaPlayer.setDataSource(this, audioUri);
            aMediaPlayer.setOnPreparedListener(aPreparedListener);
            aMediaPlayer.setVolume(100, 100);
            aMediaPlayer.setLooping(false);
            aMediaPlayer.setOnCompletionListener(aCompletionListener);
            CURRENTLY_PLAYING = activeButton;
            aMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
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
            playingButton.setText(R.string.audio_playing);
            Uri buttonSoundUri = Uri.parse(buttonSoundLocation);
            playAudioForButton(buttonSoundUri, playingButton);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent soundReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, soundReturnedIntent);
        switch (requestCode) {
            case SELECT_AUDIO:
                if (resultCode == RESULT_OK) {
                    final Button findSoundButton = (Button) findViewById(R.id.findSound);
                    final Uri audioUri = soundReturnedIntent.getData();
                    findSoundButton.setText(R.string.audio_playing);
                    playAudioForButton(audioUri, findSoundButton);
                    File buttonAudioFile =copyFileToInternalStorage("findSound", audioUri);

                    SharedPreferences.Editor editor = prefs.edit();
                    if (buttonAudioFile != null) {
                        editor.putString(INTENT_BUTTON_NAME, buttonAudioFile.toString());
                    }
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

    MediaPlayer.OnPreparedListener aPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            aMediaPlayer.start();
        }
    };

    MediaPlayer.OnCompletionListener aCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer arg0) {
            if (CURRENTLY_PLAYING != null) {
                CURRENTLY_PLAYING.setText(getResources().getString(R.string.play_sound));
                CURRENTLY_PLAYING = null;
            }
            aMediaPlayer.release();
            aMediaPlayer = null;
        }
    };

    private File copyFileToInternalStorage(String outputFilename, Uri audioUri) {
        FileOutputStream out;
        File buttonAudioFile = null;
        try {
            buttonAudioFile = new File(this.getFilesDir(), outputFilename);
            InputStream in = getContentResolver().openInputStream(audioUri);
            out = new FileOutputStream(buttonAudioFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return buttonAudioFile;
    }

}