package com.bihucj.mcandroid.ui;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bihucj.mcandroid.R;
import com.bihucj.mcandroid.view.Image3DSwitchView;

public class MainActivity extends AppCompatActivity {


    private Image3DSwitchView image3DSwitchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image3DSwitchView = (Image3DSwitchView) findViewById(R.id.image_switch_view);

        //切换的监听
        image3DSwitchView.setOnImageSwitchListener(new Image3DSwitchView.OnImageSwitchListener() {
            @Override
            public void onImageSwitch(int currentImage) {
                Toast.makeText(MainActivity.this, "位置:" + currentImage, Toast.LENGTH_SHORT).show();
            }
        });
        //切换显示位置
        findViewById(R.id.bt_change_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image3DSwitchView.setCurrentImage(3);
            }
        });
    }

}