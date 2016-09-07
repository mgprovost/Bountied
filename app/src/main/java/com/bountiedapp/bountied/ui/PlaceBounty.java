package com.bountiedapp.bountied.ui;


import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bountiedapp.bountied.Upload;
import com.bountiedapp.bountied.ImageConverter;
import com.bountiedapp.bountied.InternalReader;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.model.Bounty;
import com.bountiedapp.bountied.model.Gps;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;



public class PlaceBounty extends AppCompatActivity implements View.OnClickListener {

    private Gps gps;

    private Button buttonChoose;
    private Button buttonTakeImage;
    private Button buttonSubmit;

    private TextView imageText;
    private ImageView mImageView;

    private EditText titleText;
    private EditText descriptionText;
    private EditText bountyText;
    private EditText radiusText;

    // uicontrols
    Spinner spinner;

    //class members
    String categories[] = { "PEOPLE", "PLACES", "THINGS", "AUTOMOTIVE",
            "ANIMALS", "COLLECTABLES", "FASHION", "FOOD/BEVERAGE", "RESTURANTS/BARS", "SERVICES" };
    ArrayAdapter<String> adapterCategory;

    // local members
    String mCategory = "people";

    private Bitmap mRotatedBitmap;
    private Bitmap bitmap;
    private String mImageLocation;

    private ImageConverter imageConverter;
    private Bitmap reducedAndRotatedBitmap;

    private int PICK_IMAGE_REQUEST = 1;
    private int REQUEST_IMAGE_CAPTURE = 2;

    private String ENDPOINT ="http://192.168.1.8:3000";

    private String KEY_IMAGE = "image";
    private String KEY_NAME = "aTitle";

