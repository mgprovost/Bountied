package com.bountiedapp.bountied;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mprovost on 7/25/2016.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private final String URL = "http://192.168.1.6:3000/updatetoken/";
    private final String idFileName = "ID";


    // this is only call called if the token needs updating
    // it is called automatically by the application
    @Override
    public void onTokenRefresh() {

        InternalReader internalReader = new InternalReader(this);
        InternalWriter internalWriter = new InternalWriter(this);

        // get the token from the firebase
        String token = FirebaseInstanceId.getInstance().getToken();

        // write token to internal memory
        internalWriter.writeToMemory("token", token);

        // read the id that has been saved in internal memory and save it in variable
        String id = internalReader.readFromFile(idFileName);

        // if the id returned from the file isn't blank
        // which can happen if the user hasn't registered yet
        // then save it to the database
        if (!id.equals("")) {
            // updates the token of a user, by id number, on the server side
            updateToken(this, URL, token, id);
        }

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + token);
    }

    private void writeTokenToInternalMemory(String token) {
        writeToFile("token.txt", token);
    }

    private void writeToFile(String filename, String message) {
        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            fileOutputStream.write(message.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // probably need a function to say ok... if the token has been refreshed
    // ... simply update the token associated with with this user
    // then place it in onTokenRefresh

    private static void updateToken(Context context, final String URL, final String TOKEN, final String ID){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // response
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("id", ID);
                params.put("token", TOKEN);

                return params;
            }
        };
        NetworkSingleton.getInstance(context).addToRequestQueue(stringRequest, TAG);
    }


    // get the id of this user
    // read a string of data from a file saved in internal memory
    private String readFromFile(String filename) {

        String ret = "";

        try {
            InputStream inputStream = openFileInput(filename + ".txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

}

