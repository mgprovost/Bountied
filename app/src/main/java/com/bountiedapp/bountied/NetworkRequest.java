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

/**
 * Created by mprovost on 8/27/2016.
 */
public class NetworkRequest {

    // Tag used to cancel the request
    private String TAG = "download_bounty_list_request";

    // Download url
    private String mDownloadUrl = null;

    // progress download dialog
    private ProgressDialog mProgressDialog = null;

    public void deleteABounty(final Context context, String url, String bountyID, final PostDeletionInterface postDeletionInterface) throws JSONException {

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("bountyID", bountyID);

        mDownloadUrl = url;

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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


    public void acceptFoundBounty(final Context context, String url, String foundID, final AcceptFoundInterface acceptFoundInterface) throws JSONException {

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("foundID", foundID);

        mDownloadUrl = url;

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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

    public void declineFoundBounty(final Context context, String url, String foundID, final DeclineFoundInterface declineFoundInterface) throws JSONException {

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("foundID", foundID);

        mDownloadUrl = url;

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mDownloadUrl, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
