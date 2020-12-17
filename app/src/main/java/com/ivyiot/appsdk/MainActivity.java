package com.ivyiot.appsdk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "MainActivity";

    private ListView lv_device_search;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv_device_search = findViewById(R.id.lv_device_search);
        findViewById(R.id.btn_refresh).setOnClickListener(this);
        findViewById(R.id.btn_audio_wave_add).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_refresh:

                break;
            case R.id.btn_audio_wave_add:
//                Intent mIntent = new Intent();
//                mIntent.setClass(this, SoundWaveAddActivity.class);
//                mIntent.putExtra("uid", "2UM3CT9DSJQ46UQ5ZZZZ9Y5I");
//                mIntent.putExtra("wifi_ssid", "TP-LINK_MZP");
//                mIntent.putExtra("wifi_password", "123456app");
//                startActivity(mIntent);

                Intent mIntent = new Intent();
                mIntent.setClass(this, CloudPlaybackActivity.class);
                startActivity(mIntent);
                break;
        }
    }





}
