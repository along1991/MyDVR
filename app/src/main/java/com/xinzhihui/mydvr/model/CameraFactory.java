package com.xinzhihui.mydvr.model;

import com.xinzhihui.mydvr.AppConfig;

/**
 * Created by Administrator on 2016/9/28.
 */
public class CameraFactory {

    public CameraDev createCameraDev(int cameraid){
        CameraDev cameraDev;
        switch (cameraid) {
            case AppConfig.FRONT_CAMERA:
                cameraDev = new FrontCameraDev(AppConfig.FRONT_CAMERA_INDEX);
                break;

            case AppConfig.BEHIND_CAMERA:
                cameraDev = new BehindCameraDev(AppConfig.BEHIND_CAMERA_INDEX);
                break;

            case AppConfig.LEFT_CAMERA:
                cameraDev = new FrontCameraDev(AppConfig.LEFT_CAMERA_INDEX);
                break;

            case AppConfig.RIGHT_CAMERA:
                cameraDev = new FrontCameraDev(AppConfig.RIGHT_CAMERA_INDEX);
                break;

            default:
                cameraDev = new FrontCameraDev(AppConfig.FRONT_CAMERA_INDEX);
                break;
        }

        return cameraDev;
    }
}
