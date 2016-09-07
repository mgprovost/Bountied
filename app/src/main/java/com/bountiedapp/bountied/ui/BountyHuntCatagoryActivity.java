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
import android.widget.ImageView;
import android.widget.TextView;

import com.bountiedapp.bountied.DividerItemDecoration;
import com.bountiedapp.bountied.DownloadBountyList;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.adpter.BountyHuntAdapter;
import com.bountiedapp.bountied.model.BountyHuntListItem;
import com.bountiedapp.bountied.model.Gps;

import org.json.JSONException;

import java.util.ArrayList;

public class BountyHuntCatagoryActivity extends AppCompatActivity implements BountyHuntAdapter.ItemClickCallback {

    private static BountyHuntAdapter mBountyHuntAdapter;
    private static RecyclerView recyclerView;
    private ArrayList mBountyHuntListData;
    private Gps gps;

    // these are just static strings used for the intents
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    private static final String EXTRA_BOUNTY = "EXTRA_BOUNTY";
    private static final String EXTRA_IMAGEURL = "EXTRA_IMAGEURL";
    private static final String EXTRA_PLACERID = "EXTRA_PLACERID";
    private static final String EXTRA_LAT = "EXTRA_LAT";
    private static final String EXTRA_LNG = "EXTRA_LNG";


    private static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";

    private int REQUEST_IMAGE_CAPTURE = 1;

    private final String M_URL = "http://192.168.1.8:3000/bountylistcategorydownload";

    private String bountyID;
    private String placerID;

    private Button huntButton;
    private Button saveButton;

    private TextView mNoBountyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounty_hunt_catagory);

        // set the toolbar from the xml
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // May need to load this in the launch screen initially
        // Load a gps object which will begin updating coordinates as they become available
        gps = new Gps(this);

//        // this is a method that allows older versions of
//        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get the category that the user clicked from the previous activity
        String catagory = getIntent().getStringExtra(EXTRA_CATEGORY);

        // set that category as the title of the screen
        setTitle(catagory);

        // get the recycler view from the xml
        recyclerView = (RecyclerView)findViewById(R.id.recycler_list);

        mNoBountyTextView = (TextView) findViewById(R.id.hunt_catagory_no_bounty_text);

        // get all the relevant list data and load it into the recycler view assigned above
        getListData(this, catagory);
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

    public void getListData(final Context context, final String catagory) {

        // sets the last know user coords for gps
        gps.setLastKnownLatLng(this);

        // pull the users lat and lng coordinates to use them in the downloading of the relevant list
        String mLat = Double.toString(gps.getmLat());
        String mLng = Double.toString(gps.getmLng());

        System.out.println("Latitude is:  " + mLat);
        System.out.println("Longitude is:  " + mLng);

        // stops the gps from listening for new coords which is costly for battery life
        gps.stopGps(this);


        try {
            DownloadBountyList downloadBountyList = new DownloadBountyList();
            downloadBountyList.download(this, M_URL, catagory, mLat, mLng, new DownloadBountyList.VolleyCallback() {
                @Override
                public void onSuccess(ArrayList<BountyHuntListItem> result) {

                    mBountyHuntListData = result;


                    // if there is data for the specific catagory requested, set it in adapter
                    if (!result.toString().equals("[]")) {

                        recyclerView.setLayoutManager(new LinearLayoutManager(context));

                        mBountyHuntAdapter = new BountyHuntAdapter(result, context);

                        recyclerView.setAdapter(mBountyHuntAdapter);

                        // this basically says to Adapter when itemClickCallBack is called
                        // ill (this Activity) will handle it
                        mBountyHuntAdapter.setItemClickCallback(BountyHuntCatagoryActivity.this);

                    }
                    // otherwise display a "no bounties here" image to inform the user
                    else {
                        mNoBountyTextView.setText("Sorry, no bounties have been placed in the " +
                                                    catagory + " catagory yet.");
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(int position) {
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        Intent intent = new Intent(this, HomeDetailActivity.class);

        Bundle extras = new Bundle();

        extras.putString(EXTRA_TITLE, bountyHuntListItem.getmTitle());
        extras.putString(EXTRA_DESCRIPTION, bountyHuntListItem.getmDescription());
        extras.putString(EXTRA_BOUNTY, bountyHuntListItem.getmBounty());
        extras.putString(EXTRA_IMAGEURL, bountyHuntListItem.getmImageUrl());
        extras.putString(EXTRA_PLACERID, bountyHuntListItem.getmPlacerID());
        extras.putString(EXTRA_LAT, bountyHuntListItem.getmLat());
        extras.putString(EXTRA_LNG, bountyHuntListItem.getmLng());

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
    public void onSaveClick(int position) {
        // this will have all the info to the most recently clicked "bounty card"
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        bountyID = bountyHuntListItem.getmImageUrl();

        saveButton = new Button();
        saveButton.saveBounty(this, bountyID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // upload the picture just taken from the camera to the server
            huntButton.uploadCameraPicture(this, placerID, bountyID);
        }

    }

}
