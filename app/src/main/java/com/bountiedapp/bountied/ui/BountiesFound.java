package com.bountiedapp.bountied.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bountiedapp.bountied.DownloadBountyList;
import com.bountiedapp.bountied.NetworkRequest;
import com.bountiedapp.bountied.NetworkSingleton;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.adpter.BountiesFoundAdapter;
import com.bountiedapp.bountied.model.BountyFoundListItem;
import com.bountiedapp.bountied.model.Gps;

import org.json.JSONException;

import java.util.ArrayList;

public class BountiesFound extends AppCompatActivity implements BountiesFoundAdapter.ItemClickCallback {

    private static BountiesFoundAdapter mBountyFoundAdapter;
    private static RecyclerView recyclerView;
    private ArrayList mBountyFoundListData;
    private Gps gps;

    // these are just static strings used for the intents
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_FOUNDARRAY = "EXTRA_FOUNDARRAY";
    private static final String EXTRA_IMAGEURL = "EXTRA_IMAGEURL";

    private ArrayList foundIDs = new ArrayList();


    private final String M_URL = "http://192.168.1.8:3000/downloadbountiesfound";
    private final String M_URL_ACCEPT = "http://192.168.1.8:3000/acceptbounty";
    private final String M_URL_DECLINE = "http://192.168.1.8:3000/declinebounty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounties_found);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // May need to load this in the launch screen initially
        gps = new Gps(this);

        Bundle extras = getIntent().getBundleExtra(BUNDLE_EXTRAS);

        foundIDs = extras.getCharSequenceArrayList(EXTRA_FOUNDARRAY);

        System.out.println("Found ID's on Bounties Found page: " + foundIDs.toString() );


        recyclerView = (RecyclerView)findViewById(R.id.recycler_list_bounties_found);
        getListData(this);

     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }
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
            // take this out.....
//                // Instantiate the RequestQueue.
//                String url ="http://192.168.1.6:3000/firebase";
//
//                // Request a string response from the provided URL.
//                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
//                        new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
//                                // Display the first 500 characters of the response string.
//                                //mTextView.setText("Response is: "+ response.substring(0,500));
//                            }
//                        }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        //mTextView.setText("That didn't work!");
//                    }
//                });
//                NetworkSingleton.getInstance(this).addToRequestQueue(stringRequest, "FirebaseRequestTAG");

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

    public void toggleView (View view) {
        Log.d("Working", "toggleView");
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

        // will eventually get the category as a parameter
        final String CATEGORY = "anything";


        try {
            DownloadBountyList downloadBountyList = new DownloadBountyList();
            downloadBountyList.downloadBountiesFound(this, M_URL, foundIDs, new DownloadBountyList.VolleyCallbackTwo() {
                @Override
                public void onFinish(ArrayList<BountyFoundListItem> result) {

                    mBountyFoundListData = result;

                    for (int i = 0; i < mBountyFoundListData.size(); i++) {
                        System.out.println(mBountyFoundListData.get(i).toString());
                    }

                    recyclerView.setLayoutManager(new LinearLayoutManager(context));

                    mBountyFoundAdapter = new BountiesFoundAdapter(result, context);

                    recyclerView.setAdapter(mBountyFoundAdapter);

                    // this basically says to Adapter when itemClickCallBack is called
                    // ill (this Activity) will handle it
                    mBountyFoundAdapter.setItemClickCallback(BountiesFound.this);

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(int position) {
        BountyFoundListItem bountyFoundListItem = (BountyFoundListItem) mBountyFoundListData.get(position);

        Intent intent = new Intent(this, FullImageActivity.class);

        Bundle extras = new Bundle();

        extras.putString(EXTRA_IMAGEURL, bountyFoundListItem.getImageURL());
        intent.putExtra(BUNDLE_EXTRAS, extras);

        startActivity(intent);
    }

    @Override
    public void onAcceptClick(final Context context, int position) {

        BountyFoundListItem bountyFoundListItem = (BountyFoundListItem) mBountyFoundListData.get(position);
        String foundID = bountyFoundListItem.getFoundID();

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

    @Override
    public void onDeclineClick(int position) {
        BountyFoundListItem bountyFoundListItem = (BountyFoundListItem) mBountyFoundListData.get(position);
        String foundID = bountyFoundListItem.getFoundID();

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
