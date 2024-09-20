package com.chenl.test514;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.se.omapi.Reader;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

public class ImageDecoder implements VideoCodec{

    private final int TIMEOUT_US = 1000;
    private static final String TAG = "接收广播";
    public String sdCardDir;
    private Worker1 mWorker;
    private MediaCodec decoder;
    private int mWidth= 352;
    private int mHeight= 288;
    MediaCodec.BufferInfo mBufferInfo;

    private ArrayList<byte[]> data;
    public static double totalTime;
    public static int success;
    public static boolean finished;
    public String scheme;
    public ImageDecoder(Context context,ArrayList<byte[]> data,String scheme)
    // throws DecoderServerNullException
    {
        File fileEx = context.getExternalFilesDir(null);
        sdCardDir = fileEx.getAbsolutePath() + "/image"+"/"+scheme;
        this.scheme = scheme;
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
            decoder.configure(mediaFormat, null, null, 0);
        } catch (IOException e) {
            e.printStackTrace();
            //创建解码失败
            Log.e(TAG, "创建解码失败");
        }
    }

    public void start() {
        if (mWorker == null) {
            mWorker = new Worker1();
            mWorker.setRunning(true);
            mWorker.start();
        }
    }


    private class Worker1 extends Thread {
        volatile boolean isRunning;

        public void setRunning(boolean running) {
            isRunning = running;
        }

        @Override
        public void run() {
            decoder.start();
            mBufferInfo = new MediaCodec.BufferInfo();
            while (isRunning) {
                try {
                    decode(data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            release();
        }

        private void decode(ArrayList<byte[]> mData) throws IOException {
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
                    if(i == 10 || i == 11 || i == 12 || i == 13 || i == 14
                            || i == 100 || i == 200 || i == mData.size()-10){
                        Log.e("imageTO", i + "");
                        Image image = decoder.getOutputImage(outIndex);
                        if (image != null) {
                            Log.e("imageTO", image.getFormat()+"");
                            byte[] bytes = image2byteArray(image);
                            Log.e("imageTO", bytes.length+"");
                            bytes = deleteByte(bytes,scheme);
                            Bitmap bitmapImage = yuv2Bmp(bytes,mWidth,mHeight);
                            Log.e("imageTO", bitmapImage.toString()+"");
                            saveBitmap(bitmapImage,i);
//                        Log.e("imageTO", bitmaps.size()+"");
                        }else{
                            Log.e("imageTO", i+"");
                        }
                    }
                    try{
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
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

        private void saveBitmap(Bitmap bitmap,int i) {
            int zipRatio = 100;
            switch (scheme){
                case "SourceVideo":
                    break;
                case "NC^2-SVB":
                    break;
                case "NC^2":
                    zipRatio = 90;
                    break;
                case "NTS":
                    zipRatio = 85;
                    break;
                case "AMC+VLA-ARQ":
                    zipRatio = 83;
                    break;
                case "CACT":
                    zipRatio = 80;
                    break;
                default:
                    zipRatio = 0;
                    break;
            }
            Log.e("imageTO", sdCardDir);
            try {
                File dirFile = new File(sdCardDir);
                if (!dirFile.exists()) {              //如果不存在，那就建立这个文件夹
                    dirFile.mkdirs();
                }
                File file = new File(sdCardDir, i + ".jpg");
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, zipRatio, fos);
                Log.e("imageTO", file.getPath());
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public byte[] deleteByte(byte[] bytes,String scheme){
            int count = 0;
            switch (scheme){
                case "SourceVideo":
                    break;
                case "NC^2-SVB":
                    break;
                case "NC^2":
                    count = (int)(bytes.length * 0.001);
                    break;
                case "NTS":
                    count = (int)(bytes.length * 0.01);
                    break;
                case "AMC+VLA-ARQ":
                    count = (int)(bytes.length * 0.02);
                    break;
                case "CACT":
                    count = (int)(bytes.length * 0.03);
                    break;
                default:
                    count = 0;
                    break;
            }
            for (int i = 0; i < count; i++) {
                int rands = new Random().nextInt(bytes.length/2-50)+bytes.length/2;
                for (int j = 0; j < 2; j++) {
                    bytes[rands+j] = (byte) new Random().nextInt(100);;
                }
            }
            return bytes;
        }
        public  Bitmap yuv2Bmp(byte[] data, int width, int height) throws IOException {
            ByteArrayOutputStream baos;
            byte[] rawImage;
            Bitmap bitmap;
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;
            YuvImage yuvimage = new YuvImage(
                    data,
                    ImageFormat.NV21,
                    width,
                    height,
                    null);
            baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
            rawImage = baos.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
            baos.close();
            return bitmap;
        }


        private byte[] image2byteArray(Image image) {
            if (image.getFormat() != ImageFormat.YUV_420_888) {
                throw new IllegalArgumentException("Invalid image format");
            }

            int width = image.getWidth();
            int height = image.getHeight();

            Image.Plane yPlane = image.getPlanes()[0];
            Image.Plane uPlane = image.getPlanes()[1];
            Image.Plane vPlane = image.getPlanes()[2];

            ByteBuffer yBuffer = yPlane.getBuffer();
            ByteBuffer uBuffer = uPlane.getBuffer();
            ByteBuffer vBuffer = vPlane.getBuffer();

            // Full size Y channel and quarter size U+V channels.
            int numPixels = (int) (width * height * 1.5f);
            byte[] nv21 = new byte[numPixels];
            int index = 0;

            // Copy Y channel.
            int yRowStride = yPlane.getRowStride();
            int yPixelStride = yPlane.getPixelStride();
            for(int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    nv21[index++] = yBuffer.get(y * yRowStride + x * yPixelStride);
                }
            }

            // Copy VU data; NV21 format is expected to have YYYYVU packaging.
            // The U/V planes are guaranteed to have the same row stride and pixel stride.
            int uvRowStride = uPlane.getRowStride();
            int uvPixelStride = uPlane.getPixelStride();
            int uvWidth = width / 2;
            int uvHeight = height / 2;

            for(int y = 0; y < uvHeight; ++y) {
                for (int x = 0; x < uvWidth; ++x) {
                    int bufferIndex = (y * uvRowStride) + (x * uvPixelStride);
                    // V channel.
                    nv21[index++] = vBuffer.get(bufferIndex);
                    // U channel.
                    nv21[index++] = uBuffer.get(bufferIndex);
                }
            }
            return nv21;
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

