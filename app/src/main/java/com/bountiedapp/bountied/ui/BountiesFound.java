package com.bountiedapp.bountied.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.bountiedapp.bountied.DownloadBountyList;
import com.bountiedapp.bountied.NetworkRequest;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.adpter.BountiesFoundAdapter;
import com.bountiedapp.bountied.model.BountyFoundListItem;
import com.bountiedapp.bountied.model.Gps;
import com.bountiedapp.bountied.model.StaticStrings;

import org.json.JSONException;

import java.util.ArrayList;

public class BountiesFound extends AppCompatActivity implements BountiesFoundAdapter.ItemClickCallback {

    // endpoint to get data from the server
    private final String M_URL_DOWNLOAD = StaticStrings.BASE_URL + "downloadbountiesfound";

    // endpoints to send data to the server
    private final String M_URL_ACCEPT = StaticStrings.BASE_URL + "acceptbounty";
    private final String M_URL_DECLINE = StaticStrings.BASE_URL + "declinebounty";

    // these are just static strings used to get data
    // from the previous activity as well as send data
    // to the subsequent activity if a user clicks the card
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_FOUNDARRAY = "EXTRA_FOUNDARRAY";
    private static final String EXTRA_IMAGEURL = "EXTRA_IMAGEURL";

    // bounty found adapter is to be used by the recycler view to set the view
    // based on the information it's given
    private static BountiesFoundAdapter mBountyFoundAdapter;

    // recycler view is used to display all the "cards" produced by the data
    private static RecyclerView mRecyclerView;

    // will hold the returned data from the server when it responds
    private ArrayList mBountyFoundListData;

    private Gps mGPS;
    private ArrayList mFoundIDs = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set the view from xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounties_found);

        // set the toolbar from the xml
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load a gps object which will begin updating coordinates as they become available
        mGPS = new Gps(this);

        // get the extras from the previous activity
        Bundle extras = getIntent().getBundleExtra(BUNDLE_EXTRAS);
        mFoundIDs = extras.getCharSequenceArrayList(EXTRA_FOUNDARRAY);

        // get a reference to the recycler view from the xml
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_list_bounties_found);

        // get all the relevant list data and load it into the recycler view assigned above
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
                Intent huntIntent = new Intent(this, BountyHunt.class);
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

        // asynchronously download all data associated with the possible found
        // bounties for a given single bounty
        try {
            DownloadBountyList downloadBountyList = new DownloadBountyList();
            downloadBountyList.downloadBountiesFound(this, M_URL_DOWNLOAD, mFoundIDs, new DownloadBountyList.VolleyCallbackTwo() {
                @Override
                public void onFinish(ArrayList<BountyFoundListItem> result) {

                    // this is the returned data from the server
                    mBountyFoundListData = result;

                    // layout manager takes care of the look of the layout
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                    // pass the returned data from the server to our adapter
                    mBountyFoundAdapter = new BountiesFoundAdapter(result, context);

                    // pass our custom adapter, after it has been loaded with data, to the view
                    mRecyclerView.setAdapter(mBountyFoundAdapter);

                    // this basically says to Adapter when itemClickCallBack is called
                    // I (this Activity) will handle it
                    mBountyFoundAdapter.setItemClickCallback(BountiesFound.this);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // if any of the cards, any part of the card except the button gets clicked, do the following
    @Override
    public void onItemClick(int position) {

        // from the returned server data, get a single bounty hunt item / info associated with that item
        BountyFoundListItem bountyFoundListItem = (BountyFoundListItem) mBountyFoundListData.get(position);

        // intent will be used to open the detail - full image - activity if card is clicked
        Intent intent = new Intent(this, FullImage.class);

        // put all the following info in a bundle to send it to detail - full image - activity
        Bundle extras = new Bundle();
        extras.putString(EXTRA_IMAGEURL, bountyFoundListItem.getImageURL());

        // add the budle to the intent and start the subsequent activity
        intent.putExtra(BUNDLE_EXTRAS, extras);
        startActivity(intent);
    }

    // called when user clicks "accept" on the bounty card
    @Override
    public void onAcceptClick(final Context context, int position) {

        // this will have all the info to the most recently clicked "bounty card"
        BountyFoundListItem bountyFoundListItem = (BountyFoundListItem) mBountyFoundListData.get(position);

        String foundID = bountyFoundListItem.getFoundID();

        // make an asynchronous network request to tell our server a bounty hunt has been accepted
        NetworkRequest networkRequest = new NetworkRequest();

        try {
            networkRequest.acceptFoundBounty(this, M_URL_ACCEPT, foundID, new NetworkRequest.AcceptFoundInterface() {
                @Override
                public void accept() {
                    Intent placedIntent = new Intent(context, BountiesPlaced.class);
                    startActivity(placedIntent);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // called when user clicks "accept" on the bounty card
    @Override
    public void onDeclineClick(int position) {

        // this will have all the info to the most recently clicked "bounty card"
        BountyFoundListItem bountyFoundListItem = (BountyFoundListItem) mBountyFoundListData.get(position);

        String foundID = bountyFoundListItem.getFoundID();

        // make an asynchronous network request to tell our server a bounty hunt has been declined
        NetworkRequest networkRequest = new NetworkRequest();

        try {
            networkRequest.declineFoundBounty(this, M_URL_DECLINE, foundID, new NetworkRequest.DeclineFoundInterface() {
                @Override
                public void decline() {
                    finish();
                    startActivity(getIntent());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
