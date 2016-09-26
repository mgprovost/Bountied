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

import com.bountiedapp.bountied.DownloadBountyList;
import com.bountiedapp.bountied.InternalReader;
import com.bountiedapp.bountied.NetworkRequest;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.adpter.BountyPlacedAdapter;
import com.bountiedapp.bountied.model.BountyHuntListItem;
import com.bountiedapp.bountied.model.Gps;
import com.bountiedapp.bountied.model.StaticStrings;

import org.json.JSONException;

import java.util.ArrayList;

public class BountiesPlaced extends AppCompatActivity implements BountyPlacedAdapter.ItemClickCallback {

    // used for network requests
    // the first one is for downloading all information on users placed bounties
    // the second one is to delete a bounty from the database
    private final String M_URL_PLACED_BOUNTIES = StaticStrings.BASE_URL + "bountiesplaced";
    private final String M_URL_DELETE = StaticStrings.BASE_URL + "deletebounty";

    // these are just static strings used for the intents
    // to send the all the foundID's to the possible found bounties page
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_FOUNDARRAY = "EXTRA_FOUNDARRAY";

    // bounty hunt adapter is to be used by the recycler view to set the view
    // based on the information it's given
    private static BountyPlacedAdapter mBountyPlacedAdapter;

    // recycler view is used to display all the "cards" produced by the data
    private static RecyclerView mRecyclerView;

    // will hold the returned data from the server when it responds
    private ArrayList mBountyHuntListData;

    private Gps mGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set the view from xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounties_placed);

        // set the toolbar from the xml
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load a gps object which will begin updating coordinates as they become available
        mGPS = new Gps(this);

        // get a reference to the recycler view from the xml
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_list_bounties_placed);

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

        // need to get the ID of this user from internal memory to download
        // bounties placed specifically by this individual
        InternalReader internalReader = new InternalReader(this);
        String placerID = internalReader.readFromFile("ID");

        // asynchronously download all data on the bounties that were placed by this user
        try {
            DownloadBountyList downloadBountyList = new DownloadBountyList();
            downloadBountyList.downloadPlaced(this, M_URL_PLACED_BOUNTIES, placerID, new DownloadBountyList.VolleyCallback() {
                @Override
                public void onSuccess(ArrayList<BountyHuntListItem> result) {

                    // this is the returned data from the server
                    mBountyHuntListData = result;

                    // layout manager takes care of the look of the layout
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                    // pass the returned data from the server to our adapter
                    mBountyPlacedAdapter = new BountyPlacedAdapter(result, context);

                    // pass our custom adapter, after it has been loaded with data, to the view
                    mRecyclerView.setAdapter(mBountyPlacedAdapter);

                    // this basically says to Adapter when itemClickCallBack is called
                    // I (this Activity) will handle it
                    mBountyPlacedAdapter.setItemClickCallback(BountiesPlaced.this);

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
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        // intent will be used to open the "possible bounty finds" activity if card is clicked
        Intent intent = new Intent(this, BountiesFound.class);

        // put an array of all the found ID's to pass to next activity
        Bundle extras = new Bundle();
        extras.putCharSequenceArrayList(EXTRA_FOUNDARRAY, bountyHuntListItem.getFoundIDS());

        intent.putExtra(BUNDLE_EXTRAS, extras);
        startActivity(intent);
    }

    // called when user clicks "delete" on the bounty card
    // will delete the bounty from the server along with all hunts on this item
    @Override
    public void onDeleteIconClick(int position) {

        // from the returned server data, get a single bounty hunt item / info associated with that item
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        // get the bountyID of the card that is being clicked on
        String bountyID = bountyHuntListItem.getImageUrl();

        // asynchronously send a request to the server to delete a bounty
        // along with any associated hunts on that bounty
        NetworkRequest networkRequest = new NetworkRequest();

        try {
            networkRequest.deleteABounty(this, M_URL_DELETE, bountyID, new NetworkRequest.PostDeletionInterface() {
                @Override
                public void refresh() {
                    finish();
                    startActivity(getIntent());
                    System.out.println("Hello From placed");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
