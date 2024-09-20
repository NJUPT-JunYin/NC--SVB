package com.chenl.test514;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class YUVUtils {
    ArrayList<ArrayList<Integer>> lostList;
    private static int mWidth = 352;
    private static int mHeight = 288;
    private static int frameSize = (mWidth*mHeight*3)/2;

    public void setLostList(ArrayList<ArrayList<Integer>> list){
        this.lostList = list;
    }
    public  ArrayList<byte[]> yuvToH264(byte[] yuv,int frameRate) throws IOException {
        Log.i("YUVUtils","开始转H264");

        MediaCodec codec = MediaCodec.createEncoderByType("video/avc");
        MediaFormat format = MediaFormat.createVideoFormat("video/avc",mWidth,mHeight);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mWidth * mHeight * frameRate);
        //format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        codec.start();
        byte[] mData = new byte[frameSize];
        ArrayList<byte[]> H264List = new ArrayList<>();

        for (int i = 0; i < yuv.length/frameSize; i++) {
            Log.i("YUVUtils", "第" + i + "次转换");
            if(lostList != null && lostList.get(i%4).contains(i/4)){
                continue;
            }
            System.arraycopy(yuv, i * frameSize, mData, 0, frameSize);
            byte[] trueData = new byte[frameSize];
            yv12ToNv12(mData,trueData,mWidth,mHeight);
            mData = trueData;
            ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            int inputBufferIndex = codec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                // 计算时间戳
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(mData);
                codec.queueInputBuffer(inputBufferIndex, 0, mData.length, i, 0);
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
            // 从输出缓冲区队列中拿到编码好的内容，对内容进行相应处理后在释放
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                H264List.add(outData);

                codec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
            }
        }

        if (codec != null) {
            codec.stop();
            codec.release();
        }

        return H264List;
    }
    //YV12转YU12（I420）
    public static void yv12ToYu12(byte[] yv12bytes, byte[] i420bytes, int width, int height) {
        System.arraycopy(yv12bytes, 0, i420bytes, 0, width * height);
        System.arraycopy(yv12bytes, width * height + width * height / 4, i420bytes, width * height, width * height / 4);
        System.arraycopy(yv12bytes, width * height, i420bytes, width * height + width * height / 4, width * height / 4);
    }

    //NV12转YU12（I420）
    public static void nv12ToYu12(byte[] nv12, byte[] yu12, int width, int height) {
        int nLenY = width * height;
        int nLenU = nLenY / 4;
        System.arraycopy(nv12, 0, yu12, 0, width * height);
        for (int i = 0; i < nLenU; i++) {
            yu12[nLenY + i] = nv12[nLenY + 2 * i + 1];
            yu12[nLenY + nLenU + i] = nv12[nLenY + 2 * i];
        }
    }

    //YV12转NV12
    public static void yv12ToNv12(byte[] yv12, byte[] nv12, int width, int height) {
        int nLenY = width * height;
        int nLenU = nLenY / 4;
        System.arraycopy(yv12, 0, nv12, 0, width * height);
        for (int i = 0; i < nLenU; i++) {
            nv12[nLenY + 2 * i + 1] = yv12[nLenY + i];
            nv12[nLenY + 2 * i] = yv12[nLenY + nLenU + i];
        }
    }

    //NV21转NV12
    public static void nv21ToNv12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / (mWidth * mHeight * 5);
    }
}
