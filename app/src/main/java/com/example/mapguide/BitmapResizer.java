package com.example.mapguide;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

public class BitmapResizer implements Transformation {

    private int maxWidth = 700;
    private int maxHeight = 700;

    BitmapResizer(int maxWidth, int maxHeight){
        this.maxHeight=maxHeight;
        this.maxWidth=maxWidth;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int targetWidth, targetHeight;
        double aspectRatio;

        if (source.getWidth() > source.getHeight()) {
            targetWidth = maxWidth;
            aspectRatio = (double) source.getHeight() / (double) source.getWidth();
            targetHeight = (int) (targetWidth * aspectRatio);
        } else {
            targetHeight = maxHeight;
            aspectRatio = (double) source.getWidth() / (double) source.getHeight();
            targetWidth = (int) (targetHeight * aspectRatio);
        }

        Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
        if (result != source) {
            source.recycle();
        }
        return result;
    }

    @Override
    public String key() {
        return maxWidth + "x" + maxHeight;
    }



}
