package com.scyoung.pandora;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class ShowDynamicMenu extends AppCompatActivity {

    Button[] viewButtons = new Button[6];
    private enum ButtonState {D, DI, DS, DIS};
    private int SELECTED_BUTTON_ID;
    RelativeLayout container;
    int margin = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_dynamic_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        container = (RelativeLayout) findViewById(R.id.dynamic_menu_container);
        buildButtons();
    }

    public void buildButtons() {
        viewButtons[0] = (Button) findViewById(R.id.dynamicMenuButton0);
        viewButtons[0].setText(R.string.button_text_image);
        viewButtons[0].setTag(ButtonState.DI);
        viewButtons[1] = (Button) findViewById(R.id.dynamicMenuButton1);
        viewButtons[1].setText(R.string.button_text_image_sound);
        viewButtons[1].setTag(ButtonState.DIS);
        viewButtons[2] = (Button) findViewById(R.id.dynamicMenuButton2);
        viewButtons[2].setText(R.string.button_text_empty);
        viewButtons[2].setTag(ButtonState.D);
        viewButtons[3] = (Button) findViewById(R.id.dynamicMenuButton3);
        viewButtons[3].setVisibility(View.GONE);
        viewButtons[3].setTag(ButtonState.D);
        viewButtons[4] = (Button) findViewById(R.id.dynamicMenuButton4);
        viewButtons[4].setVisibility(View.GONE);
        viewButtons[4].setTag(ButtonState.D);
        viewButtons[5] = (Button) findViewById(R.id.dynamicMenuButton5);
        viewButtons[5].setVisibility(View.GONE);
        viewButtons[5].setTag(ButtonState.D);

        for (int i=0; i<viewButtons.length; i++) {
            registerForContextMenu(viewButtons[i]);
        }
    }

    private void redrawButtons() {
        int dimension = getButtonDimension();
        for (int i=0; i<=getLastVisibleIndex(); i++) {
            Log.d("SDM", "button index: " + i);
            Log.d("SDM", "dimension: " + dimension);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewButtons[i].getLayoutParams();
            params.width = dimension;
            params.height = dimension;
            params.rightMargin = margin;
            viewButtons[i].setLayoutParams(params);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && viewButtons[0].getWidth() == 0) {
            Log.d("SDM", "hasFocus: " + hasFocus);
            redrawButtons();
        }
    }


    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        switch ((ButtonState) v.getTag()) {
            case D:
                menu.add(1, R.id.image_action, 1, R.string.menu_title_add_image);
                menu.add(1, R.id.sound_action, 2, R.string.menu_title_add_sound);
                menu.add(1, R.id.up_vote_action, 3, R.string.menu_title_up_vote);
                menu.add(1, R.id.down_vote_action, 4, R.string.menu_title_down_vote);
                menu.add(1, R.id.remove_button_action, 5, R.string.menu_title_remove_button);
                break;
            case DI:
                menu.add(1, R.id.image_action, 1, R.string.menu_title_replace_image);
                menu.add(1, R.id.sound_action, 2, R.string.menu_title_add_sound);
                menu.add(1, R.id.up_vote_action, 3, R.string.menu_title_up_vote);
                menu.add(1, R.id.down_vote_action, 4, R.string.menu_title_down_vote);
                menu.add(1, R.id.remove_button_action, 5, R.string.menu_title_remove_button);
                break;
            case DS:
                menu.add(1, R.id.image_action, 1, R.string.menu_title_add_image);
                menu.add(1, R.id.sound_action, 2, R.string.menu_title_replace_sound);
                menu.add(1, R.id.up_vote_action, 3, R.string.menu_title_up_vote);
                menu.add(1, R.id.down_vote_action, 4, R.string.menu_title_down_vote);
                menu.add(1, R.id.remove_button_action, 5, R.string.menu_title_remove_button);
                break;
            case DIS:
                menu.add(1, R.id.image_action, 1, R.string.menu_title_replace_image);
                menu.add(1, R.id.sound_action, 2, R.string.menu_title_replace_sound);
                menu.add(1, R.id.up_vote_action, 3, R.string.menu_title_up_vote);
                menu.add(1, R.id.down_vote_action, 4, R.string.menu_title_down_vote);
                menu.add(1, R.id.remove_button_action, 5, R.string.menu_title_remove_button);
                break;
            default:
                break;
        }
        SELECTED_BUTTON_ID = v.getId();

        int viewButtonIndex = getButtonIndex(SELECTED_BUTTON_ID);
        menu.getItem(3).setEnabled(viewButtonIndex < getLastVisibleIndex());    //not upper bounds
        menu.getItem(2).setEnabled(viewButtonIndex > 0);                        //not lower bounds
        menu.getItem(4).setEnabled(getLastVisibleIndex() > 0);                  //not lower bounds
    }


    public boolean onContextItemSelected(MenuItem item) {
        Button activeButton = (Button) findViewById(SELECTED_BUTTON_ID);
        switch (item.getItemId()) {
            case R.id.image_action:
                manageImageState(activeButton);
                return true;
            case R.id.sound_action:
                manageSoundState(activeButton);
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

    private void downVote(Button selectedButton) {
        for (int i=0; i<getLastVisibleIndex(); i++) {
            if (selectedButton == viewButtons[i]) {
                exchangeButtonContent(i, i + 1);
                break;
            }
        }
    }

    private void upVote(Button selectedButton) {
        for (int i=0; i<=getLastVisibleIndex(); i++) {
            if (selectedButton == viewButtons[i]) {
                exchangeButtonContent(i, i - 1);
                break;
            }
        }
    }

    public void addButton(View view) {
        for (int i=0; i<viewButtons.length; i++) {
            if (viewButtons[i].getVisibility() == View.GONE) {
                enableButton(viewButtons[i]);
                setButtonEnablePlusUI((Button) findViewById(R.id.addButton), !buttonSetFull());
                redrawButtons();
                break;
            }
        }
    }

    private boolean buttonSetFull() {
        return viewButtons[viewButtons.length-1].getVisibility() != View.GONE;
    }

    private void manageSoundState(Button button) {
        switch ((ButtonState)button.getTag()) {
            case D:
                button.setTag(ButtonState.DS);
                button.setText(R.string.button_text_sound);
                break;
            case DI:
                button.setTag(ButtonState.DIS);
                button.setText(R.string.button_text_image_sound);
                break;
            default:
                break;
        }
    }

    private void manageImageState(Button button) {
        switch ((ButtonState)button.getTag()) {
            case D:
                button.setTag(ButtonState.DI);
                button.setText(R.string.button_text_image);
                break;
            case DS:
                button.setTag(ButtonState.DIS);
                button.setText(R.string.button_text_image_sound);
                break;
            default:
                break;
        }
    }

    private void manageButtonRemoval(Button button) {
        boolean matched = false;
        for (int i=0; i<viewButtons.length; i++) {
            if (viewButtons[i].getId() == button.getId()) {
                matched = true;
                replaceButtonContent(i, i+1);
            }
            else if (matched && viewButtons[i].getVisibility() != View.GONE) {
                replaceButtonContent(i, i+1);
            }
        }
        redrawButtons();
    }

    private void replaceButtonContent(int fromIndex, int toIndex) {
        if (toIndex < viewButtons.length) {
            viewButtons[fromIndex].setTag(viewButtons[toIndex].getTag());
            viewButtons[fromIndex].setText(viewButtons[toIndex].getText());
            viewButtons[fromIndex].setVisibility(viewButtons[toIndex].getVisibility());
        }
        else if (fromIndex >= 0 && fromIndex < viewButtons.length) {
            disableButton(viewButtons[fromIndex]);
        }
        setButtonEnablePlusUI((Button) findViewById(R.id.addButton), !buttonSetFull());
    }

    private void exchangeButtonContent(int index1, int index2) {
        if (index1 >= 0
                && index1 < viewButtons.length
                && index2 >= 0
                && index2 < viewButtons.length) {
            int tmpVis = viewButtons[index1].getVisibility();
            Object tmpTag = viewButtons[index1].getTag();
            CharSequence tmpText = viewButtons[index1].getText();
            viewButtons[index1].setVisibility(viewButtons[index2].getVisibility());
            viewButtons[index1].setTag(viewButtons[index2].getTag());
            viewButtons[index1].setText(viewButtons[index2].getText());
            viewButtons[index2].setVisibility(tmpVis);
            viewButtons[index2].setTag(tmpTag);
            viewButtons[index2].setText(tmpText);
        }
    }

    private void disableButton(Button button) {
        button.setText(R.string.button_text_empty);
        button.setTag(ButtonState.D);
        button.setVisibility(View.GONE);
    }

    private void enableButton(Button button) {
        button.setText(R.string.button_text_empty);
        button.setTag(ButtonState.D);
        button.setVisibility(View.VISIBLE);
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

    public int getLastVisibleIndex() {
        for (int i = 0; i < viewButtons.length; i++) {
            if (viewButtons[i].getVisibility() == View.GONE) {
                return i-1;      //previous index which would have passed the visibility test
            }
        }
        return viewButtons.length-1;
    }

    private int getButtonDimension() {
        int width = container.getWidth();
        int height = container.getHeight();
        int lvi = getLastVisibleIndex();
        int numColumns = getNumColumns(lvi);
        int numRows = getNumRows(lvi);
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

}
