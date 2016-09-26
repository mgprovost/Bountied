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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.bountiedapp.bountied.model.Bounty;
import com.bountiedapp.bountied.model.FoundBounty;
import com.bountiedapp.bountied.model.StaticStrings;

/***************************************************************************
 * Upload class is specifically created for uploading data to the server
 * All requests are asynchronous.
 ***************************************************************************/
public class Upload extends Application {

    // endpoints on the server where a bounty can be placed or a possible find can be placed
    private String BOUNTY_PLACED_URL = StaticStrings.BASE_URL + "placebounty";
    private String FOUND_BOUNTY_URL = StaticStrings.BASE_URL + "foundbounty";

    // Tag used to cancel the request
    private String TAG = "bounty_upload_request";

    // still need to add id, lat, lng, radius, etc here...
    private ProgressDialog progressDialog;

    // ctor
    public Upload() {
        progressDialog = null;
    }

    // send a bounty to place to the server asynchronously
    public void bountyPlaced(Context context, Bounty bounty) throws JSONException {

        // load up all info to send
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", bounty.getTitle());
        jsonObject.put("description", bounty.getDescription());
        jsonObject.put("bounty", bounty.getBounty());
        jsonObject.put("radius", bounty.getRadius());
        jsonObject.put("category", bounty.getCatagory());
        jsonObject.put("image", bounty.getImageAsString());
        jsonObject.put("lat", bounty.getLat());
        jsonObject.put("lng", bounty.getLng());
        jsonObject.put("username", bounty.getUsername());
        jsonObject.put("placerID", bounty.getPlacerID());

        // show the user a progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                BOUNTY_PLACED_URL, jsonObject,
                new Response.Listener<JSONObject>() {
                    // callback
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        progressDialog.hide();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                progressDialog.hide();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }

        };
        // Adding request to request queue
        NetworkSingleton.getInstance(context).addToRequestQueue(jsonObjReq, TAG);
    }

    // send a possible find to the server asynchronously
    public void bountyFound(Context context, FoundBounty foundBounty) throws JSONException {

        // load all info to send to server
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("placerID", foundBounty.getPlacerID());
        jsonObject.put("bountyID", foundBounty.getBountyID());
        jsonObject.put("finderID", foundBounty.getFinderID());
        jsonObject.put("image", foundBounty.getImageAsString());
        jsonObject.put("lat", foundBounty.getLat());
        jsonObject.put("lng", foundBounty.getLng());

        // show user progress dialog to show something is happening
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                FOUND_BOUNTY_URL, jsonObject,
                new Response.Listener<JSONObject>() {
                    // callback
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        progressDialog.hide();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                progressDialog.hide();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }

        };
        // Adding request to request queue
        NetworkSingleton.getInstance(context).addToRequestQueue(jsonObjReq, TAG);
    }


}
