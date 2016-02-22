package com.scyoung.pandora;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ShowDynamicMenu extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    Button[] viewButtons = new Button[6];
    private enum ButtonState {D, DI, DS, DIS};
    private int SELECTED_BUTTON_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_dynamic_menu);
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
            setOnClickListener(viewButtons[i], i);
        }
    }

    private void setOnClickListener(final Button button, final int viewButtonIndex) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(ShowDynamicMenu.this, button);
                popup.setOnMenuItemClickListener(ShowDynamicMenu.this);
                switch ((ButtonState)button.getTag()) {
                    case D:
                        popup.getMenu().add(1, R.id.image_action, 1, R.string.menu_title_add_image);
                        popup.getMenu().add(1, R.id.sound_action, 2, R.string.menu_title_add_sound);
                        popup.getMenu().add(1, R.id.remove_button_action, 3, R.string.menu_title_remove_button);
                        popup.getMenu().add(1, R.id.up_vote_action, 4, R.string.menu_title_up_vote);
                        popup.getMenu().add(1, R.id.down_vote_action, 4, R.string.menu_title_down_vote);
                        break;
                    case DI:
                        popup.getMenu().add(1, R.id.image_action, 1, R.string.menu_title_replace_image);
                        popup.getMenu().add(1, R.id.sound_action, 2, R.string.menu_title_add_sound);
                        popup.getMenu().add(1, R.id.remove_button_action, 3, R.string.menu_title_remove_button);
                        popup.getMenu().add(1, R.id.up_vote_action, 4, R.string.menu_title_up_vote);
                        popup.getMenu().add(1, R.id.down_vote_action, 4, R.string.menu_title_down_vote);
                        break;
                    case DS:
                        popup.getMenu().add(1, R.id.image_action, 1, R.string.menu_title_add_image);
                        popup.getMenu().add(1, R.id.sound_action, 2, R.string.menu_title_replace_sound);
                        popup.getMenu().add(1, R.id.remove_button_action, 3, R.string.menu_title_remove_button);
                        popup.getMenu().add(1, R.id.up_vote_action, 4, R.string.menu_title_up_vote);
                        popup.getMenu().add(1, R.id.down_vote_action, 4, R.string.menu_title_down_vote);
                        break;
                    case DIS:
                        popup.getMenu().add(1, R.id.image_action, 1, R.string.menu_title_replace_image);
                        popup.getMenu().add(1, R.id.sound_action, 2, R.string.menu_title_replace_sound);
                        popup.getMenu().add(1, R.id.remove_button_action, 3, R.string.menu_title_remove_button);
                        popup.getMenu().add(1, R.id.up_vote_action, 4, R.string.menu_title_up_vote);
                        popup.getMenu().add(1, R.id.down_vote_action, 4, R.string.menu_title_down_vote);
                        break;
                    default:
                        break;
                }
                SELECTED_BUTTON_ID = button.getId();
                popup.getMenu().getItem(4).setEnabled(viewButtonIndex < getLastVisibleIndex());    //not upper bounds
                popup.getMenu().getItem(3).setEnabled(viewButtonIndex > 0);                         //not lower bounds
                popup.show();
            }
        });
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
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
                upVote(activeButton, SELECTED_BUTTON_ID);
                return true;
            case R.id.down_vote_action:
                downVote(SELECTED_BUTTON_ID);
                return true;
            default:
                return true;
        }
    }

    private void downVote(int selected_button_id) {
        for (int i=selected_button_id; i<viewButtons.length; i++) {
            exchangeButtonContent(i, i+1);
        }
    }

    private void upVote(Button activeButton, int selected_button_id) {

    }

    public void addButton(View view) {
        for (int i=0; i<viewButtons.length; i++) {
            if (viewButtons[i].getVisibility() == View.GONE) {
                enableButton(viewButtons[i]);
                setButtonEnablePlusUI((Button) findViewById(R.id.addButton), !buttonSetFull());
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
        setButtonEnablePlusUI((Button)findViewById(R.id.addButton), !buttonSetFull());
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
            button.setBackgroundColor(Color.GRAY);
        }
        else {
            button.setBackgroundColor(Color.parseColor("#FF060078"));
        }
    }

    public int getLastVisibleIndex() {
        for (int i = 0; i < viewButtons.length; i++) {
            if (viewButtons[i].getVisibility() == View.GONE) {
                return i-1;      //previous index which would have passed the visibility test
            }
        }
        return 0;
    }

}
