package com.bountiedapp.bountied.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.bountiedapp.bountied.Button;
import com.bountiedapp.bountied.DownloadBountyList;
import com.bountiedapp.bountied.InternalReader;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.adpter.BountyHuntsInProgressAdapter;
import com.bountiedapp.bountied.model.BountyHuntListItem;
import com.bountiedapp.bountied.model.Gps;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

public class HuntsInProgress extends AppCompatActivity implements BountyHuntsInProgressAdapter.ItemClickCallback {

    // endpoint to send information to the server
    private final String M_URL = "http://192.168.1.8:3000/bountiesinprogress";

    // constant used when returning to the activity from the camera intent
    private int REQUEST_IMAGE_CAPTURE = 1;

    // these are just static strings to be used for our intent
    // specifically to send the strings to the following detail activity
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    private static final String EXTRA_BOUNTY = "EXTRA_BOUNTY";
    private static final String EXTRA_IMAGEURL = "EXTRA_IMAGEURL";
    private static final String EXTRA_PLACERID = "EXTRA_PLACERID";

    // bounty hunts in progress adapter is to be used by the recycler view
    // to set the view based on the information it's given
    private static BountyHuntsInProgressAdapter mBountyHuntsInProgressAdapter;

    // recycler view is used to display all the "cards" produced by the data
    private static RecyclerView mRecyclerView;

    // will hold the returned data from the server when it responds
    private ArrayList mBountyHuntListData;

    private Gps mGPS;
    private String mBountyID;
    private String mPlacerID;

    // will be responsible for the action that occurs
    // when the hunt/delete buttons are pressed
    private Button mHuntButton;
    private Button mDeleteButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set the view from xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunts_in_progress);

        // set the toolbar from xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // instantiate a new GPS object to use before going out to server
        mGPS = new Gps(this);

        // get a reference to the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_list_hunts_in_progress);

        // go get the data from the server
        getListData(this);

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

    // grabs the list of data from the network
    public void getListData(final Context context) {

        // sets the last know user coords for gps
        mGPS.setLastKnownLatLng(this);

        // pull the users lat and lng coordinates to use them in the downloading of the relevant list
        String mLat = Double.toString(mGPS.getLat());
        String mLng = Double.toString(mGPS.getLng());

        // stops the gps from listening for new coords which is costly for battery life
        mGPS.stopGps(this);

        // read all the bounties that a user is hunting in progress in internal memory
        // the internal reader returns a string, so convert it to a list of ids
        InternalReader internalReader = new InternalReader(this);
        String ids = internalReader.readFromFile("bounties_to_hunt");
        ArrayList arrayOfIds = getArrayFromString(ids);

        // asynchronously download the data associated with the bounty ids we read above
        // it will use the location of the user and parse out bounties the user
        // should not see because of the radius that the bounty placer specified
        try {
            DownloadBountyList downloadBountyList = new DownloadBountyList();
            downloadBountyList.downloadInProgress(this, M_URL, arrayOfIds, mLat, mLng, new DownloadBountyList.VolleyCallback() {
                @Override
                public void onSuccess(ArrayList<BountyHuntListItem> result) {

                    // this is the returned data from the server
                    mBountyHuntListData = result;

                    // layout manager takes care of the look of the layout
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                    // pass the returned data from the server to our adapter
                    mBountyHuntsInProgressAdapter = new BountyHuntsInProgressAdapter(result, context);

                    // pass our custom adapter, after it has been loaded with data, to the view
                    mRecyclerView.setAdapter(mBountyHuntsInProgressAdapter);

                    // this basically says to Adapter when itemClickCallBack is called
                    // I (this Activity) will handle it
                    mBountyHuntsInProgressAdapter.setItemClickCallback(HuntsInProgress.this);

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // used for coming back from external activity like camera activity that user launched
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if the user came back from the camera activity without a problem
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // upload the picture just taken from the camera to the server
            mHuntButton.uploadCameraPicture(this, mPlacerID, mBountyID);

            // since this bountyID is saved in internal memory as a hunt in progress
            // need to go into internal memory and erase it, because user just hunted it
            mHuntButton.deleteBounty(this, mBountyID);

            // go to the HuntsInProgress activity to see updated list
            Intent huntsIntent = new Intent(this, HuntsInProgress.class);
            startActivity(huntsIntent);
        }
    }

    // if any of the cards, any part of the card except the button gets clicked, do the following
    @Override
    public void onItemClick(int position) {

        // from the returned server data, get a single bounty hunt item / info associated with that item
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        // intent will be used to open the detail activity if card is clicked
        Intent intent = new Intent(this, HuntsInProgressDetail.class);

        // put all the following info in a bundle to send it to detail activity
        Bundle extras = new Bundle();
        extras.putString(EXTRA_TITLE, bountyHuntListItem.getTitle());
        extras.putString(EXTRA_DESCRIPTION, bountyHuntListItem.getDescription());
        extras.putString(EXTRA_BOUNTY, bountyHuntListItem.getBounty());
        extras.putString(EXTRA_IMAGEURL, bountyHuntListItem.getImageUrl());
        extras.putString(EXTRA_PLACERID, bountyHuntListItem.getPlacerID());

        intent.putExtra(BUNDLE_EXTRAS, extras);
        startActivity(intent);
    }

    // called when user clicks "hunt" on the bounty card
    @Override
    public void onHuntClick(int position) {

        // this will have all the info to the most recently clicked "bounty card"
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        // imageUrl is same as bountyID, also get the placerID
        mBountyID = bountyHuntListItem.getImageUrl();
        mPlacerID = bountyHuntListItem.getPlacerID();

        // create a new camera instance and start the camera
        mHuntButton = new Button();
        mHuntButton.startCameraIntent(this);
    }

    @Override
    public void onDeleteClick(int position) {

        // this will have all the info to the most recently clicked "bounty card"
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        // imageUrl is same as bountyID, also get the placerID
        mBountyID = bountyHuntListItem.getImageUrl();

        // delete the bounty from the users internally
        // stored saved bounties that user is hunting for
        mDeleteButton = new Button();
        mDeleteButton.deleteBounty(this, mBountyID);

        // refresh this page/activity
        finish();
        startActivity(getIntent());
    }

    // get an array list from a string
    // this returns an arraylist from the downloaded "found" string format
    private ArrayList getArrayFromString(String someString) {

        // remove the left bracket, then right bracket, then all spaces from the string
        String removeLeftBracket = someString.replace("[", "");
        String removeRightBracket = removeLeftBracket.replace("]", "");
        String stringWithRemoval = removeRightBracket.replace(" ", "");

        // split the string on all "," in it and then make each of those strings elements in array and return it
        return new ArrayList<String>(Arrays.asList(stringWithRemoval.split(",")));
    }

}
