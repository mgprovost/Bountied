package com.bountiedapp.bountied.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bountiedapp.bountied.R;

import java.util.ArrayList;

public class BountyHuntActivity extends AppCompatActivity {

    ArrayList mCatagoryList;

    // these are just static strings used for the intents
    private static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounty_hunt);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // download the catagory list here
        mCatagoryList = new ArrayList<String>();
        mCatagoryList.add("PEOPLE");
        mCatagoryList.add("PLACES");
        mCatagoryList.add("THINGS");
        mCatagoryList.add("AUTOMOTIVE");
        mCatagoryList.add("ANIMALS");
        mCatagoryList.add("COLLECTABLES");
        mCatagoryList.add("FASHION");
        mCatagoryList.add("FOOD/BEVERAGE");
        mCatagoryList.add("RESTURANTS/BARS");
        mCatagoryList.add("SERVICES");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this,
                        R.layout.item_catagory,
                        R.id.text_catagory,
                        mCatagoryList);

        ListView listView = (ListView) findViewById(R.id.list_catagories);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(BountyHuntActivity.this, BountyHuntCatagoryActivity.class);

                String catagory = (String) mCatagoryList.get(position);
                intent.putExtra(EXTRA_CATEGORY, catagory);
                startActivity(intent);
            }
        });


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



}
