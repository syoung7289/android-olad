package com.scyoung.pandora;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class RecordSoundActivity extends AppCompatActivity {

    private MediaRecorder myAudioRecorder;
    private String CURRENT_BUTTON_ABSOLUTE_NAME;
    private String CURRENT_BUTTON_NAME = "name_to_change";
    private int CURRENT_BUTTON_ID;
    private Button CURRENT_BUTTON = null;
    private String CURRENT_BUTTON_OUTPUT_FILE = null;
    private SharedPreferences prefs;
    private Resources res;
    private static MediaPlayer aMediaPlayer = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sound);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);
        res = getResources();

        prepareButtons();
    }

    private void prepareButtons() {
        Button soundButton = (Button) findViewById(R.id.recordSound);
        String buttonSoundKey = res.getResourceName(soundButton.getId());
        boolean isUserSelected = prefs.getString(buttonSoundKey, null) != null;
        if (isUserSelected) {
            soundButton.setText(R.string.play_sound);
            soundButton.setOnClickListener(playClickListener);
        }
        else {
            soundButton.setText(R.string.record_sound);
            soundButton.setOnClickListener(recordClickListener);
        }
    }

    public void recordSound(View view) {
        Button recordableButton = (Button) findViewById(R.id.recordSound);
        setAsCurrentButton(recordableButton);

        CURRENT_BUTTON_OUTPUT_FILE = this.getFilesDir() + "/" + CURRENT_BUTTON_NAME;
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(CURRENT_BUTTON_OUTPUT_FILE);

        try {
            myAudioRecorder.prepare();
            CURRENT_BUTTON.setText(R.string.button_finished_recording);
            CURRENT_BUTTON.setOnClickListener(recordingCompleteListener);
            myAudioRecorder.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void playSound(final View view) {
        final Button playingButton = (Button) view;
        final String buttonSoundLocation = prefs.getString(res.getResourceName(view.getId()), null);
        if (buttonSoundLocation != null) {
            playingButton.setText(R.string.audio_playing);
            Uri buttonSoundUri = Uri.parse(buttonSoundLocation);
            playAudioForButton(buttonSoundUri, playingButton);
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
            aMediaPlayer.setOnCompletionListener(playCompletionListener);
            CURRENT_BUTTON = activeButton;
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

    MediaPlayer.OnCompletionListener playCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer arg0) {
        if (CURRENT_BUTTON != null) {
            CURRENT_BUTTON.setText(getResources().getString(R.string.play_sound));
            CURRENT_BUTTON = null;
        }
        aMediaPlayer.release();
        aMediaPlayer = null;
        }
    };

    Button.OnClickListener playClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            playSound(v);
        }
    };

    Button.OnClickListener recordClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            recordSound(v);
        }
    };

    Button.OnClickListener recordingCompleteListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            CURRENT_BUTTON.setText(getResources().getString(R.string.play_sound));
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder = null;
            File newRecording = new File(CURRENT_BUTTON_OUTPUT_FILE);
            SharedPreferences.Editor editor = prefs.edit();
            if (newRecording != null) {
                editor.putString(CURRENT_BUTTON_ABSOLUTE_NAME, newRecording.toString());
                editor.commit();
                CURRENT_BUTTON.setOnClickListener(playClickListener);
            }
        }
    };

    public void setAsCurrentButton(Button currentButton) {
        CURRENT_BUTTON = currentButton;
        CURRENT_BUTTON_ID = CURRENT_BUTTON.getId();
        CURRENT_BUTTON_ABSOLUTE_NAME = res.getResourceName(CURRENT_BUTTON_ID);
        CURRENT_BUTTON_NAME = res.getResourceEntryName(CURRENT_BUTTON_ID);
    }
}
