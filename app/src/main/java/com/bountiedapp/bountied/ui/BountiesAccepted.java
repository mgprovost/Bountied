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

import com.bountiedapp.bountied.DividerItemDecoration;
import com.bountiedapp.bountied.DownloadBountyList;
import com.bountiedapp.bountied.InternalReader;
import com.bountiedapp.bountied.NetworkRequest;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.adpter.BountyAcceptedAdapter;
import com.bountiedapp.bountied.adpter.BountyPlacedAdapter;
import com.bountiedapp.bountied.model.BountyHuntListItem;
import com.bountiedapp.bountied.model.Gps;

import org.json.JSONException;

import java.util.ArrayList;

public class BountiesAccepted extends AppCompatActivity implements BountyAcceptedAdapter.ItemClickCallback {

    private static BountyAcceptedAdapter mBountyAcceptedAdapter;
    private static RecyclerView recyclerView;
    private ArrayList mBountyHuntListData;

    private final String M_URL = "http://192.168.1.8:3000/downloadbountiesaccepted";
    private final String M_URL_DELETE = "http://192.168.1.8:3000/deleteaccepted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounties_accepted);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_list_bounties_accepted);
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

        InternalReader internalReader = new InternalReader(this);
        String placerID = internalReader.readFromFile("ID");
        System.out.println("PLACERID: " + placerID);


        try {
            DownloadBountyList downloadBountyList = new DownloadBountyList();
            downloadBountyList.downloadAccepted(this, M_URL, placerID, new DownloadBountyList.VolleyCallback() {
                @Override
                public void onSuccess(ArrayList<BountyHuntListItem> result) {

                    mBountyHuntListData = result;

                    recyclerView.setLayoutManager(new LinearLayoutManager(context));

                    // This uses a bounty placed adapter
                    mBountyAcceptedAdapter = new BountyAcceptedAdapter(result, context);

                    recyclerView.setAdapter(mBountyAcceptedAdapter);

                    // this basically says to Adapter when itemClickCallBack is called
                    // ill (this Activity) will handle it
                    mBountyAcceptedAdapter.setItemClickCallback(BountiesAccepted.this);

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMapClick(int position) {
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        String foundLat = bountyHuntListItem.getFoundLat();
        String foundLng = bountyHuntListItem.getFoundLng();

        // for now try and switch to google maps and display san fransisco
        String mapQuery = "geo:0,0?q=" + foundLat + "," + foundLng + "(" + "Your bounty was found at this location." + ")";

        Uri gmmIntentUri = Uri.parse(mapQuery);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
        else {
            Toast.makeText(this, "Sorry, need to have google maps installed.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDeleteClick(int position) {
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
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}