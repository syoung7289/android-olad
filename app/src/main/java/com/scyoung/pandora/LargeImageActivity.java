package com.scyoung.pandora;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class LargeImageActivity extends AppCompatActivity {

    private final int SELECT_PHOTO = 1;
    private SharedPreferences prefs;
    private Resources res;
    private String CURRENT_BUTTON_ABSOLUTE_NAME;
    private String CURRENT_BUTTON_NAME = "name_to_change";
    private int CURRENT_BUTTON_ID;
    private ImageButton CURRENT_BUTTON = null;
    RelativeLayout container;
    int margin = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_large_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);
        res = getResources();
        container = (RelativeLayout) findViewById(R.id.large_image_container);
        prepareButtons();
    }

    private void prepareButtons() {
        ArrayList<View> buttons = getViewsByTag((ViewGroup) findViewById(android.R.id.content), "activityImage");
        for (View button : buttons) {
            ImageButton activityButton = (ImageButton) button;
            String replaceImageKey = res.getResourceName(button.getId());
            boolean isUserSelected = prefs.getString(replaceImageKey, null) != null;
            String imageLocation = prefs.getString(replaceImageKey, (prefs.getString(getString(R.string.no_image_uri_key), null)));
            Uri imageUri = Uri.parse(imageLocation);

            if (isUserSelected) {
                activityButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shortClick(v);
                    }
                });
            }
            else {
                activityButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        findImage(v);
                    }
                });
            }
            if (imageLocation != null) {
                activityButton.setImageBitmap(getBitmap(imageUri));
                activityButton.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            final Object tagObj = child.getTag();
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }
        }
        return views;
    }

    /**
     * Called when the user clicks the Find Image button
     */
    public void findImage(View view) {
        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
        setAsCurrentButton((ImageButton)view);
        imagePickerIntent.setType("image/*");
        startActivityForResult(imagePickerIntent, SELECT_PHOTO);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode){
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    final Uri imageUri = imageReturnedIntent.getData();
                    Bitmap selectedImage = getScaledBitmap(imageUri);
                    if (selectedImage != null) {
                        File internalFile = saveBitmapToInternalStorage(CURRENT_BUTTON_NAME, selectedImage);
                        if (internalFile != null) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(CURRENT_BUTTON_ABSOLUTE_NAME, internalFile.toString());
                            editor.commit();

                            ImageButton replaceButton = (ImageButton) findViewById(CURRENT_BUTTON_ID);
                            replaceButton.setImageBitmap(selectedImage);
                            replaceButton.setBackgroundColor(Color.TRANSPARENT);
                            replaceButton.setOnClickListener(new Button.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    shortClick(v);
                                }
                            });
                        }
                    }
                }
        }
    }

    private Bitmap getBitmap(Uri imageUri) {
        Bitmap ret = null;
        try {
            File f = new File(imageUri.getPath());
            if (f.exists()) {
                Log.d("decodeUri", f.getAbsolutePath());
                ret = BitmapFactory.decodeFile(f.getAbsolutePath());
            }
            else {
                Log.d("LIA:decodeUri", "File not found: " + imageUri.getPath());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private Bitmap getScaledBitmap(Uri uri) {
        Bitmap ret;
        try {
            InputStream imageStream = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageStream, null, options);
            Log.d("LIA", "imageHeight is: " + options.outHeight);
            Log.d("LIA", "imageWidth is: " + options.outWidth);
            imageStream.close();

            int buttonLength = getButtonDimension(2, 1);
            options.inSampleSize = calculateInSampleSize(options, buttonLength, buttonLength);
            options.inJustDecodeBounds = false;
            imageStream = getContentResolver().openInputStream(uri);
            ret = BitmapFactory.decodeStream(imageStream, null, options);
            Log.d("LIA", "imageHeight is: " + ret.getHeight());
            Log.d("LIA", "imageWidth is: " + ret.getWidth());
            Log.d("LIA", "image in bytes: " + ret.getByteCount());
            imageStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            ret = null;
        }
        return ret;
    }

    public void shortClick(View v) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.width = 700;
        params.height = 700;
        v.setLayoutParams(params);
    }

    private int getButtonDimension(int numColumns, int numRows) {
        int width = container.getWidth();
        int height = container.getHeight();
        int vFreeSpace = getVerticalFreeSpace(height, numRows);
        int hFreeSpace = getHorizontalFreeSpace(width, numColumns);
        return Math.min(vFreeSpace / numRows, hFreeSpace / numColumns);
    }

    private int getHorizontalFreeSpace(int containerWidth, int numColumns) {
        Log.d("SDM", "containerWidth: " + containerWidth);
        Log.d("SDM", "numColumns: " + numColumns);
        int cPadding = (numColumns + 1) * margin;
        Log.d("SDM", "cPadding: " + cPadding);
        return containerWidth - cPadding;
    }

    private int getVerticalFreeSpace(int containerHeight, int numRows) {
        Log.d("SDM", "containerHeight: " + containerHeight);
        Log.d("SDM", "numRows: " + numRows);
        int rPadding = (numRows + 1) * margin;
        Log.d("SDM", "rPadding: " + rPadding);
        return containerHeight - rPadding;
    }

    private File saveBitmapToInternalStorage(String outputFilename, Bitmap in) {
        FileOutputStream out;
        File buttonResourceFile = null;
        try {
            buttonResourceFile = new File(this.getFilesDir(), outputFilename);
            out = new FileOutputStream(buttonResourceFile);
            in.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return buttonResourceFile;
    }

    public void setAsCurrentButton(ImageButton currentButton) {
        CURRENT_BUTTON = currentButton;
        CURRENT_BUTTON_ID = CURRENT_BUTTON.getId();
        CURRENT_BUTTON_ABSOLUTE_NAME = res.getResourceName(CURRENT_BUTTON_ID);
        CURRENT_BUTTON_NAME = res.getResourceEntryName(CURRENT_BUTTON_ID);
    }

}
