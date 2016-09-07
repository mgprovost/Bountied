package com.bountiedapp.bountied.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bountiedapp.bountied.ImageConverter;
import com.bountiedapp.bountied.InternalReader;
import com.bountiedapp.bountied.InternalWriter;
import com.bountiedapp.bountied.Upload;
import com.bountiedapp.bountied.model.FoundBounty;
import com.bountiedapp.bountied.model.Gps;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by mprovost on 8/30/2016.
 */
public class Button {

    private String mImageLocation;

    private int REQUEST_IMAGE_CAPTURE = 1;

    public Button() {}

    public void startCameraIntent(Activity activity) {
        Intent cameraApplicationIntent = new Intent();
        cameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        activity.startActivityForResult(cameraApplicationIntent, REQUEST_IMAGE_CAPTURE);
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);

        mImageLocation = image.getAbsolutePath();
        return image;
    }

    public void uploadCameraPicture(Context context, String placerID, String bountyID) {

        Gps gps = new Gps(context);

        Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(mImageLocation);
        ImageConverter imageConverter = new ImageConverter(photoCapturedBitmap, mImageLocation);
        Bitmap reducedImageBitmap = imageConverter.reduceImageSize();
        Bitmap reducedAndRotatedBitmap = imageConverter.rotateImage(reducedImageBitmap);
        imageConverter.compressAndConvertToBase64String();

        // get reducedAndRotatedBitmap as string to send to server
        String imageAsString = imageConverter.getImageAsString();

        // get the id of the current logged in user
        InternalReader internalReader = new InternalReader(context);
        String id = internalReader.readFromFile("ID");

        // get most recent gps coords
        gps.setLastKnownLatLng(context);
        String lat = Double.toString(gps.getmLat());
        String lng = Double.toString(gps.getmLng());

        gps.stopGps(context);

        FoundBounty foundBounty = new FoundBounty(placerID, bountyID, id, imageAsString, lat, lng);

        System.out.println("FOUND BOUNTY: " + foundBounty.toString());

        Upload upload = new Upload();

        try {
            upload.bountyFound(context, foundBounty);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getmImageLocation() {
        return mImageLocation;
    }

    public void saveBounty(Context context, String bountyID) {
        InternalReader internalReader = new InternalReader(context);
        InternalWriter internalWriter = new InternalWriter(context);
        // save the current bounty id to bouties_to_hunt internal storage
        // specifically just save the id of the bounty, the data will be
        // pulled from the server when user accesses that activity
        String filename = "bounties_to_hunt";

//        internalWriter.writeToMemory(filename, "");

        // look to see what ids are stored
        String savedIds = internalReader.readFromFile(filename);

        if (savedIds.equals("") || savedIds.equals("[]")) {
            ArrayList<String> savedIdsArray = new ArrayList<String>();
            savedIdsArray.add(bountyID);
            internalWriter.writeToMemory(filename, savedIdsArray.toString());
            Toast.makeText(context, savedIds.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(context, "Bounty has been saved to your hunts-in-progress", Toast.LENGTH_LONG).show();
        }
        else if(savedIds.contains(bountyID)) {
            Toast.makeText(context, "This bounty is already saved to your hunts-in-progress", Toast.LENGTH_LONG).show();
        }
        else {
            // otherwise go ahead and store it in string format
            ArrayList<String> savedIdsArray = getArrayFromString(savedIds);
            savedIdsArray.add(bountyID);
            internalWriter.writeToMemory(filename, "");
            internalWriter.writeToMemory(filename, savedIdsArray.toString());
            Toast.makeText(context, internalReader.readFromFile("bounties_to_hunt"), Toast.LENGTH_LONG).show();
            Toast.makeText(context, "Bounty has been saved to your hunts-in-progress", Toast.LENGTH_LONG).show();
        }
    }

    public void deleteBounty(Context context, String bountyID) {
        InternalReader internalReader = new InternalReader(context);
        InternalWriter internalWriter = new InternalWriter(context);
        // save the current bounty id to bouties_to_hunt internal storage
        // specifically just save the id of the bounty, the data will be
        // pulled from the server when user accesses that activity
        String filename = "bounties_to_hunt";

        // look to see what ids are stored
        String savedIds = internalReader.readFromFile(filename);

        // if the bounty is already in the saved bounties
        // give the user a toast message saying that, and simply break out of this
        ArrayList<String> savedIdsArray = getArrayFromString(savedIds);

        int indexOfBountyToDelete = savedIdsArray.indexOf(bountyID);
        savedIdsArray.remove(indexOfBountyToDelete);

        internalWriter.writeToMemory(filename, savedIdsArray.toString());
        System.out.println("Saving to internal memory: " + savedIdsArray.toString());
    }


    // get an array list from a string
    // this return an arraylist from the downloaded "found" string format
    private ArrayList getArrayFromString(String someString) {

        // remove the left bracket, then right bracket, then all spaces from the string
        String removeLeftBracket = someString.replace("[", "");
        String removeRightBracket = removeLeftBracket.replace("]", "");
        String stringWithRemoval = removeRightBracket.replace(" ", "");

        // split the string on all "," in it and then make each of those strings elements in array and return it
        return new ArrayList<String>(Arrays.asList(stringWithRemoval.split(",")));
    }


}
