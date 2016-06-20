package com.bountiedapp.bountied.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.bountiedapp.bountied.PlaceBounty;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.ViewBountyActivity;
import com.bountiedapp.bountied.adpter.BountyHuntAdapter;
import com.bountiedapp.bountied.model.BountyHuntData;
import com.bountiedapp.bountied.model.BountyHuntListItem;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements BountyHuntAdapter.ItemClickCallback {

    // these are just static strings used for the intents
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_QUOTE = "EXTRA_QUOTE";
    private static final String EXTRA_ATTR = "EXTRA_ATTR";


    private RecyclerView recyclerView;
    private BountyHuntAdapter bountyHuntAdapter;
    private ArrayList bountyHuntListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //hiding default app icon
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        //displaying custom ActionBar
        View mActionBarView = getLayoutInflater().inflate(R.layout.my_action_bar, null);
        actionBar.setCustomView(mActionBarView);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);


        bountyHuntListData = (ArrayList) BountyHuntData.getListData();

        recyclerView = (RecyclerView)findViewById(R.id.recycler_list);

        // LayoutManager: GridLayoutManager, StaggeredGridLayoutManager, or LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        bountyHuntAdapter = new BountyHuntAdapter(BountyHuntData.getListData(), this);
        recyclerView.setAdapter(bountyHuntAdapter);

        // this basically says to Adapter when itemClickCallBack is called
        // ill (this Activity) will handle it
        bountyHuntAdapter.setItemClickCallback(this);

//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setLogo(R.drawable.ic_logo);
//        actionBar.setDisplayUseLogoEnabled(true);
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
                Intent huntIntent = new Intent(this, ViewBountyActivity.class);
                startActivity(huntIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleView (View view) {
        Log.d("Working", "toggleView");
    }

    @Override
    public void onItemClick(int position) {
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) bountyHuntListData.get(position);

        Intent intent = new Intent(this, HomeDetailActivity.class);

        Bundle extras = new Bundle();
        extras.putString(EXTRA_QUOTE, bountyHuntListItem.getTitle());
        extras.putString(EXTRA_ATTR, bountyHuntListItem.getSubTitle());

        intent.putExtra(BUNDLE_EXTRAS, extras);
        startActivity(intent);
    }

    @Override
    public void onSecondaryIconClick(int position) {
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) bountyHuntListData.get(position);

        // update the data
        if (bountyHuntListItem.isFavourite()) {
            bountyHuntListItem.setFavourite(false);
        }
        else {
            bountyHuntListItem.setFavourite(true);
        }

        // pass new data set to the adapter, then update view
        // MAY NEED TO FIX THIS FOR A LARGE DATA SET, DEPENDS ON HOW I DOWNLOAD DATA
        bountyHuntAdapter.setListData(bountyHuntListData);
        bountyHuntAdapter.notifyDataSetChanged();
    }
}
