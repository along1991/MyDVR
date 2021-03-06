package com.xinzhihui.mydvr.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;

import com.xinzhihui.mydvr.CameraActivity;
import com.xinzhihui.mydvr.ICameraManager;
import com.xinzhihui.mydvr.R;
import com.xinzhihui.mydvr.model.CameraDev;
import com.xinzhihui.mydvr.utils.LogUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RecordService extends Service {
    private final String TAG = getClass().getName();

    private List<CameraDev> cameraDevList;

//    private final IBinder mBinder = new LocalBinder();

    public class LoBinder extends ICameraManager.Stub {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void open(int cameraId) throws RemoteException {
            LogUtil.d("qiansheng", "Remote Open!!!");
            RecordService.this.open(cameraId);
        }

        @Override
        public void startPreView(int cameraId) throws RemoteException {
            LogUtil.d("qiansheng", "Remote StartPreView!!!");
            RecordService.this.startPreView(cameraId, null);
        }

        @Override
        public void stopPreView(int cameraId) throws RemoteException {
            LogUtil.d("qiansheng", "Remote StopPreView!!!");
            RecordService.this.stopPreView(cameraId);
        }

        @Override
        public boolean startRecord(int cameraId) throws RemoteException {
            LogUtil.d("qiansheng", "Remote StartRecord!!!");
            RecordService.this.startRecord(cameraId);
            return false;
        }

        @Override
        public void stopRecord(int cameraId) throws RemoteException {
            LogUtil.d("qiansheng", "Remote StopRecord!!!");
            RecordService.this.stopRecord(cameraId);
        }

        public RecordService getService() {
            return RecordService.this;
        }
    }

    private Binder mBinder = new LoBinder();


    Notification notification;

    public RecordService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cameraDevList = new ArrayList<CameraDev>();
        cameraDevList.add(null);
        cameraDevList.add(null);
        cameraDevList.add(null);
        cameraDevList.add(null);
        cameraDevList.add(null);
        cameraDevList.add(null);
        cameraDevList.add(null);
        LogUtil.d(TAG, "RecordService onCreate --------->");

        // 在API11之后构建Notification的方式
        Notification.Builder builder = new Notification.Builder(this); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, CameraActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                .setContentTitle("摄像头正在录制...") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("触摸可显示录制界面") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "RecordService onStartCommand --------->");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        LogUtil.d(TAG, "RecordService onBind --------->");
        return mBinder;
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.d(TAG, "RecordService onUnbind --------->");
//        for (CameraDev cameraDev : cameraDevList) {
//            cameraDev.mHandler = null;
//        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "RecordService onDestroy --------->");
    }


    public class LocalBinder extends Binder {
        public RecordService getService() {
            return RecordService.this;
        }
    }


    public void open(int cameraId) {
        cameraDevList.get(cameraId).open();
    }

    public void startPreView(int cameraId, SurfaceTexture surface) {
        cameraDevList.get(cameraId).startPreview(surface);
    }

    public void stopPreView(int cameraId) {
        cameraDevList.get(cameraId).stopPreview();
    }

    public boolean startRecord(int cameraId) {
        boolean isSuccess = cameraDevList.get(cameraId).startRecord();
        // 参数一：唯一的通知标识；参数二：通知消息。
        if (isSuccess) {
            startForeground(110, notification);// 开始前台服务
        }
        return isSuccess;
    }

    public void stopRecord(int cameraId) {
        cameraDevList.get(cameraId).stopRecord();
        if (!isRecording()) {
            stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        }
    }

    public void startRender(int cameraId, SurfaceTexture surface) {
        try {
            CameraDev cameraDev = cameraDevList.get(cameraId);
            cameraDev.camera.setPreviewTexture(surface);
            //TODO 反射调用startRender
            Class<?> c = cameraDev.camera.getClass();
            Method startRender = c.getMethod("startRender");
            startRender.invoke(cameraDev.camera);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            LogUtil.e(TAG, "No strartRender() method!!!");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void stopRender(int cameraId) {
        CameraDev cameraDev = cameraDevList.get(cameraId);
        //TODO 反射调用stopRender
        Class<?> c = cameraDev.camera.getClass();
        Method startRender = null;
        try {
            startRender = c.getMethod("stopRender");
            startRender.invoke(cameraDev.camera);
        } catch (NoSuchMethodException e) {
            LogUtil.e(TAG, "No stopRender() method!!!");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public void killRecord(int cameraId) {
        cameraDevList.get(cameraId).killRecord();
    }

    public void takePhoto(int cameraId) {
        cameraDevList.get(cameraId).takePhoto();
    }

    public void stopAllRecord() {
        for (CameraDev cameraDev : cameraDevList) {
            if (cameraDev != null) {
                if (cameraDev.isRecording()) {
                    cameraDev.stopRecord();
                }
            }
        }
        if (notification.visibility == View.VISIBLE) {
            stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        }
    }

    public CameraDev getCameraDev(int cameraId) {
        if (cameraDevList == null) {
            return null;
        }
        return cameraDevList.get(cameraId);
    }

    public void addCameraDev(int cameraId, CameraDev cameraDev) {
//        cameraDevList.add(cameraDev);
        cameraDevList.set(cameraId, cameraDev);
    }


    public boolean isPreView(int cameraId) {
        if (cameraDevList.get(cameraId) == null) {
            return false;
        } else {
            return cameraDevList.get(cameraId).isPreviewing();
        }
    }

    /**
     * 检查所有设备是否有设备正在录制
     *
     * @return
     */
    public boolean isRecording() {
        for (CameraDev cameraDev : cameraDevList) {
            if (cameraDev != null && cameraDev.isRecording()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断某个设备是否正在录制
     *
     * @param cameraId
     * @return
     */
    public boolean isRecording(int cameraId) {
        if (cameraDevList.get(cameraId) == null) {
            return false;
        } else {
            return cameraDevList.get(cameraId).isRecording();
        }
    }


}
