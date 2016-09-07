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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bountiedapp.bountied.NetworkSingleton;
import com.bountiedapp.bountied.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity {

    // endpoint to send information to the server
    private String UPLOAD_URL ="http://192.168.1.8:3000/register";

    // Tag used for network requests
    private String TAG = "registration_upload_request";

    private ProgressDialog mProgressDialog = null;

    // these are static strings used for the intents
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_USERNAME = "EXTRA_USERNAME";
    private static final String EXTRA_PASSWORD = "EXTRA_PASSWORD";

    // UI elements
    private EditText mUsernameTextField;
    private EditText mPasswordTextField;
    private EditText mEmailTextField;
    private Button mRegisterButton;
    private TextView mLoginText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set the view from xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // set the toolbar from xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get a reference to UI elements
        mRegisterButton = (Button)findViewById(R.id.register_submit);
        mLoginText = (TextView) findViewById(R.id.register_login);
        mUsernameTextField = (EditText)findViewById(R.id.register_username);
        mPasswordTextField = (EditText)findViewById(R.id.register_password);
        mEmailTextField = (EditText)findViewById(R.id.register_email);

        // set the registration button to "listen" for clicks
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                // get all current text in the fields
                String userName = mUsernameTextField.getText().toString();
                String password = mPasswordTextField.getText().toString();
                String email = mEmailTextField.getText().toString();

                // validate all the text to the extent that it can be done before the server validates it
                boolean fieldsAreValid = validateFields(userName, password, email);

                // if all fields are valid then send it to the server
                if (fieldsAreValid) {
                    try {
                        registerUser(view.getContext(), userName, password, email);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // else if fields are not valid tell user what they need to do
                else {
                    Toast.makeText(view.getContext(), "Registration is invalid, please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // set the login text to listen for clicks
        mLoginText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // on click go to login activity
                Intent intent = new Intent(view.getContext(), Login.class);
                startActivity(intent);
            }
        });

    }

    // validate the entered username, password, and email entered for registration
    private boolean validateFields(String username, String password, String email) {
        // if the username is left blank
        if (username == "") {
            return false;
        }
        // if the password is less than 7 characters or there are spaces in it
        if (password.length() < 7 || password.indexOf(" ") != -1) {
            return false;
        }
        // verify the email is at least in basically correct format
        if (email.matches("/.+@.+/")) {
            return false;
        }
        return true;
    }

    // the network request contained in this method is called asynchronously
    private void registerUser(final Context context, final String username, final String password, String email) throws JSONException {

        // put contents into Json object to send to server
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("password", password);
        jsonObject.put("email", email);

        // start a new progress indicator for the user
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        // create a new JsonObjectRequest and use the POST method
        // we override the onResponse method in order to implement our own callback
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                UPLOAD_URL, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        String registerResponse = "none";

                        // if registration is successful sign the user in and send them to the home activity
                        try {
                            registerResponse = (String) response.get("registerResponse");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // this will check the response that is sent back from the server
                        // and respond accordingly with a toast message
                        boolean canRegister = checkResponse(context, registerResponse);

                        // if the user can register use intent to enter user information through login screen and login for them
                        if (canRegister) {
                            startLoginActivity(context, username, password);
                        }
                        mProgressDialog.hide();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // else if user registration is not successful do to a network issue tell them why and allow them to re-register
                Toast.makeText(context, "Network Error while registering you, please try again later.", Toast.LENGTH_LONG).show();
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                mProgressDialog.hide();
            }
        });


        // Adding request to request queue in network singleton
        NetworkSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest, TAG);
    }

    // check the server response to see if there is duplicate username, email, or some other error
    private boolean checkResponse(Context context, String registerResponse) {

        if (registerResponse.equals("ERROR_USERNAME")) {
            Toast.makeText(context, "Unable to register, the username is already taken.  Please choose another.", Toast.LENGTH_LONG).show();
            return false;
        }
        else if (registerResponse.equals("ERROR_EMAIL")) {
            Toast.makeText(context, "Unable to register, the email is already taken.  Please choose another.", Toast.LENGTH_LONG).show();
            return false;
        }
        else if (registerResponse.equals("ERROR_OTHER")) {
            Toast.makeText(context, "Unable to register.  Please try again.", Toast.LENGTH_LONG).show();
            return false;
        }
        else {
            Toast.makeText(context, "You have been registered.", Toast.LENGTH_LONG).show();
            return true;
        }

    }

    // this will send the user to the login activity and
    // start the process of automatically logging the user in since they just registered
    private void startLoginActivity(Context context, String username, String password) {

        Intent intent = new Intent(context, Login.class);

        // put the username and password information in the bundle to send to the next activity
        Bundle extras = new Bundle();
        extras.putString(EXTRA_USERNAME, username);
        extras.putString(EXTRA_PASSWORD, password);

        // add the extras to the intent
        intent.putExtra(BUNDLE_EXTRAS, extras);
        startActivity(intent);
    }

}
