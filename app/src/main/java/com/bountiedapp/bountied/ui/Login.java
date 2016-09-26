package com.bountiedapp.bountied.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bountiedapp.bountied.InternalReader;
import com.bountiedapp.bountied.InternalWriter;
import com.bountiedapp.bountied.NetworkSingleton;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.model.StaticStrings;

import org.json.JSONException;
import org.json.JSONObject;


public class Login extends AppCompatActivity {

    // endpoint to send information to the server
    private String UPLOAD_URL = StaticStrings.BASE_URL + "login";

    // tag used for network request
    private String TAG = "registration_upload_request";

    // these are static strings used to get the bundled items
    // if this activity is opened from registration
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_USERNAME = "EXTRA_USERNAME";
    private static final String EXTRA_PASSWORD = "EXTRA_PASSWORD";

    // UI elements
    private EditText mUsernameTextField;
    private EditText mPasswordTextField;
    private TextView mRegisterTextView;
    private Button mLoginButton;

    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set the view from xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // set the toolbar from xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get a reference to UI elements
        mRegisterTextView = (TextView)findViewById(R.id.login_register);
        mLoginButton = (Button)findViewById(R.id.login_submit);
        mUsernameTextField = (EditText)findViewById(R.id.login_username);
        mPasswordTextField = (EditText)findViewById(R.id.login_password);

        // get the username and password as bundled extras
        // if we are coming from the registration activity
        // otherwise you will end up with null in the extras
        Bundle extras = getIntent().getBundleExtra(BUNDLE_EXTRAS);

        // if the user just registered then there will be a username and
        // password bundled in to automatically log that user in
        if (extras != null) {
            // get the username and password passed in
            // and verified from the registration activity
            String username = extras.getString(EXTRA_USERNAME);
            String password = extras.getString(EXTRA_PASSWORD);

            // log the user in
            logUserIn(this, username, password);
        }

        // set the registration text to listen for clicks
        mRegisterTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // on click, go to the registration activity
                Intent intent = new Intent(v.getContext(), Register.class);
                startActivity(intent);
            }
        });

        // set the login button to listen for clicks
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                // on click, get all current text in the fields
                String userName = mUsernameTextField.getText().toString();
                String password = mPasswordTextField.getText().toString();

                // validate all the text fields to the extent that
                // it can be done before the server validates it
                boolean fieldsAreValid = validateFields(userName, password);

                // if all fields are valid then send it to the server
                if (fieldsAreValid) {
                    logUserIn(view.getContext(), userName, password);
                }
                // else if fields are not valid tell user what they need to do
                else {
                    Toast.makeText(view.getContext(), "Login is invalid, please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    // will send an asynchronous network request to the server
    private void logUserIn(final Context context, final String username, final String password) {

        // read the token from internal memory, to send it to the server
        // the token will be used to tell which device the user is on
        // in order to send downstream/firebase messages to it
        InternalReader internalReader = new InternalReader(this);
        String token = internalReader.readFromFile("token");

        // put data in a Json object to send to the server
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
            jsonObject.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // show a progress indicator to the user
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        // create a new JsonObjectRequest and use the POST method
        // we override the onResponse method in order to implement our own callback
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                UPLOAD_URL, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // used to check responses from the server
                        String loginResponse = "none";
                        String idResponse = null;

                        // attempt to get the responses from the server
                        try {
                            loginResponse = (String) response.get("loginResponse");
                            idResponse = (String) response.get("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // this will check the response that is sent back from the server
                        // and respond accordingly with a toast message
                        boolean canLogin = checkResponse(context, loginResponse, idResponse);


                        if (canLogin) {

                            // write the id and username to internal memory for later use
                            InternalWriter internalWriter = new InternalWriter(context);
                            internalWriter.writeToMemory("ID", idResponse);
                            internalWriter.writeToMemory("username", username);

                            // start the home page activity
                            startHomeActivity(context);
                        }
                        mProgressDialog.hide();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // else if user login is not successful because of network error, tell them why
                Toast.makeText(context, "Network Error while logging in, please try again later.", Toast.LENGTH_LONG).show();
                mProgressDialog.hide();
            }
        });

        // Adding request to request queue
        NetworkSingleton.getInstance(context).addToRequestQueue(jsonObjReq, TAG);
    }

    // checks response string, will display toast message with results
    private boolean checkResponse(Context context, String loginResponse, String id) {

        if (loginResponse.equals("SUCCESS")) {
            Toast.makeText(context, "You are now logged in.", Toast.LENGTH_LONG).show();
            return true;
        }
        else {
            Toast.makeText(context, "Unable to login.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void startHomeActivity(Context context) {
        // go to the home activity
        Intent intent = new Intent(context, Home.class);
        startActivity(intent);
    }

    // validate the entered username, password, and email entered for registration
    private boolean validateFields(String username, String password) {
        // if the username is left blank
        if (username == "") {
            return false;
        }
        // if the password is less than 7 characters or there are spaces in it
        if (password.length() < 7 || password.indexOf(" ") != -1) {
            return false;
        }
        return true;
    }

}
