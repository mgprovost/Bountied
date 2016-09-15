package com.bountiedapp.bountied;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bountiedapp.bountied.model.BountyFoundListItem;
import com.bountiedapp.bountied.model.BountyHuntListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/******************************************************************************
 * Download class is specifically created for downloading data from the server
 * All requests are asynchronous.
 ******************************************************************************/

public class DownloadBountyList extends Application {

    // Tag used to cancel the request
    private String TAG = "download_bounty_list_request";

    // Download url
    private String mDownloadUrl;

    // progress download dialog
    private ProgressDialog mProgressDialog;

    // list of bounties downloaded from the server in JSON format without images
    ArrayList<BountyHuntListItem> mBountyList;
    ArrayList<BountyFoundListItem> mFoundList;

    // constructor to initialize the empty bounty lists
    public DownloadBountyList() {
        mBountyList = new ArrayList<BountyHuntListItem>();
        mFoundList = new ArrayList<BountyFoundListItem>();
        mDownloadUrl = null;
        mProgressDialog = null;
    };

    // asynchronously download a list of bounties
    // can specify a category to only pull bounties of a certain category
    public void download(final Context context, String url, String category, String latitude, String longitude, final VolleyCallback callback) throws JSONException {

        // place a category, lat, and lng to send to server in a json object
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("category", category);
        jsonObject.put("lat", latitude);
        jsonObject.put("lng", longitude);

        // get the server endpoint to send/get data to/from
        mDownloadUrl = url;

        // show a progress icon to user
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        // create the network request
        // specify a callback, so as to do something with data after we receive it
        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // callback
                        try {

                            // the bounties are stored in a JSON array currently in string format
                            JSONArray bounties = response.getJSONArray("bounties");

                            // add all the bounties returned in the JSON array to the bounty list
                            for (int i = 0; i < bounties.length(); i++) {

                                // single bounty hunt list item
                                BountyHuntListItem bountyHuntListItem = new BountyHuntListItem();

                                // get a single bounty returned as a JSON object
                                JSONObject bounty = bounties.getJSONObject(i);

                                // add title, description, bounty, and imageUrl to a bountyHuntListItem to fill array
                                bountyHuntListItem.setTitle(bounty.get("title").toString());
                                bountyHuntListItem.setDescription(bounty.get("description").toString());
                                bountyHuntListItem.setBounty(bounty.get("bounty").toString());
                                bountyHuntListItem.setImageUrl(bounty.get("imageUrl").toString());
                                bountyHuntListItem.setPlacerID(bounty.get("placerID").toString());

                                // add the single bounty to the list of bounties
                                mBountyList.add(bountyHuntListItem);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callback.onSuccess(mBountyList);
                        mProgressDialog.hide();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                mProgressDialog.hide();
            }
        });

