package com.bountiedapp.bountied.model;

// simple class to hold the data for potential found bounties

public class FoundBounty {

    private String placerID;
    private String bountyID;
    private String finderID;
    private String imageAsString;
    private String lat;
    private String lng;

    public FoundBounty(String placerID, String bountyID, String finderID, String imageAsString, String lat, String lng) {
        this.placerID = placerID;
        this.bountyID = bountyID;
        this.finderID = finderID;
        this.imageAsString = imageAsString;
        this.lat = lat;
        this.lng = lng;
    }

    public String getPlacerID() {
        return placerID;
    }

    public void setPlacerID(String placerID) {
        this.placerID = placerID;
    }

    public String getBountyID() {
        return bountyID;
    }

    public void setBountyID(String bountyID) {
        this.bountyID = bountyID;
    }

    public String getFinderID() {
        return finderID;
    }

    public void setFinderID(String finderID) {
        this.finderID = finderID;
    }

    public String getImageAsString() {
        return imageAsString;
    }

    public void setImageAsString(String imageAsString) {
        this.imageAsString = imageAsString;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return  "PlacerID: " + placerID +
                " BountyID: " + bountyID +
                " FinderID: " + finderID +
                " Lat: " + lat +
                " Lng: " + lng;
    }
}
