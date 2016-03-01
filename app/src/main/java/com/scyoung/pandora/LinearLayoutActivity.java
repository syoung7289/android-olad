package com.scyoung.pandora;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LinearLayoutActivity extends AppCompatActivity {

    LinearLayout buttonRowContainer;
    LinearLayout mainContainer;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        Point size = new Point();
//        getWindowManager().getDefaultDisplay().getSize(size);
//        Toast.makeText(this, "Window width" + size.x, Toast.LENGTH_LONG).show();
//        Toast.makeText(this, "Window height" + size.y, Toast.LENGTH_LONG).show();

        buttonRowContainer = (LinearLayout) findViewById(R.id.linear_row_container);
        mainContainer = (LinearLayout) findViewById(R.id.main_layout);
    }
    @Override
    public void onWindowFocusChanged(boolean b) {
        int width = buttonRowContainer.getWidth();
        int height = buttonRowContainer.getWidth();
        Toast.makeText(this, "RowContainer width: " + width, Toast.LENGTH_LONG).show();
        Toast.makeText(this, "RowContainer height: " + height, Toast.LENGTH_LONG).show();
        width = mainContainer.getWidth();
        height = mainContainer.getWidth();
        Toast.makeText(this, "mainContainer width: " + width, Toast.LENGTH_LONG).show();
        Toast.makeText(this, "mainContainer height: " + height, Toast.LENGTH_LONG).show();
        super.onWindowFocusChanged(b);
    }
}

