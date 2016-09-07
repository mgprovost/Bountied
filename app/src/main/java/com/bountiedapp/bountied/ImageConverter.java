package com.bountiedapp.bountied;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class is made to convert a large bitmap to a small bitmap ~ 20kb or
 */
public class ImageConverter {

    private Bitmap mReducedBitmap;
    private String mImageAsString;
    private String mImageLocation;
    private final int QUALITY_SUGGESTION = 30;

    public ImageConverter(Bitmap bitmap, String imageLocation) {
        mReducedBitmap = bitmap;
        mImageAsString = null;
        mImageLocation = imageLocation;
    }

    public Bitmap reduceImageSize() {

        BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
        bmpOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageLocation, bmpOptions);
        int cameraImageWidth = bmpOptions.outWidth;
        int cameraImageHeight = bmpOptions.outHeight;

        int scaleFactor = Math.max(cameraImageWidth/ 800, cameraImageHeight/ 800);

        System.out.println("SCALE FACTOR: " + scaleFactor);

        bmpOptions.inSampleSize = scaleFactor;
        bmpOptions.inJustDecodeBounds = false;

        mReducedBitmap = BitmapFactory.decodeFile(mImageLocation, bmpOptions);
        return mReducedBitmap;
    }

    public Bitmap rotateImage(Bitmap bitmap) {
        ExifInterface exifInterface = null;

        try {
            exifInterface = new ExifInterface(mImageLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            default:
        }

        mReducedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return mReducedBitmap;
        //mImageView.setImageBitmap(mRotatedBitmap);
    }

    // returns a base64 encoded string of the reduced bitmap
    public String getImageAsString() {
        return mImageAsString;
    }

    // compresses image to whatever quality setting is
    // then sets the compressed image to a base64 encoded string used to send over the network
    // then sets that as the member variable mImageAsString
    public void compressAndConvertToBase64String() {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        mReducedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY_SUGGESTION, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        mImageAsString = encodedImage;
    }

}
