package com.bountiedapp.bountied.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bountiedapp.bountied.DividerItemDecoration;
import com.bountiedapp.bountied.DownloadBountyList;
import com.bountiedapp.bountied.InternalReader;
import com.bountiedapp.bountied.InternalWriter;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.adpter.BountyHuntAdapter;
import com.bountiedapp.bountied.adpter.BountyHuntsInProgressAdapter;
import com.bountiedapp.bountied.model.BountyHuntListItem;
import com.bountiedapp.bountied.model.Gps;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class HuntsInProgress extends AppCompatActivity implements BountyHuntsInProgressAdapter.ItemClickCallback {

    private static BountyHuntsInProgressAdapter mBountyHuntsInProgressAdapter;
    private static RecyclerView recyclerView;
    private ArrayList mBountyHuntListData;
    private Gps gps;

    private String bountyID;
    private String placerID;

    private Button huntButton;
    private Button deleteButton;

    private int REQUEST_IMAGE_CAPTURE = 1;

    // these are just static strings used for the intents
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    private static final String EXTRA_ACTIVITY = "EXTRA_ACTIVITY";
    private static final String EXTRA_IMAGEURL = "EXTRA_IMAGEURL";
    private static final String EXTRA_PLACERID = "EXTRA_PLACERID";

    private static final String HUNTS_IN_PROGRESS = "HUNTS_IN_PROGRESS";

    private final String M_URL = "http://192.168.1.8:3000/bountiesinprogress";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunts_in_progress);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // May need to load this in the launch screen initially
        gps = new Gps(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_list_hunts_in_progress);
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


    public void getListData(final Context context) {

        // sets the last know user coords for gps
        gps.setLastKnownLatLng(this);

        // pull the users lat and lng coordinates to use them in the downloading of the relevant list
        String mLat = Double.toString(gps.getmLat());
        String mLng = Double.toString(gps.getmLng());

        System.out.println("Latitude is:  " + mLat);
        System.out.println("Longitude is:  " + mLng);

        // stops the gps from listening for new coords which is costly for battery life
        gps.stopGps(this);

        InternalReader internalReader = new InternalReader(this);
        String ids = internalReader.readFromFile("bounties_to_hunt");
        ArrayList arrayOfIds = getArrayFromString(ids);

        try {
            DownloadBountyList downloadBountyList = new DownloadBountyList();
            downloadBountyList.downloadInProgress(this, M_URL, arrayOfIds, mLat, mLng, new DownloadBountyList.VolleyCallback() {
                @Override
                public void onSuccess(ArrayList<BountyHuntListItem> result) {

                    mBountyHuntListData = result;

                    recyclerView.setLayoutManager(new LinearLayoutManager(context));

                    mBountyHuntsInProgressAdapter = new BountyHuntsInProgressAdapter(result, context);

                    recyclerView.setAdapter(mBountyHuntsInProgressAdapter);

                    // this basically says to Adapter when itemClickCallBack is called
                    // ill (this Activity) will handle it
                    mBountyHuntsInProgressAdapter.setItemClickCallback(HuntsInProgress.this);

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // upload the picture just taken from the camera to the server
            huntButton.uploadCameraPicture(this, placerID, bountyID);

            // since this bountyID is saved in internal memory as a hunt in progress
            // need to go into internal memory and erase it
            huntButton.deleteBounty(this, bountyID);

            // go to the HuntsInProgress activity to see updated list
            Intent huntsIntent = new Intent(this, HuntsInProgress.class);
            startActivity(huntsIntent);
        }

    }

    @Override
    public void onItemClick(int position) {
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        Intent intent = new Intent(this, HuntsInProgressDetail.class);

        Bundle extras = new Bundle();
        extras.putString(EXTRA_TITLE, bountyHuntListItem.getmTitle());
//        extras.putString(EXTRA_DESCRIPTION, bountyHuntListItem.getmDescription());
//        extras.putString(EXTRA_BOUNTY, bountyHuntListItem.getmBounty());
        extras.putString(EXTRA_IMAGEURL, bountyHuntListItem.getmImageUrl());
        extras.putString(EXTRA_PLACERID, bountyHuntListItem.getmPlacerID());

        System.out.println(extras.toString());

        intent.putExtra(BUNDLE_EXTRAS, extras);
        startActivity(intent);
    }

    @Override
    public void onHuntClick(int position) {
        // this will have all the info to the most recently clicked "bounty card"
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        // imageUrl is same as bountyID, also get the placerID
        bountyID = bountyHuntListItem.getmImageUrl();
        placerID = bountyHuntListItem.getmPlacerID();

        // create a new camera instance and start the camera
        huntButton = new Button();
        huntButton.startCameraIntent(this);
    }

    @Override
    public void onDeleteClick(int position) {
        // this will have all the info to the most recently clicked "bounty card"
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        bountyID = bountyHuntListItem.getmImageUrl();

        deleteButton = new Button();
        deleteButton.deleteBounty(this, bountyID);

        finish();
        startActivity(getIntent());
    }

    // get an array list from a string
    // this return an arraylist from the downloaded "found" string format
    private ArrayList getArrayFromString(String someString) {

        // remove the left bracket, then right bracket, then all spaces from the string
        String removeLeftBracket = someString.replace("[", "");
        String removeRightBracket = removeLeftBracket.replace("]", "");
        String stringWithRemoval = removeRightBracket.replace(" ", "");

        // split the string on all "," in it and then make each of those strings elements in array and return it
        return new ArrayList<String>(Arrays.asList(stringWithRemoval.split(",")));
    }

}
