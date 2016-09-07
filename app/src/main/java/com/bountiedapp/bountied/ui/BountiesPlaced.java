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
import android.widget.Toast;

import com.bountiedapp.bountied.DividerItemDecoration;
import com.bountiedapp.bountied.DownloadBountyList;
import com.bountiedapp.bountied.InternalReader;
import com.bountiedapp.bountied.NetworkRequest;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.adpter.BountyPlacedAdapter;
import com.bountiedapp.bountied.model.BountyHuntListItem;
import com.bountiedapp.bountied.model.Gps;

import org.json.JSONException;

import java.util.ArrayList;

public class BountiesPlaced extends AppCompatActivity implements BountyPlacedAdapter.ItemClickCallback {

    private static BountyPlacedAdapter mBountyPlacedAdapter;
    private static RecyclerView recyclerView;
    private ArrayList mBountyHuntListData;
    private Gps gps;

    // these are just static strings used for the intents
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_FOUNDARRAY = "EXTRA_FOUNDARRAY";

    private final String M_URL = "http://192.168.1.8:3000/bountiesplaced";
    private final String M_URL_DELETE = "http://192.168.1.8:3000/deletebounty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounties_placed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // May need to load this in the launch screen initially
        gps = new Gps(this);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_list_bounties_placed);
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
        String placerID = internalReader.readFromFile("ID");
        System.out.println("PLACERID: " + placerID);


        try {
            DownloadBountyList downloadBountyList = new DownloadBountyList();
            downloadBountyList.downloadPlaced(this, M_URL, placerID, new DownloadBountyList.VolleyCallback() {
                @Override
                public void onSuccess(ArrayList<BountyHuntListItem> result) {

                    mBountyHuntListData = result;

                    recyclerView.setLayoutManager(new LinearLayoutManager(context));

                    mBountyPlacedAdapter = new BountyPlacedAdapter(result, context);

                    recyclerView.setAdapter(mBountyPlacedAdapter);

                    // this basically says to Adapter when itemClickCallBack is called
                    // ill (this Activity) will handle it
                    mBountyPlacedAdapter.setItemClickCallback(BountiesPlaced.this);

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(int position) {
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        Intent intent = new Intent(this, BountiesFound.class);

        Bundle extras = new Bundle();

        extras.putCharSequenceArrayList(EXTRA_FOUNDARRAY, bountyHuntListItem.getFoundIDS());

        System.out.println("FOUND ********: " + extras.toString());

        intent.putExtra(BUNDLE_EXTRAS, extras);
        startActivity(intent);
    }

    @Override
    public void onSecondaryIconClick(int position) {

        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);
        String bountyID = bountyHuntListItem.getmImageUrl();

        System.out.println("DELETE THIS PLACED BOUNTY");
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
