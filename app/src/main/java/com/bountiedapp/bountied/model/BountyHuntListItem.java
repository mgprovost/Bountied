package com.bountiedapp.bountied.model;

import java.util.ArrayList;
import java.util.Arrays;

// Simple class to pass data back and for to the network class and adapters
// holds information having to do with a - bounty hunt -

public class BountyHuntListItem {

    private String title;
    private String description;
    private String bounty;
    private String imageUrl;
    private String placerID;
    private String lat;
    private String lng;
    private String foundLat;
    private String foundLng;
    private ArrayList foundIDS;

    public BountyHuntListItem() {
        title = null;
        description = null;
        bounty = null;
        imageUrl = null;
        placerID = null;
        lat = null;
        lng = null;
        foundLat = null;
        foundLng = null;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getBounty() {

        return bounty;
    }

    public void setBounty(String bounty) {

        this.bounty = bounty;
    }

    public String getImageUrl() {

        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

    public String getPlacerID() {
        return placerID;
    }

    public void setPlacerID(String placerID) {
        this.placerID = placerID;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String mLat) {
        this.lat = mLat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getFoundLat() {
        return foundLat;
    }

    public void setFoundLat(String foundLat) {
        this.foundLat = foundLat;
    }

    public String getFoundLng() {
        return foundLng;
    }

    public void setFoundLng(String foundLng) {
        this.foundLng = foundLng;
    }

    public ArrayList getFoundIDS() {
        return foundIDS;
    }

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
        return  "Title: " + title +
                " Description: " + description +
                " Bounty: " + bounty +
                " Lat: " + lat +
                " Lng: " + lng +
                " BountyID: " + imageUrl +
                " PlacerID: " + placerID;
    }

    // get an array list from a string
    private ArrayList getArrayFromString(String someString) {

        // remove the left bracket, then right bracket, then all spaces from the string
        String removeLeftBracket = someString.replace("[", "");
        String removeRightBracket = removeLeftBracket.replace("]", "");
        String stringWithRemoval = removeRightBracket.replace(" ", "");

        // split the string on all "," in it and then make each
        // of those strings elements in array and return it
        return new ArrayList<String>(Arrays.asList(stringWithRemoval.split(",")));
    }

}
