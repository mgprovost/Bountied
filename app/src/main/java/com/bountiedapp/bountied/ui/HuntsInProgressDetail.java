package com.bountiedapp.bountied.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.bountiedapp.bountied.*;
import com.squareup.picasso.Picasso;

public class HuntsInProgressDetail extends AppCompatActivity implements View.OnClickListener {

    // endpoint to get image data from the server
    private static final String BOUNTY_IMAGES_BASE_URL = "http://192.168.1.8:3000/images/";

    // constant used when returning to the activity from the camera intent
    private int REQUEST_IMAGE_CAPTURE = 1;

    // these are just static strings used for the receiving of
    // data from the hunts in progress activity to this detail activity
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    private static final String EXTRA_BOUNTY = "EXTRA_BOUNTY";
    private static final String EXTRA_IMAGEURL = "EXTRA_IMAGEURL";
    private static final String EXTRA_PLACERID = "EXTRA_PLACERID";

    // functionality associated with the camera/hunt button
    private com.bountiedapp.bountied.Button mHuntButton;

    // UI element camera floating action button
    private FloatingActionButton mCameraFab;

    private String mBountyId;
    private String mPlacerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set the view from xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunts_in_progress_detail);

        // set the toolbar from xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get reference to camera fab, then set the
        // click listener to be handled by this activity
        mCameraFab = (FloatingActionButton)findViewById(R.id.fab_hunt);
        mCameraFab.setOnClickListener(this);

        // get the bundled extras from previous activity
        Bundle extras = getIntent().getBundleExtra(BUNDLE_EXTRAS);
        String imageUrl = extras.getString(EXTRA_IMAGEURL) + ".jpg";
        mBountyId = extras.getString(EXTRA_IMAGEURL);
        mPlacerId = extras.getString(EXTRA_PLACERID);

        // create url of where the image is currently available
        Uri uri = Uri.parse(BOUNTY_IMAGES_BASE_URL + imageUrl);

        // get a reference to the imageview, needs to final
        ImageView imageView = (ImageView)findViewById(R.id.hunts_in_progress_detail_image);

        // load image from server into imageview, crop it, and make it fit
        Picasso.with(this).load(uri).fit().centerCrop().into(imageView);

        // set title, description, and bounty views from the bundled extras
        ((TextView)findViewById(R.id.hunts_in_progress_detail_title)).setText(extras.getString(EXTRA_TITLE));
        ((TextView)findViewById(R.id.hunts_in_progress_detail_description)).setText(extras.getString(EXTRA_DESCRIPTION));
        ((TextView)findViewById(R.id.hunts_in_progress_detail_bounty)).setText("$" + extras.getString(EXTRA_BOUNTY));

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

        // the following intents serve to start different activities as buttons are pressed
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

    // handle the clicking of the camera/hunt fab button here
    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id){
            case R.id.fab_hunt:

                mHuntButton = new com.bountiedapp.bountied.Button();
                mHuntButton.startCameraIntent(this);
                break;
        }
    }


    // handle the returning from the camera activity here
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