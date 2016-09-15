package com.bountiedapp.bountied;

import android.app.ProgressDialog;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/***********************************************************************
 *  The Network Request class is build to handle requests other than
 *  uploads or downloads to the server.
 *  All requests are made asynchronously.
 ***********************************************************************/
public class NetworkRequest {

    // Tag used to cancel the request
    private String TAG = "download_bounty_list_request";

    // Download url
    private String mDownloadUrl = null;

    // progress download dialog
    private ProgressDialog mProgressDialog = null;

    // asynchronous request to server to delete a bounty from the database
    public void deleteABounty(final Context context, String url, String bountyID, final PostDeletionInterface postDeletionInterface) throws JSONException {

        // send the bounty id to the server
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("bountyID", bountyID);

        mDownloadUrl = url;

        // start a progress dialog so the user knows something is happening
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // on callback we want to reload the screen without the deleted bounty
                        postDeletionInterface.refresh();
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

    // asynchronous request to server to accept a bounty find
    public void acceptFoundBounty(final Context context, String url, String foundID, final AcceptFoundInterface acceptFoundInterface) throws JSONException {

        // send the found ID to the server
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("foundID", foundID);

        mDownloadUrl = url;

        // start a progress dialog so the user knows something is happening
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // on callback take us back to the placed bounties screen
                        // since we just accepted a bounty, the current screen will
                        // be empty
                        acceptFoundInterface.accept();
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

    // asynchronous request to server to decline a bounty find
    public void declineFoundBounty(final Context context, String url, String foundID, final DeclineFoundInterface declineFoundInterface) throws JSONException {

        // send the found ID to server
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("foundID", foundID);

        mDownloadUrl = url;

        // start a progress dialog so the user knows something is happening
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // callback to refresh the screen without deleted bounty
                        declineFoundInterface.decline();
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


    // the are used to be overridden in the activities in which they are applied
    // they are ways to perform actions in the activities, on callback from these methods
    public interface PostDeletionInterface {
        void refresh();
    }

    public interface AcceptFoundInterface {
        void accept();
    }

    public interface DeclineFoundInterface {
        void decline();
    }
}
