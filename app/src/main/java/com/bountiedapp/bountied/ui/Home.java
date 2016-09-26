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
import android.widget.Toast;

import com.bountiedapp.bountied.Button;
import com.bountiedapp.bountied.DownloadBountyList;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.adpter.BountyHuntAdapter;
import com.bountiedapp.bountied.model.BountyHuntListItem;
import com.bountiedapp.bountied.model.Gps;
import com.bountiedapp.bountied.model.StaticStrings;

import org.json.JSONException;

import java.util.ArrayList;

public class Home extends AppCompatActivity implements BountyHuntAdapter.ItemClickCallback {

    // endpoint to send information to the server
    private final String M_URL = StaticStrings.BASE_URL + "bountylistdownload";

    // constant used when returning to the activity from the camera intent
    private int REQUEST_IMAGE_CAPTURE = 1;

    // these are just static strings used for the sending
    // data from this activity to the detail activity
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    private static final String EXTRA_BOUNTY = "EXTRA_BOUNTY";
    private static final String EXTRA_IMAGEURL = "EXTRA_IMAGEURL";
    private static final String EXTRA_PLACERID = "EXTRA_PLACERID";
    private static final String EXTRA_LAT = "EXTRA_LAT";
    private static final String EXTRA_LNG = "EXTRA_LNG";

    // bounty hunt adapter is to be used by the recycler view to set the view
    // based on the information it's given
    private static BountyHuntAdapter mBountyHuntAdapter;

    // recycler view is used to display all the "cards" produced by the data
    private static RecyclerView mRecyclerView;

    // will hold the returned data from the server when it responds
    private ArrayList mBountyHuntListData;

    private Gps mGPS;
    private String mBountyID;
    private String mPlacerID;

    // UI objects
    private Button mHuntButton;
    private Button mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set the view from xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // set the toolbar from xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // instantiate a new GPS object to use before going out to server
        mGPS = new Gps(this);

        // get a reference to the recycler view in the xml
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_list);

        // go get the data from the server
        getListData(this);

     }

    // used to display the main menu on top of the screen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu, this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // the following intents serve to start different activities as buttons are pressed
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

        // just a placeholder, since we aren't looking for a specific catagory
        final String CATEGORY = "anything";

        // network request to download a list of random bounties with no specified catagory
        // it will however use the location of the user and parse out bounties the user
        // should not see because of the radius that the bounty placer specified
        try {
            DownloadBountyList downloadBountyList = new DownloadBountyList();
            downloadBountyList.download(this, M_URL, CATEGORY, mLat, mLng, new DownloadBountyList.VolleyCallback() {
                @Override
                public void onSuccess(ArrayList<BountyHuntListItem> result) {

                    // this is the returned data from the server
                    mBountyHuntListData = result;

                    // layout manager takes care of the look of the layout
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                    // pass the returned data from the server to our adapter
                    mBountyHuntAdapter = new BountyHuntAdapter(result, context);

                    // pass our custom adapter, after it has been loaded with data, to the view
                    mRecyclerView.setAdapter(mBountyHuntAdapter);

                    // this basically says to Adapter when itemClickCallBack is called
                    // I (this Activity) will handle it
                    mBountyHuntAdapter.setItemClickCallback(Home.this);

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

        // intent will be used to open the detail activity if card is clicked
        Intent intent = new Intent(this, BountyDetail.class);

        // put all the following info in a bundle to send it to detail activity
        Bundle extras = new Bundle();
        extras.putString(EXTRA_TITLE, bountyHuntListItem.getTitle());
        extras.putString(EXTRA_DESCRIPTION, bountyHuntListItem.getDescription());
        extras.putString(EXTRA_BOUNTY, bountyHuntListItem.getBounty());
        extras.putString(EXTRA_IMAGEURL, bountyHuntListItem.getImageUrl());
        extras.putString(EXTRA_PLACERID, bountyHuntListItem.getPlacerID());
        extras.putString(EXTRA_LAT, bountyHuntListItem.getLat());
        extras.putString(EXTRA_LNG, bountyHuntListItem.getLng());

        intent.putExtra(BUNDLE_EXTRAS, extras);
        startActivity(intent);
    }

    // called when user clicks "hunt" on the bounty card
    @Override
    public void onHuntClick(int position) {

        // this will have all the info to the most recently clicked "bounty card"
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        // imageUrl is same as bountyID, also get the placerID
        // need these for when the camera returns to this activity
        mBountyID = bountyHuntListItem.getImageUrl();
        mPlacerID = bountyHuntListItem.getPlacerID();

        // create a new camera instance and start the camera
        mHuntButton = new Button();
        mHuntButton.startCameraIntent(this);
    }

    // called when clicked save button on card, saves a bounty to a users
    @Override
    public void onSaveClick(int position) {

        // this will have all the info to the most recently clicked "bounty card"
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        // imageUrl is same as bountyID
        mBountyID = bountyHuntListItem.getImageUrl();

        // this will save the bountyID of the card that the user clicked on to internal memory
        // it is saved to a dedicated internal file that keeps track of all saved bounty hunts
        mSaveButton = new Button();
        mSaveButton.saveBounty(this, mBountyID);
    }


    // used on return from an outside activity, in this case it is used when returning from camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // upload the picture just taken from the camera to the server
            mHuntButton.uploadCameraPicture(this, mPlacerID, mBountyID);
        }
        else {
            Toast.makeText(this, "A camera issue has been detected.", Toast.LENGTH_LONG).show();
        }
    }

}
