package com.bountiedapp.bountied.model;

/**
 * Created by mprovost on 8/26/2016.
 */

/****************************************************
 *    The BountyFoundListItem class is just a simple
 *    POJO class used to pass data with network
 *    calls mostly.
 ****************************************************/

public class BountyFoundListItem {

    private String foundID;
    private String imageURL;

    public BountyFoundListItem() {}

    public String getFoundID() {
        return foundID;
    }

    public void setFoundID(String foundID) {
        this.foundID = foundID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

}
