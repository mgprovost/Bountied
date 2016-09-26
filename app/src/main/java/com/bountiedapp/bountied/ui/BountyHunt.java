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

public class BountyHunt extends AppCompatActivity {

    // list to store the string categories in
    ArrayList mCatagoryList;

    // static strings used for the intent
    private static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set the view from xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounty_hunt);

        // set the toolbar from xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // add any catagories to list here
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

        // create a generic list adapter to display to list info with
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this,
                        R.layout.item_catagory,
                        R.id.text_catagory,
                        mCatagoryList);

        // get a reference to the listview
        ListView listView = (ListView) findViewById(R.id.list_catagories);

        // set the adapter on the listview
        listView.setAdapter(adapter);

        // handle all clicks on the list here
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(BountyHunt.this, BountyHuntCatagory.class);

                // whatever category is selected from the list, send that to the next activity
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

}
