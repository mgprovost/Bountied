package com.bountiedapp.bountied.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.bountiedapp.bountied.ImageConverter;
import com.bountiedapp.bountied.InternalReader;
import com.bountiedapp.bountied.InternalWriter;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.Upload;
import com.bountiedapp.bountied.model.FoundBounty;
import com.bountiedapp.bountied.model.Gps;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HuntsInProgressDetail extends AppCompatActivity implements View.OnClickListener {

    // these are just static strings used for the intents
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    private static final String EXTRA_BOUNTY = "EXTRA_BOUNTY";
    private static final String EXTRA_IMAGEURL = "EXTRA_IMAGEURL";
    private static final String EXTRA_PLACERID = "EXTRA_PLACERID";
    private static final String EXTRA_LAT = "EXTRA_LAT";
    private static final String EXTRA_LNG = "EXTRA_LNG";

    private Button mHuntButton;

    private int REQUEST_IMAGE_CAPTURE = 1;

    private String mImageLocation;

    private static final String BOUNTY_IMAGES_BASE_URL = "http://192.168.1.8:3000/images/";

    private FloatingActionButton mCameraFab;


    private String mBountyId;
    // need to get the following
    private String mPlacerId;
    private String mLat;
    private String mLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunts_in_progress_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCameraFab = (FloatingActionButton)findViewById(R.id.fab_hunt);
        mCameraFab.setOnClickListener(this);

        Bundle extras = getIntent().getBundleExtra(BUNDLE_EXTRAS);

        String imageUrl = extras.getString(EXTRA_IMAGEURL) + ".jpg";
        mBountyId = extras.getString(EXTRA_IMAGEURL);
        mPlacerId = extras.getString(EXTRA_PLACERID);

//        mLat = extras.getString(EXTRA_LAT);
//        mLng = extras.getString(EXTRA_LNG);

        System.out.println("BOUNTY ID:    "  + mBountyId);
        System.out.println("Home Detail extras:  " + extras.toString());


        System.out.println("Image URL:  " + imageUrl);



        Uri uri = Uri.parse(BOUNTY_IMAGES_BASE_URL + imageUrl);

        final ImageView imageView = (ImageView)findViewById(R.id.detail_image);

        Picasso.with(this).load(uri).fit().centerCrop().into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                System.out.println("HEIGHT: " + imageView.getDrawable().getIntrinsicHeight());
                System.out.println("WIDTH: " + imageView.getDrawable().getIntrinsicWidth());
            }

            @Override
            public void onError() {

            }
        });


        ((TextView)findViewById(R.id.detail_title)).setText(extras.getString(EXTRA_TITLE));
        ((TextView)findViewById(R.id.detail_description)).setText(extras.getString(EXTRA_DESCRIPTION));

    }

    // creates the options menu of icons in the upper right hand corner
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    // controls what happens when the icons in the menu (upper right hand corner) are clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                finish();
                return true;
            // Launch the correct Activity here
            case R.id.action_place:
                Intent placeIntent = new Intent(this, PlaceBounty.class);
                startActivity(placeIntent);
                return true;
            case R.id.action_hunt:
                Intent huntIntent = new Intent(this, BountyHuntActivity.class);
                startActivity(huntIntent);
                return true;
            case R.id.action_placed:
                Intent placedIntent = new Intent(this, BountiesPlaced.class);
                startActivity(placedIntent);
                return true;
            case R.id.action_hunts:
                Intent huntsIntent = new Intent(this, HuntsInProgress.class);
                startActivity(huntsIntent);
                return true;
            case R.id.action_accepted:
                Intent acceptedIntent = new Intent(this, BountiesAccepted.class);
                startActivity(acceptedIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id){
            case R.id.fab_hunt:

                mHuntButton = new Button();
                mHuntButton.startCameraIntent(this);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // upload the picture just taken from the camera to the server
            mHuntButton.uploadCameraPicture(this, mPlacerId, mBountyId);

            // since this bountyID is saved in internal memory as a hunt in progress
            // need to go into internal memory and erase it
            mHuntButton.deleteBounty(this, mBountyId);

            // restart the activity to see updated list
            Intent huntsIntent = new Intent(this, HuntsInProgress.class);
            startActivity(huntsIntent);

        }

    }

}