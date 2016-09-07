package com.bountiedapp.bountied.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bountiedapp.bountied.DownloadBountyList;
import com.bountiedapp.bountied.adpter.BountyHuntAdapter;
import com.bountiedapp.bountied.DividerItemDecoration;
import com.bountiedapp.bountied.ui.HomeDetailActivity;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by mprovost on 6/15/2016.
 */

public class BountyHuntData extends Activity implements BountyHuntAdapter.ItemClickCallback {

    private static BountyHuntAdapter mBountyHuntAdapter;
    private static RecyclerView recyclerView;
    private ArrayList mBountyHuntListData;

    // these are just static strings used for the intents
    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_TITLE = "EXTRA_QUOTE";
    private static final String EXTRA_DESCRIPTION = "EXTRA_ATTR";

    private final String M_URL = "http://192.168.1.8:3000/bountylistdownload";

    // holds the downloaded bounties
    ArrayList<BountyHuntListItem> data = new ArrayList<BountyHuntListItem>();

    public BountyHuntData(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void getListData(final Context context) {

        // pull the users lat and lng coordinates to use them in the downloading of the relevant list
        String mLat = "41.018950";
        String mLng = "-74.299850";

        // will eventually get the category as a parameter
        final String CATEGORY = "anything";

        try {
            DownloadBountyList downloadBountyList = new DownloadBountyList();
            downloadBountyList.download(context, M_URL, CATEGORY, mLat, mLng, new DownloadBountyList.VolleyCallback() {
                @Override
                public void onSuccess(ArrayList<BountyHuntListItem> result) {

                    mBountyHuntListData = result;
                    recyclerView.setHasFixedSize(true);
                    // LayoutManager: GridLayoutManager, StaggeredGridLayoutManager, or LinearLayoutManager
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));

                    mBountyHuntAdapter = new BountyHuntAdapter(result, context);


                    //recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
                    recyclerView.setAdapter(mBountyHuntAdapter);

                    // this basically says to Adapter when itemClickCallBack is called
                    // ill (this Activity) will handle it
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mBountyHuntAdapter.setItemClickCallback(this);
    }

    @Override
    public void onItemClick(int position) {
        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);

        Intent intent = new Intent(this, HomeDetailActivity.class);

        Bundle extras = new Bundle();
        extras.putString(EXTRA_TITLE, bountyHuntListItem.getmTitle());
//        extras.putString(EXTRA_DESCRIPTION, bountyHuntListItem.getmDescription());

        intent.putExtra(BUNDLE_EXTRAS, extras);
        startActivity(intent);
    }


    @Override
    public void onHuntClick(int position) {

    }

    @Override
    public void onSaveClick(int position) {

    }
//
//    @Override
//    public void onSecondaryIconClick(int position) {
//        BountyHuntListItem bountyHuntListItem = (BountyHuntListItem) mBountyHuntListData.get(position);
//
//        // pass new data set to the adapter, then update view
//        // MAY NEED TO FIX THIS FOR A LARGE DATA SET, DEPENDS ON HOW I DOWNLOAD DATA
//        mBountyHuntAdapter.setListData(mBountyHuntListData);
//        mBountyHuntAdapter.notifyDataSetChanged();
//    }

}