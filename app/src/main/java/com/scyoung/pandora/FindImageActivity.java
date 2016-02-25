package com.scyoung.pandora;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class FindImageActivity extends AppCompatActivity {

    private final int SELECT_PHOTO = 1;
    private SharedPreferences prefs;
    private Resources res;
    private String INTENT_BUTTON_NAME;
    private int INTENT_BUTTON_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_image);
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

        prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);
        res = getResources();
        prepareButtons();
    }

    private void prepareButtons() {
        ArrayList<View> buttons = getViewsByTag((ViewGroup) findViewById(android.R.id.content), "activityImage");
        for (View button : buttons) {
            Button activityButton = (Button) button;
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
                activityButton.setText(R.string.replace);
                activityButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        findImage(v);
                    }
                });
            }
            if (encodedImage != null) {
                setBackground(activityButton, new BitmapDrawable(getResources(), decodeBase64(encodedImage)));
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
                    BitmapDrawable backgroundDrawable = new BitmapDrawable(res, selectedImage);

                    if (decodeUri(imageUri) != null) {
                        Button replaceButton = (Button) findViewById(INTENT_BUTTON_ID);
                        replaceButton.setText("");
                        setBackground(replaceButton, backgroundDrawable);
                        replaceButton.setOnClickListener(new Button.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                shortClick(v);
                            }
                        });
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(INTENT_BUTTON_NAME, encodeBase64(selectedImage));
                        editor.commit();
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
            final InputStream imageStream = getContentResolver().openInputStream(uri);
            ret = BitmapFactory.decodeStream(imageStream);
        } catch (FileNotFoundException e) {
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
        Toast.makeText(this, "Perform new Action", Toast.LENGTH_LONG).show();
    }

    public void longClick(View v) {
        Toast.makeText(this, "Show Dialog requesting button edit", Toast.LENGTH_LONG).show();
    }

}