    // floating action buttons and animations for it
    private FloatingActionButton fab, fab_choose, fab_take, fab_submit;
    private Boolean isFabOpen = false;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_bounty);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // May need to load this in the launch screen initially
        gps = new Gps(this);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageText = (TextView) findViewById(R.id.place_image_text);
        mImageView = (ImageView) findViewById(R.id.imageView);

        titleText = (EditText) findViewById(R.id.place_title_input);
        descriptionText = (EditText) findViewById(R.id.place_description_input);
        bountyText = (EditText) findViewById(R.id.place_bounty_input);
        radiusText = (EditText) findViewById(R.id.place_radius_input);

        spinner = (Spinner) findViewById(R.id.place_catagory_spinner);


        // initialize and set the array adapter for the spinner to use
        adapterCategory = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categories);

        // set the dropdown view resource for the spinner to use from a list of stock resources
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // link up the spinner with the adapter
        spinner.setAdapter(adapterCategory);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                // On selecting a spinner item
                mCategory = (String)adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // don't need to do anything here if a default is set
            }
        });

        // get a reference to all the floating action buttons on this screen
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab_choose = (FloatingActionButton)findViewById(R.id.fab_choose);
        fab_take = (FloatingActionButton)findViewById(R.id.fab_take);
        fab_submit = (FloatingActionButton)findViewById(R.id.fab_submit);

        // get a reference to all floating action button animations
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);

        // set the floating action buttons to be clickable
        fab.setOnClickListener(this);
        fab_choose.setOnClickListener(this);
        fab_take.setOnClickListener(this);
        fab_submit.setOnClickListener(this);

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


    private class sendPost extends AsyncTask<Void, Void, String> {

        Context mContext = null;

        public sendPost(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected String doInBackground(Void... voids) {

            imageConverter.compressAndConvertToBase64String();
            String imageAsString = imageConverter.getImageAsString();
            return imageAsString;

        }


        @Override
        protected void onPostExecute(String imageAsString) {

            InternalReader internalReader = new InternalReader(mContext);

            String username = internalReader.readFromFile("username");
            String title = titleText.getText().toString();
            String description = descriptionText.getText().toString();
            String bountyAmount = bountyText.getText().toString();
            String radius = radiusText.getText().toString();
            String placerID = internalReader.readFromFile("ID");

            // sets the last know user coords for gps
            gps.setLastKnownLatLng(mContext);

            // pull the users lat and lng coordinates to use them in the downloading of the relevant list
            String lat = Double.toString(gps.getmLat());
            String lng = Double.toString(gps.getmLng());

            // stops the gps from listening for new coords which is costly for battery life
            gps.stopGps(mContext);

            Bounty bounty = new Bounty(title, description, bountyAmount, radius, mCategory, lat, lng, imageAsString, username, placerID);
            //do stuff
            try {
                Upload upload = new Upload();
                upload.bountyPlaced(mContext, bounty);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // this will take the user the placed bounties activity
            Intent placedIntent = new Intent(mContext, BountiesPlaced.class);
            startActivity(placedIntent);
        }


    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);

        mImageLocation = image.getAbsolutePath();
        return image;
    }


    private void showFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            // this will get rid of the placeholder text telling the user to select an image
            imageText.setVisibility(View.GONE);

            // create a destination file which is where the temporary image will be placed
            File destinationFile = null;

            try {
                destinationFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // the uri will be where the image to be copied currently is located
            Uri sourceUri = data.getData();

            // copy the image that the user selected to the destination temporary file
            try {
                copy(sourceUri, destinationFile);
            } catch (IOException e) {
                e.printStackTrace();
            }


            Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(mImageLocation);
            imageConverter = new ImageConverter(photoCapturedBitmap, mImageLocation);
            Bitmap reducedImageBitmap = imageConverter.reduceImageSize();
            reducedAndRotatedBitmap = imageConverter.rotateImage(reducedImageBitmap);
            mImageView.setImageBitmap(reducedAndRotatedBitmap);

        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // this will get rid of the placeholder text telling the user to select an image
            imageText.setVisibility(View.GONE);


            Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(mImageLocation);
            imageConverter = new ImageConverter(photoCapturedBitmap, mImageLocation);
            Bitmap reducedImageBitmap = imageConverter.reduceImageSize();
            reducedAndRotatedBitmap = imageConverter.rotateImage(reducedImageBitmap);
            mImageView.setImageBitmap(reducedAndRotatedBitmap);

        }

    }

    // used to copy a file from a source uri (where it is located)
    // to a destination file, using streams of input and output
    public void copy(Uri src, File dest) throws IOException {

        // create an input stream from a uri, then create an output stream from a file
        InputStream inputStream = getContentResolver().openInputStream(src);
        OutputStream outputStream = new FileOutputStream(dest);

        // Transfer all bytes from input to output
        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        // close both input and output streams
        inputStream.close();
        outputStream.close();
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id){
            case R.id.fab:

                animateFAB();
                break;
            case R.id.fab_choose:

                showFileChooser();
                break;
            case R.id.fab_take:
                Intent cameraApplicationIntent = new Intent();
                cameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

                File photoFile = null;

                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraApplicationIntent, REQUEST_IMAGE_CAPTURE);

                break;
            case R.id.fab_submit:

                if (validateInput()) {
                    new sendPost(v.getContext()).execute();
                }
                break;
        }
    }

    public void animateFAB(){

        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fab_choose.startAnimation(fab_close);
            fab_take.startAnimation(fab_close);
            fab_submit.startAnimation(fab_close);
            fab_choose.setClickable(false);
            fab_take.setClickable(false);
            fab_submit.setClickable(false);
            isFabOpen = false;
        } else {

            fab.startAnimation(rotate_forward);
            fab_choose.startAnimation(fab_open);
            fab_take.startAnimation(fab_open);
            fab_submit.startAnimation(fab_open);
            fab_choose.setClickable(true);
            fab_take.setClickable(true);
            fab_submit.setClickable(true);
            isFabOpen = true;
        }
    }

    // this will check that all fields that must be filled out are
    // will return an array list with the fields that are not filled out properly
    private boolean validateInput() {

        boolean allValid = true;

        String title = titleText.getText().toString();
        String description = descriptionText.getText().toString();
        String bounty = bountyText.getText().toString();
        String radius = radiusText.getText().toString();

        // check that the title, description, bounty, and radius entered are valid
        if (!validText(title)) {
            // set the text to red and to say must enter a valid...
            titleText.setHint("You need to enter a title in order to submit.");
            titleText.setHintTextColor(Color.RED);
            allValid = false;
        }
        if (!validText(description)) {
            // set the text to red and to say must enter a valid...
            descriptionText.setHint("You need to enter a description in order to submit.");
            descriptionText.setHintTextColor(Color.RED);
            allValid = false;
        }
        if (!vadidBounty(bounty)) {
            // set the text to red and to say must enter a valid...
            bountyText.setHint("You need to enter a valid integer dollar amount (i.e. 5");
            bountyText.setHintTextColor(Color.RED);
            allValid = false;
        }
        if (!vadidRadius(radius)) {
            // set the text to red and to say must enter a valid...
            radiusText.setHint("You need to enter a valid integer radius amount in miles (i.e. 15)");
            radiusText.setHintTextColor(Color.RED);
            allValid = false;
        }

        // if all fields are valid return true, otherwise return false
        if (allValid) {
            return true;
        }
        return false;

    }

    // if the text field is not empty it is valid
    private boolean validText(String title) {
        if (title.equals("")) {
            return false;
        }
        return true;
    }

    // // check that a valid bounty has been entered between 0 and $1 million
    private boolean vadidBounty(String bounty) {

        int bountyAsInt;

        try{
            bountyAsInt = Integer.parseInt(bounty);
        }catch(NumberFormatException e){
            return false;
        }

        if (bountyAsInt > 0 && bountyAsInt < 1000000) {
            return true;
        }
        return false;
    }

    // check that a valid Radius has been entered between 0 and 5000 miles
    private boolean vadidRadius(String radius) {

        int radiusAsInt;

        // try to parse the String as an integer, if can't, its invalid, return false
        try{
            radiusAsInt = Integer.parseInt(radius);
        }catch(NumberFormatException e){
            return false;
        }

        // if the radius is between 0 and 5000 (max radius) return true
        if (radiusAsInt > 0 && radiusAsInt < 5000) {
            return true;
        }
        return false;
    }

}
