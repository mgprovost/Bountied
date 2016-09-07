package com.bountiedapp.bountied.model;

/**
 * Created by mprovost on 6/28/2016.
 */
public class Bounty {

    private String title;
    private String description;
    private String bounty;
    private String radius;
    private String category;
    private String lat;
    private String lng;
    private String imageAsString;
    private String username;
    private String placerID;


    public Bounty(String title, String description, String bounty, String radius, String category,
                  String lat, String lng, String imageAsString, String username, String placerID) {
        this.title = title;
        this.description = description;
        this.bounty = bounty;
        this.radius = radius;
        this.category = category;
        this.lat = lat;
        this.lng = lng;
        this.imageAsString = imageAsString;
        this.username = username;
        this.placerID = placerID;
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

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public String getCatagory() { return category; }

    public void setCatagory(String catagory) { this.category = catagory; }

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

    public String getImageAsString() {
        return imageAsString;
    }

    public void setImageAsString(String imageAsString) {
        this.imageAsString = imageAsString;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getPlacerID() { return placerID; }

    public void setPlacerID(String placerID) { this.placerID = placerID; }

}
