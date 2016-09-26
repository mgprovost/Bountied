package com.bountiedapp.bountied;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bountiedapp.bountied.model.FoundBounty;
import com.bountiedapp.bountied.model.Gps;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class Button {

    // constant used when returning to the activity from the camera intent
    private int REQUEST_IMAGE_CAPTURE = 1;

    // keeps track of the temp file where image is stored
    private String mImageLocation;

    public Button() {
        mImageLocation = null;
    }

    // starts the camera activity while giving it a temp file to assign the return image to
    public void startCameraIntent(Activity activity) {

        // create a new intent and tell it what type of intent to be
        Intent cameraApplicationIntent = new Intent();
        cameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        // create the temp file
        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // asign the temp file to the intent and start
        cameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        activity.startActivityForResult(cameraApplicationIntent, REQUEST_IMAGE_CAPTURE);
    }

    // create a temp file
    public File createImageFile() throws IOException {

        // create the filename and make it unique by using the data and time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);

        // return the path/location of the temp file
        mImageLocation = image.getAbsolutePath();
        return image;
    }


    // take a photo from a temp file, reduce the size, rotate the image,
    // compress it, and convert the image to a base64 string
    // then upload it to the database, along with other info like the lat and lng, etc...
    public void uploadCameraPicture(Context context, String placerID, String bountyID) {

        // start the gps listening
        Gps gps = new Gps(context);

        // take the image from the temp file and convert to bitmap
        // then reduce the size, rotate it if it is necessary, and compress it
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
        String lat = Double.toString(gps.getLat());
        String lng = Double.toString(gps.getLng());

        // stop gps from listening for new locations
        gps.stopGps(context);

        FoundBounty foundBounty = new FoundBounty(placerID, bountyID, id, imageAsString, lat, lng);

        // upload the bounty asynchronously
        Upload upload = new Upload();

        try {
            upload.bountyFound(context, foundBounty);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // save a bounty id to internal memory
    public void saveBounty(Context context, String bountyID) {

        // create an internal reader and writer of internal memory
        InternalReader internalReader = new InternalReader(context);
        InternalWriter internalWriter = new InternalWriter(context);

        // save the current bounty id to bounties_to_hunt internal storage
        // specifically just save the id of the bounty, the data will be
        // pulled from the server when user accesses that activity
        String filename = "bounties_to_hunt";

        // get the ids that are stored as a string
        String savedIds = internalReader.readFromFile(filename);

        // if there are no saved bounties
        if (savedIds.equals("") || savedIds.equals("[]")) {

            ArrayList<String> savedIdsArray = new ArrayList<String>();
            savedIdsArray.add(bountyID);
            internalWriter.writeToMemory(filename, savedIdsArray.toString());
            Toast.makeText(context, "Bounty has been saved to your hunts-in-progress", Toast.LENGTH_LONG).show();
        }
        // else if the bounty the user is trying to save is already in the saved hunts
        else if(savedIds.contains(bountyID)) {
            Toast.makeText(context, "This bounty is already saved to your hunts-in-progress", Toast.LENGTH_LONG).show();
        }
        // else there are bounties in there and need to add one more to it
        else {
            ArrayList<String> savedIdsArray = getArrayFromString(savedIds);
            savedIdsArray.add(bountyID);
            internalWriter.writeToMemory(filename, "");
            internalWriter.writeToMemory(filename, savedIdsArray.toString());
            Toast.makeText(context, "Bounty has been saved to your hunts-in-progress", Toast.LENGTH_LONG).show();
        }
    }

    // deletes a bounty from the list of internally saved ids
    // to be used when deleting a bounty from hunts in progress
    public void deleteBounty(Context context, String bountyID) {

        // create an internal reader and writer of internal memory
        InternalReader internalReader = new InternalReader(context);
        InternalWriter internalWriter = new InternalWriter(context);

        // internal area where the bounty iDs are stored
        String filename = "bounties_to_hunt";

        // get the savedID's as a string
        String savedIds = internalReader.readFromFile(filename);

        // get an array of the bounty ids from the string
        ArrayList<String> savedIdsArray = getArrayFromString(savedIds);

        // find where the bounty is in the array and remove it from array
        int indexOfBountyToDelete = savedIdsArray.indexOf(bountyID);
        savedIdsArray.remove(indexOfBountyToDelete);

        // write the bounties left to memory
        internalWriter.writeToMemory(filename, savedIdsArray.toString());
        Toast.makeText(context, "Bounty has been deleted from your hunts-in-progress", Toast.LENGTH_LONG).show();
    }


    // get an array list from a string
    // this will return an arraylist from the string format [ string, string, string, ]
    private ArrayList getArrayFromString(String someString) {

        // remove the left bracket, then right bracket, then all spaces from the string
        String removeLeftBracket = someString.replace("[", "");
        String removeRightBracket = removeLeftBracket.replace("]", "");
        String stringWithRemoval = removeRightBracket.replace(" ", "");

        // split the string on all "," in it and then make
        // each of those strings elements in array and return it
        return new ArrayList<String>(Arrays.asList(stringWithRemoval.split(",")));
    }

    // will return the location of the image
    public String getmImageLocation() {
        return mImageLocation;
    }


}
