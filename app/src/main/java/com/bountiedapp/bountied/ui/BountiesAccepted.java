package com.bountiedapp.bountied.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bountiedapp.bountied.DownloadBountyList;
import com.bountiedapp.bountied.InternalReader;
import com.bountiedapp.bountied.NetworkRequest;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.adpter.BountyAcceptedAdapter;
import com.bountiedapp.bountied.model.BountyHuntListItem;

import org.json.JSONException;

import java.util.ArrayList;

public class BountiesAccepted extends AppCompatActivity implements BountyAcceptedAdapter.ItemClickCallback {

    // endpoint on our server to download all bounties the user has accepted to date
    private final String M_URL_ACCEPTED = "http://192.168.1.8:3000/downloadbountiesaccepted";

    // endpoint on our server to delete any accepted bounties from the database
    private final String M_URL_DELETE = "http://192.168.1.8:3000/deleteaccepted";

    // bounty accepted adapter is to be used by the recycler view to set the view
    // based on the information it's given
    private static BountyAcceptedAdapter mBountyAcceptedAdapter;

    // recycler view is used to display all the "cards" produced by the data
    private static RecyclerView mRecyclerView;

    // will hold the returned data from the server when it responds
    private ArrayList mBountyHuntListData;

    // called anytime the activity loads
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set the view from xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounties_accepted);

        // set the toolbar from the xml
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get a reference to the recycler view from the xml
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_list_bounties_accepted);

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

        // get the ID of this user from internal memory
        InternalReader internalReader = new InternalReader(this);
        String placerID = internalReader.readFromFile("ID");
        System.out.println("PLACERID: " + placerID);

        // asynchronously download all of the accepted bounty data associated with this user
        try {
            DownloadBountyList downloadBountyList = new DownloadBountyList();
            downloadBountyList.downloadAccepted(this, M_URL_ACCEPTED, placerID, new DownloadBountyList.VolleyCallback() {
                @Override
                public void onSuccess(ArrayList<BountyHuntListItem> result) {

                    // this is the returned data from the server
                    mBountyHuntListData = result;

                    // layout manager takes care of the look of the layout
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                    // pass the returned data from the server to our adapter
                    mBountyAcceptedAdapter = new BountyAcceptedAdapter(result, context);

                    // pass our custom adapter, after it has been loaded with data, to the view
                    mRecyclerView.setAdapter(mBountyAcceptedAdapter);

                    // this basically says to Adapter when itemClickCallBack is called
                    // I (this Activity) will handle it
                    mBountyAcceptedAdapter.setItemClickCallback(BountiesAccepted.this);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // if the map button is clicked on an individual card go to google maps
    // and place a marker at the location of where the bounty was found
    // so the user can get directions and navigate to it
    @Override
    public void onMapClick(int position) {

        // from the returned server data, get a single bounty hunt item / info associated with that item
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        String foundLat = bountyHuntListItem.getFoundLat();
        String foundLng = bountyHuntListItem.getFoundLng();

        // set a map query for google maps that includes the lat and lng of where the bounty was found
        String mapQuery = "geo:0,0?q=" + foundLat + "," + foundLng + "(" + "Your bounty was found at this location." + ")";

        // get ready to open google maps with a URI and an intent
        Uri gmmIntentUri = Uri.parse(mapQuery);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // if google maps is on the phone open it, otherwise tell the user
        // google maps is need to use this feature
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
        else {
            Toast.makeText(this, "Sorry, need to have google maps installed.", Toast.LENGTH_LONG).show();
        }
    }

    // if the delete button is clicked then delete this accepted bounty from the database
    @Override
    public void onDeleteClick(int position) {

        // from the returned server data, get a single bounty hunt item / info associated with that item
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        String bountyID = bountyHuntListItem.getImageUrl();

        // asynchronously delete the accepted bounty information from the database
        NetworkRequest networkRequest = new NetworkRequest();

        try {
            networkRequest.deleteABounty(this, M_URL_DELETE, bountyID, new NetworkRequest.PostDeletionInterface() {
                @Override
                public void refresh() {
                    finish();
                    startActivity(getIntent());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}