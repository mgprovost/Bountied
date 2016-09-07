package com.bountiedapp.bountied.model;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by mprovost on 6/15/2016.
 *
 * Java respresentation of our data to be displayed in RecyclerView
 */
public class BountyHuntListItem {

    private String mTitle;
    private String mDescription;
    private String mBounty;
    private String mImageUrl;
    private String mPlacerID;
    private String mLat;
    private String mLng;
    private String foundLat;
    private String foundLng;

    public BountyHuntListItem() {
        mTitle = null;
        mDescription = null;
        mBounty = null;
        mImageUrl = null;
        mPlacerID = null;
        mLat = null;
        mLng = null;
        foundLat = null;
        foundLng = null;
    }


    private ArrayList foundIDS;

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getmBounty() {
        return mBounty;
    }

    public void setmBounty(String mBounty) {
        this.mBounty = mBounty;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    public String getmPlacerID() { return mPlacerID; }

    public void setmPlacerID(String mPlacerID) { this.mPlacerID = mPlacerID; }

    public String getmLat() { return mLat; }

    public void setmLat(String mLat) { this.mLat = mLat; }

    public String getmLng() { return mLng; }

    public void setmLng(String mLng) { this.mLng = mLng; }

    public String getFoundLat() { return foundLat; }

    public void setFoundLat(String foundLat) { this.foundLat = foundLat; }

    public String getFoundLng() { return foundLng; }

    public void setFoundLng(String foundLng) { this.foundLng = foundLng; }


    public ArrayList getFoundIDS() { return foundIDS; }

    public void setFoundIDS(String foundIDS) {
        if (foundIDS.equals("")) {
            this.foundIDS = new ArrayList();
        }
        else {
            this.foundIDS = getArrayFromString(foundIDS);
        }
    }

    @Override
    public String toString() {
        return  "Title: " + mTitle +
                " Description: " + mDescription +
                " Bounty: " + mBounty +
                " Lat: " + mLat +
                " Lng: " + mLng +
                " BountyID: " + mImageUrl +
                " PlacerID: " + mPlacerID;
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
