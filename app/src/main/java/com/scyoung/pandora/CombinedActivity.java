package com.scyoung.pandora;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CombinedActivity extends AppCompatActivity {

    ImageButton[] viewButtons = new ImageButton[6];
    ImageView[] buttonIndicators = new ImageView[6];
    private final int SELECT_PHOTO = 1;
    private final int SELECT_AUDIO = 2;
    private final int DEFAULT = 0;
    private final int DEFAULT_IMAGE = 1;
    private final int DEFAULT_AUDIO = 2;
    private final int DEFAULT_IMAGE_AUDIO = 3;
    private final String IMAGE_TYPE = "_IMAGE";
    private final String AUDIO_TYPE = "_AUDIO";
    private SharedPreferences prefs;
    private Resources res;
    private static MediaPlayer aMediaPlayer = null;
    private MediaRecorder myAudioRecorder;
    private String CURRENT_BUTTON_ABSOLUTE_NAME;
    private String CURRENT_BUTTON_NAME = "name_to_change";
    private int CURRENT_BUTTON_ID;
    private ImageButton CURRENT_BUTTON = null;
    private String CURRENT_BUTTON_OUTPUT_FILE = null;
    private boolean shouldRedraw = false;
    RelativeLayout container;
    int margin = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int rotation = this.getWindow().getWindowManager().getDefaultDisplay().getRotation();
        Log.d("CA", "onCreate started: " + ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) ? "LANDSCAPE" : "PORTRAIT"));
        setContentView(R.layout.activity_combined);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        shouldRedraw = false;
        prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);
        res = getResources();
        container = (RelativeLayout) findViewById(R.id.combined_container);
        final ViewTreeObserver vto = container.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                public void onGlobalLayout() {
                    if (shouldRedraw) {
                        redrawButtons();
                        shouldRedraw = false;
                        container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
        buildButtons();
        Log.d("CA", "onCreate ended");
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d("CA", "onResume started");
//        redrawButtons();
//    }

/*****
     * Button Management
     */
    public void buildButtons() {
        initButtonArray();

        //TODO: Add collapse function for default buttons between active buttons
        boolean firstVisibleFound = false;
        for (int i = viewButtons.length - 1; i >= 0; i--) {
            viewButtons[i].setOnTouchListener(aButtonTouchEffect);
            String buttonName = res.getResourceName(viewButtons[i].getId());
            String replaceImageKey = buttonName + IMAGE_TYPE;
            boolean isImageSelected = prefs.getString(replaceImageKey, null) != null;
            String imageLocation = prefs.getString(replaceImageKey, (prefs.getString(getString(R.string.no_image_uri_key), null)));
            Uri imageUri = Uri.parse(imageLocation);
            if (isImageSelected) {
                firstVisibleFound = true;
                viewButtons[i].setOnClickListener(buttonSelectedClickListener);
                viewButtons[i].setVisibility(View.VISIBLE);
                viewButtons[i].setTag(DEFAULT_IMAGE);
                boolean isAudioSelected = prefs.getString(buttonName + AUDIO_TYPE, null) != null;
                if (isAudioSelected) {
                    viewButtons[i].setTag(DEFAULT_IMAGE_AUDIO);
                }
            }
            else {
                if (firstVisibleFound) {
                    viewButtons[i].setVisibility(View.VISIBLE);
                }
                viewButtons[i].setOnClickListener(findImageClickListener);
            }
            if (imageLocation != null) {
                viewButtons[i].setImageBitmap(getBitmap(imageUri));
                viewButtons[i].setBackgroundColor(Color.TRANSPARENT);
                registerForContextMenu(viewButtons[i]);
            }
        }
    }

    private void initButtonArray() {
        viewButtons[0] = (ImageButton) findViewById(R.id.combinedButton0);
        viewButtons[0].setTag(DEFAULT);
        viewButtons[1] = (ImageButton) findViewById(R.id.combinedButton1);
        viewButtons[1].setTag(DEFAULT);
        viewButtons[2] = (ImageButton) findViewById(R.id.combinedButton2);
        viewButtons[2].setTag(DEFAULT);
        viewButtons[2].setVisibility(View.GONE);
        viewButtons[3] = (ImageButton) findViewById(R.id.combinedButton3);
        viewButtons[3].setVisibility(View.GONE);
        viewButtons[3].setTag(DEFAULT);
        viewButtons[4] = (ImageButton) findViewById(R.id.combinedButton4);
        viewButtons[4].setVisibility(View.GONE);
        viewButtons[4].setTag(DEFAULT);
        viewButtons[5] = (ImageButton) findViewById(R.id.combinedButton5);
        viewButtons[5].setVisibility(View.GONE);
        viewButtons[5].setTag(DEFAULT);
        buttonIndicators[0] = (ImageView) findViewById(R.id.combinedButton0_indicator);
        buttonIndicators[1] = (ImageView) findViewById(R.id.combinedButton1_indicator);
        buttonIndicators[2] = (ImageView) findViewById(R.id.combinedButton2_indicator);
        buttonIndicators[3] = (ImageView) findViewById(R.id.combinedButton3_indicator);
        buttonIndicators[4] = (ImageView) findViewById(R.id.combinedButton4_indicator);
        buttonIndicators[5] = (ImageView) findViewById(R.id.combinedButton5_indicator);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        shouldRedraw = hasFocus && (viewButtons[0].getWidth() == 0);
    }

    private void redrawButtons() {
        Log.d("CA", "redrawButtons started");
        int lvi = getLastVisibleIndex();
        int dimension = getButtonDimension(getNumColumns(lvi), getNumRows(lvi));
        for (int i=0; i<=getLastVisibleIndex(); i++) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewButtons[i].getLayoutParams();
            params.width = dimension;
            params.height = dimension;
            params.rightMargin = margin;
            params.bottomMargin=margin;
            viewButtons[i].setLayoutParams(params);
        }
        Log.d("CA", "redrawButtons with dimension: " + dimension);
        Log.d("CA", "redrawButtons ended");
    }

/***** END: Button Management */

/*****
     * Button actions
     */
    public void buttonSelected(View view) {
        final ImageButton playingButton = (ImageButton) view;
        final String buttonSoundLocation = prefs.getString((res.getResourceName(view.getId())+AUDIO_TYPE), null);
        if (buttonSoundLocation != null) {
            Uri buttonSoundUri = Uri.parse(buttonSoundLocation);
            playAudioForButton(buttonSoundUri, playingButton);
        }
    }

    public void findImage(View v) {
        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
        setAsCurrentButton((ImageButton) v, IMAGE_TYPE);
        imagePickerIntent.setType("image/*");
        startActivityForResult(imagePickerIntent, SELECT_PHOTO);
    }

    private void findSound(View view) {
        Intent soundPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        soundPickerIntent.setType("audio/*");
        setAsCurrentButton((ImageButton) view, AUDIO_TYPE);
        startActivityForResult(soundPickerIntent, SELECT_AUDIO);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        switch (requestCode){
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    final Uri imageUri = returnedIntent.getData();
                    int maxButtonSide = getButtonDimension(2,1);
                    Bitmap selectedImage = ImageUtil.getScaledBitmap(imageUri, maxButtonSide, this);
                    if (selectedImage != null) {
                        File internalFile = saveBitmapToInternalStorage(CURRENT_BUTTON_NAME, selectedImage);
                        if (internalFile != null) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(CURRENT_BUTTON_ABSOLUTE_NAME, internalFile.toString());
                            editor.commit();

                            CURRENT_BUTTON.setImageBitmap(selectedImage);
                            CURRENT_BUTTON.setBackgroundColor(Color.TRANSPARENT);
                            CURRENT_BUTTON.setOnClickListener(buttonSelectedClickListener);
                            addButtonAttribute(CURRENT_BUTTON, SELECT_PHOTO);
                            registerForContextMenu(CURRENT_BUTTON);
                        }
                    }
                }
                break;
            case SELECT_AUDIO:
                if (resultCode == RESULT_OK) {
                    final Uri audioUri = returnedIntent.getData();
                    File buttonAudioFile = saveAudioToInternalStorage(CURRENT_BUTTON_NAME, audioUri);
                    if (buttonAudioFile.exists()) {
                        playAudioForButton(audioUri, CURRENT_BUTTON);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(CURRENT_BUTTON_ABSOLUTE_NAME, buttonAudioFile.toString());
                        editor.commit();

                        CURRENT_BUTTON.setOnClickListener(buttonSelectedClickListener);
                        addButtonAttribute(CURRENT_BUTTON, SELECT_AUDIO);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void exchangeButtonContent(int index1, int index2) {
        if (index1 >= 0
                && index1 < viewButtons.length
                && index2 >= 0
                && index2 < viewButtons.length) {
            int tmpVis = viewButtons[index1].getVisibility();
            int tmpTag = (int)viewButtons[index1].getTag();
            Bitmap tmpImg = ((BitmapDrawable)viewButtons[index1].getDrawable()).getBitmap();
            viewButtons[index1].setOnClickListener(determineClickListener((int)viewButtons[index2].getTag()));
            viewButtons[index1].setVisibility(viewButtons[index2].getVisibility());
            viewButtons[index1].setTag(viewButtons[index2].getTag());
            viewButtons[index1].setImageBitmap(((BitmapDrawable) viewButtons[index2].getDrawable()).getBitmap());
            viewButtons[index2].setOnClickListener(determineClickListener(tmpTag));
            viewButtons[index2].setVisibility(tmpVis);
            viewButtons[index2].setTag(tmpTag);
            viewButtons[index2].setImageBitmap(tmpImg);
            exchangeFileNames(viewButtons[index1], viewButtons[index2]);
        }
    }

    private void exchangeFileNames(ImageButton button1, ImageButton button2) {
        int button1Id = button1.getId();
        int button2Id = button2.getId();
        String absName1 = res.getResourceName(button1Id);
        String absName2 = res.getResourceName(button2Id);

        // get existing keys
        String imageKey1 = absName1 + IMAGE_TYPE;
        String imageKey2 = absName2 + IMAGE_TYPE;
        String audioKey1 = absName1 + AUDIO_TYPE;
        String audioKey2 = absName2 + AUDIO_TYPE;

        // get existing values
        String imageValue1 = prefs.getString(imageKey1, null);
        String imageValue2 = prefs.getString(imageKey2, null);
        String audioValue1 = prefs.getString(audioKey1, null);
        String audioValue2 = prefs.getString(audioKey2, null);

        // remove all keys from SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(imageKey1);
        editor.remove(imageKey2);
        editor.remove(audioKey1);
        editor.remove(audioKey2);

        // switch key value pairs if they existed
        if (imageValue1 != null) {
            editor.putString(imageKey2, imageValue1);
        }
        if (imageValue2 != null) {
            editor.putString(imageKey1, imageValue2);
        }
        if (audioValue1 != null) {
            editor.putString(audioKey2, audioValue1);
        }
        if (audioValue2 != null) {
            editor.putString(audioKey1, audioValue2);
        }

        editor.commit();
    }

    private void replaceFileNames(ImageButton toButton, ImageButton fromButton) {
        SharedPreferences.Editor editor = prefs.edit();
        String toAudioKey = res.getResourceName(toButton.getId()) + AUDIO_TYPE;
        String toImageKey = res.getResourceName(toButton.getId()) + IMAGE_TYPE;
        String fromAudioKey = res.getResourceName(fromButton.getId()) + AUDIO_TYPE;
        String fromImageKey = res.getResourceName(fromButton.getId()) + IMAGE_TYPE;
        String fromAudioValue = prefs.getString(fromAudioKey, null);
        String fromImageValue = prefs.getString(fromImageKey, null);
        if (fromAudioValue != null) {
            editor.putString(toAudioKey, fromAudioValue);
        }
        else {
            editor.remove(toAudioKey);
        }
        if (fromImageValue != null) {
            editor.putString(toImageKey, fromImageValue);
        }
        else {
            editor.remove(toImageKey);
        }
        editor.remove(fromAudioKey);
        editor.remove(fromImageKey);
        editor.commit();

    }

    private void replaceButtonContent(int toIndex, int fromIndex) {
        if (fromIndex < viewButtons.length) {
            int fromIndexTag = (int)viewButtons[fromIndex].getTag();
            viewButtons[toIndex].setOnClickListener(determineClickListener(fromIndexTag));
            viewButtons[toIndex].setTag(fromIndexTag);
            viewButtons[toIndex].setImageBitmap(((BitmapDrawable) viewButtons[fromIndex].getDrawable()).getBitmap());
            viewButtons[toIndex].setVisibility(viewButtons[fromIndex].getVisibility());
            replaceFileNames(viewButtons[toIndex], viewButtons[fromIndex]);
        }
        else if (toIndex >= 0 && toIndex < viewButtons.length) {
            disableButton(viewButtons[toIndex]);
        }
        setButtonEnablePlusUI((Button) findViewById(R.id.addButton), !buttonSetFull());
    }

    private void enableButton(ImageButton button) {
        button.setTag(DEFAULT);
        button.setVisibility(View.VISIBLE);
    }

    private void disableButton(ImageButton button) {
        button.setTag(DEFAULT);
        button.setVisibility(View.GONE);
    }

    private boolean buttonSetFull() {
        return viewButtons[viewButtons.length-1].getVisibility() != View.GONE;
    }

    private void setButtonEnablePlusUI(Button button, boolean enabled) {
        button.setEnabled(enabled);
        if (!enabled) {
            button.setTextColor(Color.GRAY);
        }
        else {
            button.setTextColor(Color.WHITE);
        }
    }

    private void recordingIndicator(ImageButton button, boolean on) {
        ImageView indicator = buttonIndicators[getButtonIndex(button.getId())];
        if (on) {
            button.setAlpha(0.5f);
            indicator.setBackgroundResource(R.drawable.rec_animation);
            AnimationDrawable background = (AnimationDrawable) indicator.getBackground();
            background.start();
        }
        else {
            button.setAlpha(1f);
            indicator.setBackgroundResource(0);
        }
    }

    private void playingIndicator(ImageButton button, boolean on) {
        String buttonName = res.getResourceEntryName(button.getId());
        int id = res.getIdentifier(buttonName + "_indicator", "id", this.getPackageName());
        ImageView indicator = (ImageView)findViewById(id);
        if (on) {
            indicator.setBackgroundResource(R.drawable.play_animation);
            AnimationDrawable background = (AnimationDrawable) indicator.getBackground();
            background.start();
        }
        else {
            indicator.setBackgroundResource(0);
        }
    }

/***** END: Button actions */

/*****
     * Context Menu handling
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (preparedForAnotherEvent()) {
            switch ((int) v.getTag()) {
                case DEFAULT:
                    menu.add(1, R.id.image_action, 1, R.string.menu_title_add_image);
                    menu.add(1, R.id.sound_action, 2, R.string.menu_title_add_sound);
                    menu.add(1, R.id.record_action, 3, R.string.menu_title_record_sound);
                    menu.add(1, R.id.up_vote_action, 4, R.string.menu_title_up_vote);
                    menu.add(1, R.id.down_vote_action, 5, R.string.menu_title_down_vote);
                    menu.add(1, R.id.remove_button_action, 6, R.string.menu_title_remove_button);
                    menu.getItem(1).setVisible(false);
                    menu.getItem(2).setVisible(false);
                    break;
                case DEFAULT_IMAGE:
                    menu.add(1, R.id.image_action, 1, R.string.menu_title_replace_image);
                    menu.add(1, R.id.sound_action, 2, R.string.menu_title_add_sound);
                    menu.add(1, R.id.record_action, 3, R.string.menu_title_record_sound);
                    menu.add(1, R.id.up_vote_action, 4, R.string.menu_title_up_vote);
                    menu.add(1, R.id.down_vote_action, 5, R.string.menu_title_down_vote);
                    menu.add(1, R.id.remove_button_action, 6, R.string.menu_title_remove_button);
                    break;
                case DEFAULT_AUDIO:
                    menu.add(1, R.id.image_action, 1, R.string.menu_title_add_image);
                    menu.add(1, R.id.sound_action, 2, R.string.menu_title_replace_sound);
                    menu.add(1, R.id.record_action, 3, R.string.menu_title_replace_recording);
                    menu.add(1, R.id.up_vote_action, 4, R.string.menu_title_up_vote);
                    menu.add(1, R.id.down_vote_action, 5, R.string.menu_title_down_vote);
                    menu.add(1, R.id.remove_button_action, 6, R.string.menu_title_remove_button);
                    break;
                case DEFAULT_IMAGE_AUDIO:
                    menu.add(1, R.id.image_action, 1, R.string.menu_title_replace_image);
                    menu.add(1, R.id.sound_action, 2, R.string.menu_title_replace_sound);
                    menu.add(1, R.id.record_action, 3, R.string.menu_title_replace_recording);
                    menu.add(1, R.id.up_vote_action, 4, R.string.menu_title_up_vote);
                    menu.add(1, R.id.down_vote_action, 5, R.string.menu_title_down_vote);
                    menu.add(1, R.id.remove_button_action, 6, R.string.menu_title_remove_button);
                    break;
                default:
                    break;
            }
            CURRENT_BUTTON_ID = v.getId();

            int viewButtonIndex = getButtonIndex(CURRENT_BUTTON_ID);
            menu.getItem(4).setEnabled(viewButtonIndex < getLastVisibleIndex());    //not upper bounds
            menu.getItem(3).setEnabled(viewButtonIndex > 0);                        //not lower bounds
            menu.getItem(5).setEnabled(getLastVisibleIndex() > 1);                  //not lower bounds
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        ImageButton activeButton = (ImageButton) findViewById(CURRENT_BUTTON_ID);
        switch (item.getItemId()) {
            case R.id.image_action:
                manageImageState(activeButton);
                return true;
            case R.id.sound_action:
                manageSoundState(activeButton);
                return true;
            case R.id.record_action:
                manageRecording(activeButton);
                return true;
            case R.id.remove_button_action:
                manageButtonRemoval(activeButton);
                return true;
            case R.id.up_vote_action:
                upVote(activeButton);
                return true;
            case R.id.down_vote_action:
                downVote(activeButton);
                return true;
            default:
                return true;
        }
    }

/***** END: Context Menu handling */

/*****
     * Menu Actions
     */
    private void downVote(ImageButton selectedButton) {
        for (int i=0; i<getLastVisibleIndex(); i++) {
            if (selectedButton == viewButtons[i]) {
                exchangeButtonContent(i, i + 1);
                break;
            }
        }
    }

    private void upVote(ImageButton selectedButton) {
        for (int i=0; i<=getLastVisibleIndex(); i++) {
            if (selectedButton == viewButtons[i]) {
                exchangeButtonContent(i, i - 1);
                break;
            }
        }
    }

    private void manageSoundState(ImageButton button) {
        findSound(button);
    }

    private void manageImageState(ImageButton button) {
        findImage(button);
    }

    private void manageButtonRemoval(ImageButton button) {
        boolean matched = false;
        int buttonID = button.getId();
        for (int i=0; i<viewButtons.length; i++) {
            if (viewButtons[i].getId() == buttonID) {
                matched = true;
                deleteFilesAssociatedWithButton(buttonID);
                replaceButtonContent(i, i+1);
            }
            else if (matched && viewButtons[i].getVisibility() != View.GONE) {
                replaceButtonContent(i, i+1);
            }
        }
        redrawButtons();
    }

    public void addButton(View view) {
        Log.d("CA", "addButton started");
        if (preparedForAnotherEvent()) {
            for (int i = 0; i < viewButtons.length; i++) {
                if (viewButtons[i].getVisibility() == View.GONE) {
                    enableButton(viewButtons[i]);
                    setButtonEnablePlusUI((Button) findViewById(R.id.addButton), !buttonSetFull());
                    redrawButtons();
                    break;
                }
            }
        }
        Log.d("CA", "addButton end");
    }

    private void manageRecording(ImageButton activeButton) {
        setAsCurrentButton(activeButton, AUDIO_TYPE);
        recordingIndicator(activeButton, true);

        CURRENT_BUTTON_OUTPUT_FILE = this.getFilesDir() + "/" + CURRENT_BUTTON_NAME + getDateString();
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(CURRENT_BUTTON_OUTPUT_FILE);
        myAudioRecorder.setMaxDuration(10000);

        try {
            myAudioRecorder.prepare();
            CURRENT_BUTTON.setOnClickListener(recordingCompleteListener);
            myAudioRecorder.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

/***** END: Menu Actions /*

/*****
     * Image Utils
     */
    private Bitmap getBitmap(Uri imageUri) {
        Bitmap ret = null;
        try {
            File f = new File(imageUri.getPath());
            if (f.exists()) {
                Log.d("decodeUri", f.getAbsolutePath());
                ret = BitmapFactory.decodeFile(f.getAbsolutePath());
            }
            else {
                Log.d("CA:decodeUri", "File not found: " + imageUri.getPath());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private File saveBitmapToInternalStorage(String outputFilename, Bitmap in) {
        FileOutputStream out;
        File buttonResourceFile = null;
        outputFilename += getDateString();
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
/***** END: Image Utils


/*****
     * Audio Utils
     */
    private File saveAudioToInternalStorage(String outputFilename, Uri audioUri) {
        FileOutputStream out;
        File buttonAudioFile = null;
        outputFilename += getDateString();
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

    private void playAudioForButton(Uri audioUri, ImageButton activeButton) {
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
            playingIndicator(activeButton, true);
            setAsCurrentButton(activeButton, AUDIO_TYPE);
            aMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/***** END: Audio Utils */

/*****
     * Button Utils
     */
    private int getButtonDimension(int numColumns, int numRows) {
    int width = container.getWidth();
    int height = container.getHeight();
    int vFreeSpace = getVerticalFreeSpace(height, numRows);
    int hFreeSpace = getHorizontalFreeSpace(width, numColumns);
    return Math.min(vFreeSpace / numRows, hFreeSpace / numColumns);
}

    public int getLastVisibleIndex() {
        for (int i = viewButtons.length - 1; i >= 0; i--) {
            if (viewButtons[i].getVisibility() != View.GONE) {
                return i;      //previous index which would have passed the visibility test
            }
        }
        return 0;
    }

    private int getHorizontalFreeSpace(int containerWidth, int numColumns) {
        Log.d("CA", "containerWidth: " + containerWidth);
        Log.d("CA", "numColumns: " + numColumns);
        int cPadding = (numColumns + 1) * margin;
        Log.d("CA", "cPadding: " + cPadding);
        return containerWidth - cPadding;
    }

    private int getVerticalFreeSpace(int containerHeight, int numRows) {
        Log.d("CA", "containerHeight: " + containerHeight);
        Log.d("CA", "numRows: " + numRows);
        int rPadding = (numRows + 1) * margin;
        Log.d("CA", "rPadding: " + rPadding);
        return containerHeight - rPadding;
    }

    private int getNumRows(int currentIndex) {
        if (currentIndex > 2) {
            return 2;
        }
        else {
            return 1;
        }
    }

    private int getNumColumns(int currentIndex) {
        switch (currentIndex) {
            case 0:
                return 1;
            case 1:
                return 2;
            default:
                return 3;
        }
    }

    public void setAsCurrentButton(ImageButton currentButton, String mediaType) {
        CURRENT_BUTTON = currentButton;
        CURRENT_BUTTON_ID = CURRENT_BUTTON.getId();
        CURRENT_BUTTON_ABSOLUTE_NAME = res.getResourceName(CURRENT_BUTTON_ID) + mediaType;
        CURRENT_BUTTON_NAME = "combinedButton" + mediaType;
    }

    private int getButtonIndex(int selected_button_id) {
        int index = 0;
        for (int i=0; i<viewButtons.length; i++) {
            if (viewButtons[i].getId() == selected_button_id) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void addButtonAttribute(ImageButton button, int button_attribute) {
        if (button_attribute == SELECT_PHOTO && !hasImageAttribute(button) ||
                button_attribute == SELECT_AUDIO && !hasAudioAttribute(button)) {
            button.setTag(((int) button.getTag()) + button_attribute);
        }
    }

    private boolean hasAudioAttribute(ImageButton button) {
        return (int)button.getTag() >= DEFAULT_AUDIO;
    }

    private boolean hasImageAttribute(ImageButton button) {
        int tag = (int)button.getTag();
        return tag == DEFAULT_IMAGE_AUDIO || tag == DEFAULT_IMAGE;
    }

/***** END: Button Utils */

    /*****
     * Listeners
     */
    ImageButton.OnClickListener findImageClickListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (preparedForAnotherEvent()) {
                findImage(v);
            }
        }
    };

    ImageButton.OnClickListener buttonSelectedClickListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (preparedForAnotherEvent()) {
                buttonSelected(v);
            }
        }
    };

    ImageButton.OnClickListener recordingCompleteListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myAudioRecorder != null) {
                myAudioRecorder.stop();
                myAudioRecorder.release();
                myAudioRecorder = null;
                recordingIndicator((ImageButton) v, false);
                File newRecording = new File(CURRENT_BUTTON_OUTPUT_FILE);
                if (newRecording != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(CURRENT_BUTTON_ABSOLUTE_NAME, newRecording.toString());
                    editor.commit();
                    addButtonAttribute(CURRENT_BUTTON, SELECT_AUDIO);
                    CURRENT_BUTTON.setOnClickListener(buttonSelectedClickListener);
                }
            }
        }
    };

    private ImageButton.OnClickListener determineClickListener(int tmpTag) {
        if (tmpTag == DEFAULT) {
            return findImageClickListener;
        }
        else {
            return buttonSelectedClickListener;
        }
    }
    MediaPlayer.OnPreparedListener aPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            aMediaPlayer.start();
        }
    };

    MediaPlayer.OnCompletionListener aCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer arg0) {
            playingIndicator(CURRENT_BUTTON, false);
            if (aMediaPlayer != null) {
                CURRENT_BUTTON = null;
                aMediaPlayer.release();
                aMediaPlayer = null;
            }
        }
    };

    Button.OnTouchListener aButtonTouchEffect = new Button.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ((ImageView)v).setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
//                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
//                    params.width = params.width - 10;
//                    params.height = params.width - 10;
//                    params.rightMargin = params.rightMargin + 5;
//                    params.bottomMargin = params.bottomMargin + 5;
//                    params.leftMargin = params.leftMargin + 5;
//                    params.topMargin = params.topMargin + 5;
//                    v.setLayoutParams(params);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    ((ImageView)v).clearColorFilter();
//                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
//                    params.width = params.width + 10;
//                    params.height = params.width + 10;
//                    params.rightMargin = params.rightMargin - 5;
//                    params.bottomMargin = params.bottomMargin - 5;
//                    params.leftMargin = params.leftMargin - 5;
//                    params.topMargin = params.topMargin - 5;
//                    v.setLayoutParams(params);
                    v.invalidate();
                    break;
                }
            }
            return false;
        }
    };

    public boolean preparedForAnotherEvent() {
        for (int i=0; i<buttonIndicators.length; i++) {
            viewButtons[i].setAlpha(1f);
            viewButtons[i].setOnClickListener(determineClickListener((int)viewButtons[i].getTag()));
            buttonIndicators[i].setBackgroundResource(0);
        }
        if (myAudioRecorder != null) {
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder = null;
        }
        if (aMediaPlayer != null) {
            aMediaPlayer.release();
            aMediaPlayer = null;
        }
        return true;
    }

/***** END: Listeners */

    private String getDateString() {
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return dateFormatter.format(date);
    }


    private void deleteFilesAssociatedWithButton(int buttonID) {
        String imageKey = res.getResourceName(buttonID) + IMAGE_TYPE;
        String audioKey = res.getResourceName(buttonID) + AUDIO_TYPE;
        String imageFileName = prefs.getString(imageKey, null);
        String audioFileName = prefs.getString(audioKey, null);
        if (imageFileName != null) {
            try {
                File imageFile = new File(imageFileName);
                imageFile.delete();
            }
            catch (Exception e) {
                // didn't exist keep going
            }
        }
        if (audioFileName != null) {
            try {
                File audioFile = new File(audioFileName);
                audioFile.delete();
            }
            catch (Exception e) {
                // didn't exist keep going
            }
        }
    }
}
