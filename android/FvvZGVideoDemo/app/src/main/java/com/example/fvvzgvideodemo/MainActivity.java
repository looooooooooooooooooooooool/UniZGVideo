package com.example.fvvzgvideodemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import fvv.sdk.FvvZGVideoSDK.FvvZGVideo;
import fvv.sdk.FvvZGVideoSDK.MessageHandle;

public class MainActivity extends AppCompatActivity {

    private String appID = "you app id";
    private String appSign = "you app sign 例如：(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x04,(byte)0x05";

    private FvvZGVideo fvvZGVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fvvZGVideo = new FvvZGVideo(getApplication(), true, new MessageHandle() {
            @Override
            public void Callback(String type, Object o) {
                Log.i("Callback",type + ":" + o.toString());
            }
        });
        fvvZGVideo.init(appID,appSign);
        //fvvZGVideo.setViewConfig("high",800,600,30,800 * 600 * 5);
        fvvZGVideo.setUser("fvv","fvv123");
        fvvZGVideo.startPreview(findViewById(R.id.textureView),false,"");
        fvvZGVideo.setPreviewMode("SCALETOFILL");

    }

    public void loginRoom(View view){
        fvvZGVideo.loginRoom("fvv","");
    }

    public void loginOut(View view){
        fvvZGVideo.logoutRoom();
    }

    public void startPublish(View view){
        fvvZGVideo.startPublishing("fvv","fvvTest","","");
    }

    public void stopPublish(View view){
        fvvZGVideo.stopPublishing();
    }

    public void startRecord(View view){
        fvvZGVideo.startRecord("","","/sdcard/new.mp4","");
    }

    public void stopRecord(View view){
        fvvZGVideo.stopRecord("");
    }

    public void enableBeautify(View view){
        fvvZGVideo.enableBeautify();
    }

    public void disableBeautify(View view){
        fvvZGVideo.disableBeautify();
    }

    public void test(View view){
        fvvZGVideo.setUser("fvv2","fvv222");
    }

    public void test2(View view){
        //fvvZGVideo.enableBeautify();
        fvvZGVideo.startPlayingStream("fvv",findViewById(R.id.textureView));
    }
}
