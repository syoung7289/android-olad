package com.scyoung.pandora;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.util.Map;

public class SandboxActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sandbox);
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

        initPreferences();
    }

    /**
     * Called when the user clicks the Send button
     */
    public void playSound(View view) {
        Intent intent = new Intent(this, PlaySoundActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user clicks the Find Image button
     */
    public void findImage(View view) {
        Intent intent = new Intent(this, FindImageActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user clicks the Send button
     */
    public void openParameterActivityPane(View view) {
        Intent intent = new Intent(this, PassParameterActivity.class);
        startActivity(intent);
    }

    /**
     * This will seed the preferences with the encoded no image for default button usage
     */
    private void initPreferences() {
        prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);
        if (prefs.getString(getString(R.string.no_image_key), null) == null) {

            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.noimage);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] b = stream.toByteArray();
            String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.no_image_key), imageEncoded);
            editor.commit();
        }
        else {
            Map<String, ?> allEntries = prefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            }
        }
    }

    public void flushPreferences(View view) {
        Map<String, ?> allEntries = prefs.getAll();
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey() != getString(R.string.no_image_key)) {
                editor.remove(entry.getKey());
            }
        }
        editor.commit();
        Log.d("Flushed Preferences: ", "complete");
        allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
        }
    }
}
