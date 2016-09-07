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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.bountiedapp.bountied.R;
import com.squareup.picasso.Picasso;


public class HomeDetailActivity extends AppCompatActivity implements View.OnClickListener {

    // endpoint to send information to the server
    private static final String BOUNTY_IMAGES_BASE_URL = "http://192.168.1.8:3000/images/";

    // constant used when returning to the activity from the camera intent
    private int REQUEST_IMAGE_CAPTURE = 1;

    // these are just static strings used to get information from previous activity
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    private static final String EXTRA_BOUNTY = "EXTRA_BOUNTY";
    private static final String EXTRA_IMAGEURL = "EXTRA_IMAGEURL";
    private static final String EXTRA_PLACERID = "EXTRA_PLACERID";
    private static final String EXTRA_LAT = "EXTRA_LAT";
    private static final String EXTRA_LNG = "EXTRA_LNG";

    // floating action button elements and animation elements for the fab
    private FloatingActionButton mFabOpen;
    private FloatingActionButton mFabSave;
    private FloatingActionButton mFabHunt;
    private Boolean isFabOpen = false;
    private Animation fab_open;
    private Animation fab_close;
    private Animation rotate_forward;
    private Animation rotate_backward;


    private String mBountyId;
    private String mPlacerId;
    private String mImageLocation;
    private String mLat;
    private String mLng;

    // used with the fab, with the camera, and on return from the camera
    private Button huntButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set the view from xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_detail);

        // set the toolbar from xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get references to all fabs
        mFabOpen = (FloatingActionButton)findViewById(R.id.fab_open);
        mFabSave = (FloatingActionButton)findViewById(R.id.fab_save);
        mFabHunt = (FloatingActionButton)findViewById(R.id.fab_hunt);

        // get references to all fab animations
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);

        // allow the fabs to be clicked and handled by this activity
        mFabOpen.setOnClickListener(this);
        mFabSave.setOnClickListener(this);
        mFabHunt.setOnClickListener(this);

        // get all extras that came from previous activity
        Bundle extras = getIntent().getBundleExtra(BUNDLE_EXTRAS);

        // get the image url to set the image on screen with picasso
        String imageUrl = extras.getString(EXTRA_IMAGEURL) + ".jpg";

        // get bountyID and placerID from previous activity for later use
        mBountyId = extras.getString(EXTRA_IMAGEURL);
        mPlacerId = extras.getString(EXTRA_PLACERID);

//        mLat = extras.getString(EXTRA_LAT);
//        mLng = extras.getString(EXTRA_LNG);

        // create a uri from the base url defined on top of page and the unique imageurl
        Uri uri = Uri.parse(BOUNTY_IMAGES_BASE_URL + imageUrl);

        // get a reference to the imageview to put image associated with the bounty in
        final ImageView imageView = (ImageView)findViewById(R.id.detail_image);

        // use picasso to download the image from the uri provided
        Picasso.with(this).load(uri).fit().centerCrop().into(imageView);

        // get the title, description, and bounty price from
        // the previous activity and set into their respective views
        ((TextView)findViewById(R.id.detail_title)).setText(extras.getString(EXTRA_TITLE));
        ((TextView)findViewById(R.id.detail_description)).setText(extras.getString(EXTRA_DESCRIPTION));
        ((TextView)findViewById(R.id.detail_bounty)).setText("$" + extras.getString(EXTRA_BOUNTY));

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


    // handles all clicks on the floating action buttons
    @Override
    public void onClick(View view) {

        int id = view.getId();

        switch (id) {
            // opens the floating action button
            case R.id.fab_open:

                animateFAB();
                break;

            // handles the saving of a bounty to a saved bounty file in internal memory
            case R.id.fab_save:

                Button saveButton = new Button();
                saveButton.saveBounty(this, mBountyId);
                break;

            // opens the camera activity, captures and displays image
            case R.id.fab_hunt:

                // create a new camera instance and start the camera
                huntButton = new Button();
                huntButton.startCameraIntent(this);
                break;
        }
    }

    // this handles the animation for the floating action buttons
    public void animateFAB(){

        if(isFabOpen){

            mFabOpen.startAnimation(rotate_backward);
            mFabSave.startAnimation(fab_close);
            mFabHunt.startAnimation(fab_close);
            mFabSave.setClickable(false);
            mFabHunt.setClickable(false);
            isFabOpen = false;
        }
        else {

            mFabOpen.startAnimation(rotate_forward);
            mFabSave.startAnimation(fab_open);
            mFabHunt.startAnimation(fab_open);
            mFabSave.setClickable(true);
            mFabHunt.setClickable(true);
            isFabOpen = true;
        }
    }

    // used on return from an outside activity, in this case it is used when returning from camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // upload the picture just taken from the camera to the server
            huntButton.uploadCameraPicture(this, mPlacerId, mBountyId);

        }

    }

}
