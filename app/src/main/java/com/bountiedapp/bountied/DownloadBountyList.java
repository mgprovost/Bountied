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
import com.bountiedapp.bountied.model.Bounty;
import com.bountiedapp.bountied.model.BountyFoundListItem;
import com.bountiedapp.bountied.model.BountyHuntListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mprovost on 6/27/2016.
 */
public class DownloadBountyList extends Application {

    // Tag used to cancel the request
    private String TAG = "download_bounty_list_request";

    // Download url
    private String mDownloadUrl = null;

    // progress download dialog
    private ProgressDialog mProgressDialog = null;

    // list of bounties downloaded from the server in JSON format
    // no images in here
    ArrayList<BountyHuntListItem> mBountyList;
    ArrayList<BountyFoundListItem> mFoundList;

    // constructor to initialize the empty bounty list
    public DownloadBountyList() {
        mBountyList = new ArrayList<BountyHuntListItem>();
        mFoundList = new ArrayList<BountyFoundListItem>();
    };

    public void download(final Context context, String url, String category, String latitude, String longitude, final VolleyCallback callback) throws JSONException {

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("category", category);
        jsonObject.put("lat", latitude);
        jsonObject.put("lng", longitude);

        mDownloadUrl = url;

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray bounties = response.getJSONArray("bounties");
                            Log.d(TAG, bounties.toString());

                            // add all the bounties returned in the JSON array to the bounty list
                            for (int i = 0; i < bounties.length(); i++) {

                                // single bounty hunt list item
                                BountyHuntListItem bountyHuntListItem = new BountyHuntListItem();

                                // get a single bounty returned as a JSON object
                                JSONObject bounty = bounties.getJSONObject(i);

                                // add title, description, bounty, and imageUrl to a bountyHuntListItem to fill array
                                bountyHuntListItem.setmTitle(bounty.get("title").toString());
                                bountyHuntListItem.setmDescription(bounty.get("description").toString());
                                bountyHuntListItem.setmBounty(bounty.get("bounty").toString());
                                bountyHuntListItem.setmImageUrl(bounty.get("imageUrl").toString());
//                                bountyHuntListItem.setmLat(bounty.get("lat").toString());
//                                bountyHuntListItem.setmLng(bounty.get("lng").toString());
                                bountyHuntListItem.setmPlacerID(bounty.get("placerID").toString());

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

    public void downloadInProgress(final Context context, String url, ArrayList bountyIDs, String latitude, String longitude, final VolleyCallback callback) throws JSONException {

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("bountyIDs", bountyIDs);
        jsonObject.put("lat", latitude);
        jsonObject.put("lng", longitude);

        mDownloadUrl = url;

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray bounties = response.getJSONArray("bounties");
                            Log.d(TAG, bounties.toString());

                            // add all the bounties returned in the JSON array to the bounty list
                            for (int i = 0; i < bounties.length(); i++) {

                                // single bounty hunt list item
                                BountyHuntListItem bountyHuntListItem = new BountyHuntListItem();



                                // get a single bounty returned as a JSON object
                                JSONObject bounty = bounties.getJSONObject(i);

                                // add title, description, bounty, and imageUrl to a bountyHuntListItem to fill array
                                bountyHuntListItem.setmTitle(bounty.get("title").toString());
                                bountyHuntListItem.setmDescription(bounty.get("description").toString());
                                bountyHuntListItem.setmBounty(bounty.get("bounty").toString());
                                bountyHuntListItem.setmImageUrl(bounty.get("imageUrl").toString());
//                                bountyHuntListItem.setmLat(bounty.get("lat").toString());
//                                bountyHuntListItem.setmLng(bounty.get("lng").toString());
                                bountyHuntListItem.setmPlacerID(bounty.get("placerID").toString());

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

    public void downloadPlaced(final Context context, String url, String placerID, final VolleyCallback callback) throws JSONException {

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("placerID", placerID);

        mDownloadUrl = url;

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray bounties = response.getJSONArray("bounties");
                            Log.d(TAG, bounties.toString());

                            // add all the bounties returned in the JSON array to the bounty list
                            for (int i = 0; i < bounties.length(); i++) {

                                // single bounty hunt list item
                                BountyHuntListItem bountyHuntListItem = new BountyHuntListItem();


                                // get a single bounty returned as a JSON object
                                JSONObject bounty = bounties.getJSONObject(i);

                                // add title, description, bounty, and imageUrl to a bountyHuntListItem to fill array
                                bountyHuntListItem.setmTitle(bounty.get("title").toString());
                                bountyHuntListItem.setmDescription(bounty.get("description").toString());
                                bountyHuntListItem.setmBounty(bounty.get("bounty").toString());
                                bountyHuntListItem.setmImageUrl(bounty.get("imageUrl").toString());
//                                bountyHuntListItem.setmLat(bounty.get("lat").toString());
//                                bountyHuntListItem.setmLng(bounty.get("lng").toString());
                                bountyHuntListItem.setmPlacerID(bounty.get("placerID").toString());

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

    public void downloadBountiesFound(final Context context, String url, ArrayList foundIDS, final VolleyCallbackTwo callback) throws JSONException {

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("foundIDS", foundIDS);

        System.out.println("JSONObject:  " + jsonObject.toString());
        mDownloadUrl = url;

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray bounties = response.getJSONArray("bounties");
                            Log.d(TAG, bounties.toString());

                            // add all the bounties returned in the JSON array to the bounty list
                            for (int i = 0; i < bounties.length(); i++) {

                                // single bounty hunt list item
                                BountyFoundListItem bountyFoundListItem = new BountyFoundListItem();


                                // get a single bounty returned as a JSON object
                                JSONObject bounty = bounties.getJSONObject(i);

                                // add title, description, bounty, and imageUrl to a bountyHuntListItem to fill array
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


    public void downloadAccepted(final Context context, String url, String placerID, final VolleyCallback callback) throws JSONException {

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("placerID", placerID);

        mDownloadUrl = url;

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray bounties = response.getJSONArray("bounties");
                            Log.d(TAG, bounties.toString());

                            // add all the bounties returned in the JSON array to the bounty list
                            for (int i = 0; i < bounties.length(); i++) {

                                // single bounty hunt list item
                                BountyHuntListItem bountyHuntListItem = new BountyHuntListItem();


                                // get a single bounty returned as a JSON object
                                JSONObject bounty = bounties.getJSONObject(i);

                                // add title, description, bounty, and imageUrl to a bountyHuntListItem to fill array
                                bountyHuntListItem.setmTitle(bounty.get("title").toString());
                                bountyHuntListItem.setmDescription(bounty.get("description").toString());
                                bountyHuntListItem.setmBounty(bounty.get("bounty").toString());
                                bountyHuntListItem.setmImageUrl(bounty.get("imageUrl").toString());
                                bountyHuntListItem.setFoundLat(bounty.get("foundLat").toString());
                                bountyHuntListItem.setFoundLng(bounty.get("foundLng").toString());
                                bountyHuntListItem.setmPlacerID(bounty.get("placerID").toString());

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


    public interface VolleyCallback{
        void onSuccess(ArrayList<BountyHuntListItem> result);
    }

    public interface VolleyCallbackTwo {
        void onFinish(ArrayList<BountyFoundListItem> result);
    }




}