        // Adding request to request queue
        NetworkSingleton.getInstance(context).addToRequestQueue(jsonObjReq, TAG);
    }

    // asynchronously download a list of bounties
    // specify a list of bountyID's and this will pull all the bounty data that match those ID's
    public void downloadInProgress(final Context context, String url, ArrayList bountyIDs, String latitude, String longitude, final VolleyCallback callback) throws JSONException {

        // place the ID's, lat, and lng to send to the server
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("bountyIDs", bountyIDs);
        jsonObject.put("lat", latitude);
        jsonObject.put("lng", longitude);

        // the url to download from
        mDownloadUrl = url;

        // start the progress icon to show user something is happening
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        // create the network request
        // specify a callback, so as to do something with data after we receive it
        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // callback
                        try {

                            // the bounties are stored in a JSON array currently in string format
                            JSONArray bounties = response.getJSONArray("bounties");

                            // add all the bounties returned in the JSON array to the bounty list
                            for (int i = 0; i < bounties.length(); i++) {

                                // single bounty hunt list item
                                BountyHuntListItem bountyHuntListItem = new BountyHuntListItem();


                                // get a single bounty returned as a JSON object
                                JSONObject bounty = bounties.getJSONObject(i);

                                // add title, description, bounty, and imageUrl to a bountyHuntListItem to fill array
                                bountyHuntListItem.setTitle(bounty.get("title").toString());
                                bountyHuntListItem.setDescription(bounty.get("description").toString());
                                bountyHuntListItem.setBounty(bounty.get("bounty").toString());
                                bountyHuntListItem.setImageUrl(bounty.get("imageUrl").toString());
                                bountyHuntListItem.setPlacerID(bounty.get("placerID").toString());

                                // add the single bounty to the list of bounties
                                mBountyList.add(bountyHuntListItem);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callback.onSuccess(mBountyList);
                        mProgressDialog.hide();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                mProgressDialog.hide();
            }
        });
        // Adding request to request queue
        NetworkSingleton.getInstance(context).addToRequestQueue(jsonObjReq, TAG);
    }

    // asynchronously download a list of possible bounty finds
    // specify the ID of the user placing the bounty
    // and this will pull all the data of bounties that match that ID
    public void downloadPlaced(final Context context, String url, String placerID, final VolleyCallback callback) throws JSONException {

        // add the user ID to send to server
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("placerID", placerID);

        // download url
        mDownloadUrl = url;

        // start the progress icon to show the user something is happening
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        // create the network request
        // specify a callback, so as to do something with data after we receive it
        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // callback
                        try {

                            // the bounties are stored in a JSON array currently in string format
                            JSONArray bounties = response.getJSONArray("bounties");

                            // add all the bounties returned in the JSON array to the bounty list
                            for (int i = 0; i < bounties.length(); i++) {

                                // single bounty hunt list item
                                BountyHuntListItem bountyHuntListItem = new BountyHuntListItem();

                                // get a single bounty returned as a JSON object
                                JSONObject bounty = bounties.getJSONObject(i);

                                // add title, description, bounty, and imageUrl to a bountyHuntListItem to fill array
                                bountyHuntListItem.setTitle(bounty.get("title").toString());
                                bountyHuntListItem.setDescription(bounty.get("description").toString());
                                bountyHuntListItem.setBounty(bounty.get("bounty").toString());
                                bountyHuntListItem.setImageUrl(bounty.get("imageUrl").toString());
                                bountyHuntListItem.setPlacerID(bounty.get("placerID").toString());
                                bountyHuntListItem.setFoundIDS(bounty.get("found").toString());

                                // add the single bounty to the list of bounties
                                mBountyList.add(bountyHuntListItem);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callback.onSuccess(mBountyList);
                        mProgressDialog.hide();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                mProgressDialog.hide();
            }
        });

        // Adding request to request queue
        NetworkSingleton.getInstance(context).addToRequestQueue(jsonObjReq, TAG);
    }

    // asynchronously download a list of bounties
    // specify the ID's of possible finds for a single bounty
    // and this will pull all the data of finds that match a certain bounty
    public void downloadBountiesFound(final Context context, String url, ArrayList foundIDS, final VolleyCallbackTwo callback) throws JSONException {

        // put an array of found ID's to send to server
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("foundIDS", foundIDS);

        // download url
        mDownloadUrl = url;

        // start progress icon to show user something is happening
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        // create the network request
        // specify a callback, so as to do something with data after we receive it
        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // callback
                        try {

                            // the finds are stored in a JSON array currently in string format
                            JSONArray bounties = response.getJSONArray("bounties");

                            // add all the finds returned in the JSON array to the bounty list
                            for (int i = 0; i < bounties.length(); i++) {

                                // single bounty found list item
                                BountyFoundListItem bountyFoundListItem = new BountyFoundListItem();

                                // get a single bounty returned as a JSON object
                                JSONObject bounty = bounties.getJSONObject(i);

                                // add the foundID and the imageURL to a list
                                bountyFoundListItem.setFoundID(bounty.get("foundID").toString());
                                bountyFoundListItem.setImageURL(bounty.get("imageURL").toString());

                                // add the single bounty to the list of bounties
                                mFoundList.add(bountyFoundListItem);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callback.onFinish(mFoundList);
                        mProgressDialog.hide();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                mProgressDialog.hide();
            }
        });
        // Adding request to request queue
        NetworkSingleton.getInstance(context).addToRequestQueue(jsonObjReq, TAG);
    }


    // asynchronously download a list of bounty finds accepted
    // specify the ID's of finds a user has accepted for bounties placed
    // and this will pull all the data of the finds
    public void downloadAccepted(final Context context, String url, String placerID, final VolleyCallback callback) throws JSONException {

        // put the user ID to send to server
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("placerID", placerID);

        // download url
        mDownloadUrl = url;

        // start the progress icon to show user something is happening
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        // create the network request
        // specify a callback, so as to do something with data after we receive it
        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // callback
                        try {

                            // the finds are stored in a JSON array currently in string format
                            JSONArray bounties = response.getJSONArray("bounties");

                            // add all the finds returned in the JSON array to the bounty list
                            for (int i = 0; i < bounties.length(); i++) {

                                // single bounty hunt list item
                                BountyHuntListItem bountyHuntListItem = new BountyHuntListItem();

                                // get a single bounty returned as a JSON object
                                JSONObject bounty = bounties.getJSONObject(i);

                                // add title, description, bounty, and imageUrl to a bountyHuntListItem to fill array
                                bountyHuntListItem.setTitle(bounty.get("title").toString());
                                bountyHuntListItem.setDescription(bounty.get("description").toString());
                                bountyHuntListItem.setBounty(bounty.get("bounty").toString());
                                bountyHuntListItem.setImageUrl(bounty.get("imageUrl").toString());
                                bountyHuntListItem.setFoundLat(bounty.get("foundLat").toString());
                                bountyHuntListItem.setFoundLng(bounty.get("foundLng").toString());
                                bountyHuntListItem.setPlacerID(bounty.get("placerID").toString());

                                // add the single bounty to the list of bounties
                                mBountyList.add(bountyHuntListItem);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callback.onSuccess(mBountyList);
                        mProgressDialog.hide();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                mProgressDialog.hide();
            }
        });

        // Adding request to request queue
        NetworkSingleton.getInstance(context).addToRequestQueue(jsonObjReq, TAG);
    }


    // callback interfaces that are used so they can be overridden in the activities
    // this way the activities can use the data that's returned here
    public interface VolleyCallback{
        void onSuccess(ArrayList<BountyHuntListItem> result);
    }

    public interface VolleyCallbackTwo {
        void onFinish(ArrayList<BountyFoundListItem> result);
    }

}