package com.anibear.andfacedetection.utils.interfaces;

import com.anibear.andfacedetection.utils.models.RectModel;

public interface FaceDetectStatus {
    void onFaceLocated(RectModel rectModel);
    void onFaceNotLocated() ;
}
