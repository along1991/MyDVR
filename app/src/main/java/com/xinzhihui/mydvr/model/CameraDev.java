package com.xinzhihui.mydvr.model;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;

import com.xinzhihui.mydvr.AppConfig;
import com.xinzhihui.mydvr.listener.CameraStatusListener;
import com.xinzhihui.mydvr.utils.DateTimeUtil;
import com.xinzhihui.mydvr.utils.LogUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2016/9/28.
 */
public abstract class CameraDev {
    private final String TAG = getClass().getName();

    public int cameraid;
    public CameraStatusListener statusListener;

    private Camera camera;
    private MediaRecorder mediaRecorder;

    private boolean isPreviewing = false;
    private boolean isRecording = false;


    /**
     * 打开摄像头
     * @return Camera对象
     */
    public Camera open(){
        if (camera!=null) {
            camera.release();
            camera = null;
        }
        try {
            camera = Camera.open(cameraid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    /**
     * 开始预览
     * @param surface
     */
    public void startPreview(SurfaceTexture surface){
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.getSupportedPreviewSizes().get(0);
                LogUtil.d("qiansheng", "size:" + parameters.getSupportedPreviewSizes().get(0) );
                parameters.setPreviewSize(1280, 720);

                camera.setParameters(parameters);

                camera.setPreviewTexture(surface);
                camera.startPreview();
                setPreviewing(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止预览，释放资源
     */
    public void stopPreview(){
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;

            setPreviewing(false);
        }else {
            LogUtil.i(TAG, "stopPreView ----------->camera is null");
        }
    }

    /**
     * 开始录像
     * @param
     */
    public void startRecord(final int profileType){
        mediaRecorder = new MediaRecorder();
        File dir = new File(AppConfig.FRONT_VIDEO_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(AppConfig.FRONT_VIDEO_PATH + DateTimeUtil.getCurrentNumberDateTime() + ".mp4");
        CamcorderProfile camcorderProfile = null;
        camcorderProfile = CamcorderProfile.get(profileType);

        camera.unlock();

        mediaRecorder.reset();

        mediaRecorder.setCamera(camera);

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); //前置
        mediaRecorder.setOutputFormat(camcorderProfile.fileFormat); //先设置输出格式
        mediaRecorder.setVideoFrameRate(camcorderProfile.videoFrameRate);

        mediaRecorder.setVideoSize(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight);

        mediaRecorder.setVideoEncodingBitRate(camcorderProfile.videoBitRate);

        mediaRecorder.setVideoEncoder(camcorderProfile.videoCodec); //后设置视频编码格式

        mediaRecorder.setOutputFile(file.getAbsolutePath());

        mediaRecorder.setMaxDuration(AppConfig.MAX_DURATION);

        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                switch (what) {
                    case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                        stopRecord();
                        startRecord(profileType);
                        break;
                    default:
                        break;
                }
            }
        });

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();

        setRecording(true);
//        handler.sendEmptyMessage(1);
        statusListener.onStartRecord();
    }

    /**
     * 停止录像
     * @param
     */
    public void stopRecord(){
        if (mediaRecorder != null) {
            try {
                mediaRecorder.setOnInfoListener(null);
                mediaRecorder.stop();
                mediaRecorder.release();

//            mediaRecorder = null;

                setRecording(false);
                statusListener.onStopRecord();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                LogUtil.e(TAG, "stopRecord *************>mediaRecorder stop failed!!!");
            }
        }else {
            setRecording(false);
            LogUtil.i(TAG, "stopRecord --------->mediaRecorder is null!");
        }

    }

    public void setPreviewing(boolean previewing) {
        isPreviewing = previewing;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public boolean isPreviewing() {
        return isPreviewing;
    }

    public boolean isRecording() {
        return isRecording;
    }
}
