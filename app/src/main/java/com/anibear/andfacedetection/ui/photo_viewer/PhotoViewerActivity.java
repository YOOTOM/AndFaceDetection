package com.anibear.andfacedetection.ui.photo_viewer;

import android.os.Bundle;
import android.widget.ImageView;

import com.anibear.andfacedetection.R;
import com.anibear.andfacedetection.utils.base.BaseActivity;
import com.anibear.andfacedetection.utils.base.PublicMethods;

import static com.anibear.andfacedetection.utils.base.Cons.IMG_EXTRA_KEY;
import static com.anibear.andfacedetection.utils.base.Cons.IMG_FILE;

public class PhotoViewerActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
        if (getIntent().hasExtra(IMG_EXTRA_KEY)) {
            ImageView imageView = findViewById(R.id.image);
            String imagePath = getIntent().getStringExtra(IMG_EXTRA_KEY);
            imageView.setImageBitmap(PublicMethods.getBitmapByPath(imagePath, IMG_FILE));
        }
    }
}
