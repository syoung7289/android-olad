package com.scyoung.pandora;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class LargeImageActivity extends AppCompatActivity {

    private final int SELECT_PHOTO = 1;
    private SharedPreferences prefs;
    private Resources res;
    private String INTENT_BUTTON_NAME;
    private int INTENT_BUTTON_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_large_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);
        res = getResources();
        prepareButtons();
    }

    private void prepareButtons() {
        ArrayList<View> buttons = getViewsByTag((ViewGroup) findViewById(android.R.id.content), "activityImage");
        for (View button : buttons) {
            ImageButton activityButton = (ImageButton) button;
            String replaceImageKey = res.getResourceName(button.getId());
            boolean isUserSelected = prefs.getString(replaceImageKey, null) != null;
            String encodedImage = prefs.getString(replaceImageKey, (prefs.getString(getString(R.string.no_image_key), null)));
            if (isUserSelected) {
                activityButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shortClick(v);
                    }
                });
            }
            else {
//                activityButton.setText(R.string.replace);
                activityButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        findImage(v);
                    }
                });
            }
            if (encodedImage != null) {
                activityButton.setImageBitmap(decodeBase64(encodedImage));
                activityButton.setBackgroundColor(Color.TRANSPARENT);
//                setBackground(activityButton, new BitmapDrawable(getResources(), decodeBase64(encodedImage)));
            }
            button.setOnLongClickListener(new Button.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    longClick(v);
                    return true;
                }
            });
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
        imagePickerIntent.setType("image/*");
        INTENT_BUTTON_ID = view.getId();
        INTENT_BUTTON_NAME = res.getResourceName(INTENT_BUTTON_ID);
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

    public void findImageArchive(View view) {
        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
        imagePickerIntent.setType("image/*");
        INTENT_BUTTON_ID = R.id.replaceImage;
        INTENT_BUTTON_NAME = res.getResourceName(INTENT_BUTTON_ID);
        startActivityForResult(imagePickerIntent, SELECT_PHOTO);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode){
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    final Uri imageUri = imageReturnedIntent.getData();
                    Bitmap selectedImage = decodeUri(imageUri);

                    if (decodeUri(imageUri) != null) {
                        ImageButton replaceButton = (ImageButton) findViewById(INTENT_BUTTON_ID);
                        replaceButton.setImageBitmap(selectedImage);
                        replaceButton.setBackgroundColor(Color.TRANSPARENT);
                        Log.d("LIA", "PaddingBottom: " + replaceButton.getPaddingBottom());
                        Log.d("LIA", "PaddingTop: " + replaceButton.getPaddingTop());
                        Log.d("LIA", "PaddingRight: " + replaceButton.getPaddingRight());
                        Log.d("LIA", "PaddingLeft: " + replaceButton.getPaddingLeft());
                        replaceButton.setOnClickListener(new Button.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                shortClick(v);
                            }
                        });
//                        SharedPreferences.Editor editor = prefs.edit();
//                        editor.putString(INTENT_BUTTON_NAME, encodeBase64(selectedImage));
//                        editor.commit();
                    }
                }
        }
    }

    private String encodeBase64(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] b = stream.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private Bitmap decodeUri(Uri uri) {
        Bitmap ret;
        try {
            InputStream imageStream = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageStream, null, options);
            Log.d("LIA", "imageHeight is: " + options.outHeight);
            Log.d("LIA", "imageWidth is: " + options.outWidth);
            Log.d("LIA", "imageType is: " + options.outMimeType);
            imageStream.close();

            options.inSampleSize = calculateInSampleSize(options, 850, 850);
            options.inJustDecodeBounds = false;
            imageStream = getContentResolver().openInputStream(uri);
            ret = BitmapFactory.decodeStream(imageStream, null, options);
             Log.d("LIA", "imageHeight is: " + options.outHeight);
            Log.d("LIA", "imageWidth is: " + ret.getWidth());
            Log.d("LIA", "imageType is: " + options.outMimeType);
            Log.d("LIA", "image in bytes: " + ret.getByteCount());
            imageStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            ret = null;
        }
        return ret;
    }

    private Bitmap decodeBase64(String encodedImage) {
        byte[] decodeByte = Base64.decode(encodedImage, 0);
        return BitmapFactory.decodeByteArray(decodeByte, 0, decodeByte.length);
    }

    public void setBackground(Button button, BitmapDrawable drawable) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            button.setBackgroundDrawable(drawable);
        } else {
            button.setBackground(drawable);
        }
    }

    public void shortClick(View v) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.width = 700;
        params.height = 700;
        v.setLayoutParams(params);
    }

    public void longClick(View v) {
        Toast.makeText(this, "Show Dialog requesting button edit", Toast.LENGTH_LONG).show();
    }

}
