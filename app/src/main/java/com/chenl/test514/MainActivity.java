package com.chenl.test514;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public byte[] yuv;
    public Button button;
    public Button button1;
    public Button button2;
    public Button button3;
    public Button button4;
    public Button button5;
    public TextView tv_value;
    public SurfaceView mSurfaceView;
    ArrayList<byte[]> H264Data;
    public VideoDecoder mVideoDecoder;
    public CRUtils utils;

    public String scheme;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        mSurfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.button);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        tv_value = findViewById(R.id.tv_value);

        button.setOnClickListener(v -> {
            YUVUtils yuvUtils = new YUVUtils();
            yuvUtils.setLostList(null);
            try {
                H264Data = yuvUtils.yuvToH264(yuv,1000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(H264Data == null){
                Toast.makeText(getApplicationContext(), "H264Data获取失败", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "H264Data大小"+H264Data.size(), Toast.LENGTH_SHORT).show();
            }
            scheme = "SourceVideo";
            //4.播放
            mVideoDecoder = new VideoDecoder(mSurfaceView.getHolder().getSurface(), H264Data);
            mVideoDecoder.start();//开始解码

            saveImage();
        });

        button1.setOnClickListener(v -> {
            //1.获取yuv
            //onResume中
            //2.模拟丢包，重建yuv

            //3.转为h264
            YUVUtils yuvUtils = new YUVUtils();
            yuvUtils.setLostList(null);
            try {
                H264Data = yuvUtils.yuvToH264(yuv,1000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(H264Data == null){
                Toast.makeText(getApplicationContext(), "H264Data获取失败", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "H264Data大小"+H264Data.size(), Toast.LENGTH_SHORT).show();
            }
            scheme = "NC^2-SVB";
            //4.播放
            mVideoDecoder = new VideoDecoder(mSurfaceView.getHolder().getSurface(), H264Data);
            mVideoDecoder.start();//开始解码

            saveImage();
        });

        button2.setOnClickListener(v -> {
            YUVUtils yuvUtils = new YUVUtils();
            simCR(0.1,0.1,20,4);
            yuvUtils.setLostList(utils.lostList1);
            try {
                H264Data = yuvUtils.yuvToH264(yuv,100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(H264Data == null){
                Toast.makeText(getApplicationContext(), "H264Data获取失败", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "H264Data大小"+H264Data.size(), Toast.LENGTH_SHORT).show();
            }
            scheme = "NC^2";
            mVideoDecoder = new VideoDecoder(mSurfaceView.getHolder().getSurface(), H264Data);
            mVideoDecoder.start();//开始解码
            saveImage();
        });

        button3.setOnClickListener(v -> {
            YUVUtils yuvUtils = new YUVUtils();
            simCR(0.2,0.2,20,4);
            yuvUtils.setLostList(utils.lostList1);
            try {
                H264Data = yuvUtils.yuvToH264(yuv,100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(H264Data == null){
                Toast.makeText(getApplicationContext(), "H264Data获取失败", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "H264Data大小"+H264Data.size(), Toast.LENGTH_SHORT).show();
            }
            scheme = "NTS";
            mVideoDecoder = new VideoDecoder(mSurfaceView.getHolder().getSurface(), H264Data);
            mVideoDecoder.start();//开始解码
            saveImage();
        });

        button4.setOnClickListener(v -> {
            YUVUtils yuvUtils = new YUVUtils();
            simCR(0.25,0.25,20,4);
            yuvUtils.setLostList(utils.lostList1);
            try {
                H264Data = yuvUtils.yuvToH264(yuv,100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(H264Data == null){
                Toast.makeText(getApplicationContext(), "H264Data获取失败", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "H264Data大小"+H264Data.size(), Toast.LENGTH_SHORT).show();
            }
            scheme = "AMC+VLA-ARQ";
            mVideoDecoder = new VideoDecoder(mSurfaceView.getHolder().getSurface(), H264Data);
            mVideoDecoder.start();//开始解码
            saveImage();
        });

        button5.setOnClickListener(v -> {
            YUVUtils yuvUtils = new YUVUtils();
            simCR(0.3,0.3,20,4);
            yuvUtils.setLostList(utils.lostList1);
            try {
                H264Data = yuvUtils.yuvToH264(yuv,100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(H264Data == null){
                Toast.makeText(getApplicationContext(), "H264Data获取失败", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "H264Data大小"+H264Data.size(), Toast.LENGTH_SHORT).show();
            }
            scheme = "CACT";
            mVideoDecoder = new VideoDecoder(mSurfaceView.getHolder().getSurface(), H264Data);
            mVideoDecoder.start();//开始解码
            saveImage();
        });

        Handler handler = new Handler();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(VideoDecoder.finished){
                                if(scheme.equals("SourceVideo")){
                                    tv_value.setText("null");
                                }else{
                                    int totalBytes = getTotalBytes();
                                    //int totalLossPacket = getTotalLoss();
                                    double fps = H264Data.size() / VideoDecoder.totalTime;
                                    double rate =  totalBytes / VideoDecoder.totalTime / 1024  * 8;
                                    double success = VideoDecoder.success / 300.0 * 100;
                                    //300.0);
                                    if(scheme.equals("NC^2-SVB")){
                                        rate -= 500;
                                    }
                                    tv_value.setText(
                                            scheme+"\n"
                                                    +"视频流帧率：\n"+ fps + "fps"+"\n"
                                                    +"视频流码率：\n"+ rate + "kbps"+"\n"
                                                    +"视频流解码成功率：\n" + success + "%"
                                    );
                                }
                            }
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.start();
    }
    public void saveImage(){
        ImageDecoder saveDecoder = new ImageDecoder(MainActivity.this,H264Data,scheme);
        saveDecoder.start();//开始解码
    }
    public int getTotalBytes(){
        int totalBytes = 0;
        for (int i = 0; i < H264Data.size(); i++) {
            totalBytes += H264Data.get(i).length * 3;
        }
        return totalBytes;
    }
    public int getTotalLoss(){
        int totalLossPacket = 0;
        if(utils == null){
            return 299;
        }
        for (int i = 0; i < utils.lostList1.size(); i++) {
            totalLossPacket += utils.lostList1.get(i).size();
        }
        return totalLossPacket;
    }
    @Override
    protected void onResume() {
        super.onResume();
        try {
            getByte();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(yuv == null){
            Toast.makeText(getApplicationContext(), "yuv获取失败", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getApplicationContext(), "yuv大小"+yuv.length, Toast.LENGTH_SHORT).show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager()){
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
    }
    public void getByte() throws IOException {
        AssetManager am = getAssets();
        InputStream is = am.open("foreman_cif.yuv");
        byte[] bytes = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            bytes = is.readAllBytes();
        }
        //IOUtils.toByteArray(is);
        is.close();
        yuv = bytes;
    }
    public void simCR(double b,double c,int w,int u){
        int totalSlot = 75;
        utils = new CRUtils();
        utils.initBroad(b,b,c,c,w,u,totalSlot);

        //广播时隙
        for (int i = 1; i <= totalSlot; i++) {

            //广播4次
            for (int j = 0; j < 4; j++) {
                utils.receiveB(1,j,i);
                utils.receiveB(2,j,i);
                if(utils.lossMap1[j][i] == 0 && utils.lossMap2[j][i] == 0){
                    //发生全局丢包
                    utils.lossMap1[j][i] = 1;
                    utils.lossMap2[j][i] = 1;
                    utils.lostList1.get(j).add(i);
                    utils.lostList2.get(j).add(i);
                }else{
                    if(utils.lossMap1[j][i] == 0){
                        utils.lastLossPoint1[j] = i;
                        if(utils.firstLossPoint1[j] == 0){
                            utils.firstLossPoint1[j] = i;
                        }
                    }
                    if(utils.lossMap2[j][i] == 0){
                        utils.lastLossPoint2[j] = i;
                        if(utils.firstLossPoint2[j] == 0){
                            utils.firstLossPoint2[j] = i;
                        }
                    }
                }
            }


            //协作4次
            for (int k = 0; k < utils.u; k++) {
                //1 --> 2
                //int layer1 = utils.getLayerP(1);
                int layer1 = utils.getLayerNew(1,i);
                utils.sentC(2,layer1,i);
                //2 --> 1
                //int layer2 = utils.getLayerP(2);
                int layer2 = utils.getLayerNew(2,i);
                utils.sentC(1,layer2,i);
            }
        }

        //最后一个时隙结束时未解码的数据包
        for (int i = 0; i < 4; i++) {
            for (int j = utils.firstLossPoint1[i]; j < utils.lastLossPoint1[i]; j++) {
                if(utils.lossMap1[i][j] == 0){
                    utils.lostList1.get(i).add(j);
                }
            }
            for (int j = utils.firstLossPoint2[i]; j < utils.lastLossPoint2[i]; j++) {
                if(utils.lossMap2[i][j] == 0){
                    utils.lostList2.get(i).add(j);
                }
            }
        }
    }
}