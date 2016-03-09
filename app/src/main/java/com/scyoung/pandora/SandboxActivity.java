package com.scyoung.pandora;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class SandboxActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private ImageView container;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sandbox);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        container = (ImageView)findViewById(R.id.image_container);
        int reqDimension = Math.min(size.x, size.y);
        Bitmap background = ImageUtil.getScaledBitmap(R.drawable.puzzle_pieces_white_corner, reqDimension, this);
        container.setImageBitmap(background);

        initPreferences();
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus && container == null) {
//            container = (ImageView)findViewById(R.id.image_container);
//            int reqDimension = Math.min(container.getWidth(), container.getHeight());
//            Bitmap background = ImageUtil.getScaledBitmap(R.drawable.puzzle_pieces_white_corner, reqDimension, this);
//            container.setImageBitmap(background);
//        }
//    }
    /**
     * Called when the user clicks the Send button
     */
    public void playSound(View view) {
        Intent intent = new Intent(this, PlaySoundActivity.class);
        startActivity(intent);
    }

    public void findSound(View view) {
        Intent intent = new Intent(this, FindSoundActivity.class);
        startActivity(intent);
    }

    public void loadLargeImage(View view) {
        Intent intent = new Intent(this, LargeImageActivity.class);
        startActivity(intent);
    }

    public void openRecordSound(View view) {
        Intent intent = new Intent(this, RecordSoundActivity.class);
        startActivity(intent);
    }

    public void openCombined(View view) {
        Intent intent = new Intent(this, CombinedActivity.class);
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

    public void openLinearLayoutPane(View view) {
        Intent intent = new Intent(this, LinearLayoutActivity.class);
        startActivity(intent);
    }

    public void openShowDynamicPopupPane(View view) {
        Intent intent = new Intent(this, ShowDynamicMenu.class);
        startActivity(intent);
    }

    /**
     * This will seed the preferences with the encoded no image for default button usage
     */
    private void initPreferences() {
        prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);
        SharedPreferences.Editor editor;

        //write no image pic to preferences as encoded string
        if (prefs.getString(getString(R.string.no_image_key), null) == null) {
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.noimage_large);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] b = stream.toByteArray();
            String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

            editor = prefs.edit();
            editor.putString(getString(R.string.no_image_key), imageEncoded);
            editor.commit();
        }

        // write no image pic to internal storage and set in preferences with new filename
        if (prefs.getString(getString(R.string.no_image_uri_key), null) == null) {
            String fileName = getString(R.string.no_image_uri_key);
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.noimage_large);
            File newInternalFile = writeBitmapToInternalStorage(fileName, image);
            if (newInternalFile != null) {
                editor = prefs.edit();
                editor.putString(fileName, newInternalFile.toString());
                editor.commit();
            }
        }

        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    public void flushPreferences(View view) {
//        Map<String, ?> allEntries = prefs.getAll();
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
//        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//            if (entry.getKey() != getString(R.string.no_image_key) && entry.getKey() != getString(R.string.no_image_uri_key)) {
//                editor.remove(entry.getKey());
//            }
//        }
        editor.commit();
        Log.d("Flushed Preferences: ", "complete");
        initPreferences();
//        allEntries = prefs.getAll();
//        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
//        }
    }

    private File writeBitmapToInternalStorage(String outputFilename, Bitmap bitmap) {
        FileOutputStream out;
        File buttonResourceFile = null;
        try {
            buttonResourceFile = new File(this.getFilesDir(), outputFilename);
            out = new FileOutputStream(buttonResourceFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return buttonResourceFile;
    }
}
