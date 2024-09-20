package com.chenl.test514;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.os.Environment;
import android.se.omapi.Reader;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

public class VideoDecoder implements VideoCodec{
    private Surface mSurface;


    private final int TIMEOUT_US = 1000;
    private static final String TAG = "接收广播";
    public ArrayList<Bitmap> bitmaps;
    private Worker mWorker;
    private MediaCodec decoder;
    private int mWidth= 352;
    private int mHeight= 288;
    MediaCodec.BufferInfo mBufferInfo;

    private ArrayList<byte[]> data;
    public static double totalTime;
    public static int success;
    public static boolean finished;
    public VideoDecoder(Surface surface, ArrayList<byte[]> data)
    // throws DecoderServerNullException
    {
        if (surface == null) {
            Log.e("surface","未初始化");
            // throw new DecoderServerNullException();
        }
        this.mSurface = surface;
        this.data = data;
        initMediaCodec();
    }

    private void initMediaCodec() {
        try {
            //创建解码器 H264的Type为  AAC
            decoder = MediaCodec.createDecoderByType("video/avc");
            //创建配置
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", mWidth, mHeight);
            mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mHeight * mWidth);
            mediaFormat.setInteger(MediaFormat.KEY_MAX_HEIGHT, mHeight);
            mediaFormat.setInteger(MediaFormat.KEY_MAX_WIDTH, mWidth);
            //设置解码预期的帧速率【以帧/秒为单位的视频格式的帧速率的键】
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 10);
            //配置绑定mediaFormat和surface
            decoder.configure(mediaFormat, mSurface, null, 0);
        } catch (IOException e) {
            e.printStackTrace();
            //创建解码失败
            Log.e(TAG, "创建解码失败");
        }
    }

    public void start() {
        if (mWorker == null) {
            mWorker = new Worker();
            mWorker.setRunning(true);
            mWorker.start();
        }
    }

    private class Worker extends Thread {
        volatile boolean isRunning;

        public void setRunning(boolean running) {
            isRunning = running;
        }

        @Override
        public void run() {
            decoder.start();
            mBufferInfo = new MediaCodec.BufferInfo();
            while (isRunning) {
                decode(data);
            }
            release();
        }

        private void decode(ArrayList<byte[]> mData) {
            finished = false;
            int packetCoded = 0;
            double startTime = System.currentTimeMillis();
            for (int i = 0; i < mData.size(); i++) {
                // 查询1000毫秒后，如果dSP芯片的buffer全部被占用，返回-1；存在则大于0
                int inIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
                if (inIndex >= 0) {
                    //根据返回的index拿到可以用的buffer
                    ByteBuffer buffer = decoder.getInputBuffer(inIndex);
                    if (buffer == null) {
                        Log.i(TAG, "buffer=null");
                        return;
                    }
                    //清空缓存
                    buffer.clear();
                    if (mData.get(i) == null) {
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        decoder.queueInputBuffer(inIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isRunning = false;
                    } else {
                        //开始为buffer填充数据
                        buffer.put(mData.get(i), 0, mData.get(i).length);
                        //buffer.clear();
                        buffer.limit(mData.get(i).length);
                        //填充数据后通知mediacodec查询inIndex索引的这个buffer
                        decoder.queueInputBuffer(inIndex, 0, mData.get(i).length, 0,0);
                        //MediaCodec.BUFFER_FLAG_SYNC_FRAME
                        packetCoded++;
                        Log.e(TAG, "解码成功");
                    }
                }
                //mediaCodec 查询 "mediaCodec的输出方队列"得到索引
                int outIndex = decoder.dequeueOutputBuffer(mBufferInfo,TIMEOUT_US);

                if (outIndex >= 0) {
                    try {
                        //暂时以休眠线程方式放慢播放速度
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //如果surface绑定了，则直接输入到surface渲染并释放
                    decoder.releaseOutputBuffer(outIndex, true);
                }else {
                    Log.e(TAG, "没有解码成功");
                }
            }
            setRunning(false);
            double endTime = System.currentTimeMillis();
            totalTime = (endTime - startTime)/1000;
            finished = true;
            success = packetCoded;
        }

        /**
         * 释放资源
         */
        private void release() {
            if (decoder != null) {
                decoder.stop();
                decoder.release();
            }
        }
    }

    public int bytesToInt(byte[] bytes) {
        int i;
        i = (int) ((bytes[0] & 0xff) | ((bytes[1] & 0xff) << 8)
                | ((bytes[2] & 0xff) << 16) | ((bytes[3] & 0xff) << 24));
        return i;
    }
}
