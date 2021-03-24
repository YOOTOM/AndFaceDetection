package com.anibear.andfacedetection.utils.interfaces;

import android.graphics.Bitmap;

import com.anibear.andfacedetection.utils.common.FrameMetadata;
import com.anibear.andfacedetection.utils.common.GraphicOverlay;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

public interface FrameReturn{
    void onFrame(
            Bitmap image,
            FirebaseVisionFace face,
            FrameMetadata frameMetadata,
            GraphicOverlay graphicOverlay
    );
}