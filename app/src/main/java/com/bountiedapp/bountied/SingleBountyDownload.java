package com.bountiedapp.bountied;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.EventLogTags;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.bountiedapp.bountied.NetworkSingleton;

/**
 * Created by mprovost on 6/13/2016.
 */
public class SingleBountyDownload extends Application {

    // Tag used to cancel the request
    private String TAG = "json_request";
    private String DOWNLOAD_URL = "http://192.168.1.8:3000/singlebountydownload";
    // still need to add id, lat, lng, radius, etc here...
    private ProgressDialog progressDialog = null;

    public SingleBountyDownload(Context context, String id) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);


        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                DOWNLOAD_URL, jsonObject,
                new Response.Listener<JSONObject>() {

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

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Content-Type", "application/json; charset=utf-8");
//                headers.put("User-agent", "My useragent");
//                return headers;
//            }

        };

        // Adding request to request queue
        NetworkSingleton.getInstance(context).addToRequestQueue(jsonObjReq, TAG);
    }
}

