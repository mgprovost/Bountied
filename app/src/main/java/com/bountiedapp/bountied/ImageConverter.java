package com.bountiedapp.bountied;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/***************************************************************************************
 *  ImageConverter Class is made to convert a large bitmap to a small bitmap ~ 20kb
 ***************************************************************************************/

public class ImageConverter {

    // quality suggestion is used in the compression
    // however if the image is a png this is disregarded
    // since png's are lossless
    private final int QUALITY_SUGGESTION = 30;

    private Bitmap mReducedBitmap;
    private String mImageAsString;
    private String mImageLocation;

    // ctor that takes a blank bitmap and where an image is currently located
    public ImageConverter(Bitmap bitmap, String imageLocation) {
        mReducedBitmap = bitmap;
        mImageAsString = null;
        mImageLocation = imageLocation;
    }


    // reduce the size of an image based on a scaling factor that is calculated
    public Bitmap reduceImageSize() {

        // basic options object
        BitmapFactory.Options bmpOptions = new BitmapFactory.Options();

        // this allows us to query the bitmap without having to allocate to memory for pixels
        bmpOptions.inJustDecodeBounds = true;

        // decode a filepath into a bitmap
        BitmapFactory.decodeFile(mImageLocation, bmpOptions);

        // get the width and height of the image
        int cameraImageWidth = bmpOptions.outWidth;
        int cameraImageHeight = bmpOptions.outHeight;

        // we need a scale factor because send a massive original size image
        // is too expensive to continuously send to back and forth from our database
        int scaleFactor = Math.max(cameraImageWidth/ 800, cameraImageHeight/ 800);

        // If set to a value > 1, requests the decoder to subsample the
        // original image, returning a smaller image to save memory.
        bmpOptions.inSampleSize = scaleFactor;
        bmpOptions.inJustDecodeBounds = false;

        // decode a filepath into a bitmap
        mReducedBitmap = BitmapFactory.decodeFile(mImageLocation, bmpOptions);

        return mReducedBitmap;
    }

    // check to see if image is set to correct rotation so it does not show up flipped or sideways
    // if not, it will rotate the image to the proper rotation with which to view
    public Bitmap rotateImage(Bitmap bitmap) {

        // exifInterface is for finding out details about an image
        ExifInterface exifInterface = null;

        try {
            exifInterface = new ExifInterface(mImageLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get the orientation of the image
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        // matrix is used for rotation of the image
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

    // returns a base64 encoded string of the reduced bitmap
    public String getImageAsString() {
        return mImageAsString;
    }
}
