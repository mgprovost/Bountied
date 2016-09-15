package com.bountiedapp.bountied.ui;


import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bountiedapp.bountied.Upload;
import com.bountiedapp.bountied.ImageConverter;
import com.bountiedapp.bountied.InternalReader;
import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.model.Bounty;
import com.bountiedapp.bountied.model.Gps;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PlaceBounty extends AppCompatActivity implements View.OnClickListener {

    // constants used when returning to the activity from the camera intent and the image picker
    private int PICK_IMAGE_REQUEST = 1;
    private int REQUEST_IMAGE_CAPTURE = 2;

    // floating action button elements and animation elements for the fab
    private FloatingActionButton mFabOpen;
    private FloatingActionButton mFabChoose;
    private FloatingActionButton mFabTakePhoto;
    private FloatingActionButton mFabSubmit;
    private Boolean isFabOpen = false;
    private Animation fab_open;
    private Animation fab_close;
    private Animation rotate_forward;
    private Animation rotate_backward;

    // used in the spinner
    String mCategories[] = { "Catagory", "PEOPLE", "PLACES", "THINGS", "AUTOMOTIVE",
            "ANIMALS", "COLLECTABLES", "FASHION", "FOOD/BEVERAGE", "RESTURANTS/BARS", "SERVICES" };


    private Gps mGPS;

    // UI elements
    private TextView mImageText;
    private ImageView mImageView;
    private EditText mTitleText;
    private EditText mDescriptionText;
    private EditText mBountyText;
    private EditText mRadiusText;
    private Spinner mSpinner;

    // adapter for the spinner
    ArrayAdapter<String> mAdapterCategory;

    // user selection of a category, set the Category as default for error checking purposes
    String mCategory = "Category";

    // the location the image that is chosen is stored in memory temporarily
    private String mImageLocation;

    // custom class that does a number of conversions on a chosen image
    // the class will reduce the size of the image, compress it, and more
    private ImageConverter mImageConverter;

    // this will hold the bitmap of the selected image after it has been resize, rotated, etc...
    private Bitmap mReducedAndRotatedBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set the view from xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_bounty);

        // set the toolbar from xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load a gps object which will begin updating coordinates as they become available
        mGPS = new Gps(this);

        // get references to UI elements
        mImageText = (TextView) findViewById(R.id.place_image_text);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mTitleText = (EditText) findViewById(R.id.place_title_input);
        mDescriptionText = (EditText) findViewById(R.id.place_description_input);
        mBountyText = (EditText) findViewById(R.id.place_bounty_input);
        mRadiusText = (EditText) findViewById(R.id.place_radius_input);
        mSpinner = (Spinner) findViewById(R.id.place_catagory_spinner);

        // initialize and set the array adapter for the spinner to use
        mAdapterCategory = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mCategories);

        // set the dropdown view resource for the spinner to use from a list of stock resources
        mAdapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // link up the spinner with the adapter
        mSpinner.setAdapter(mAdapterCategory);

        // controls what happens when user clicks on a category in the spinner
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                // On selecting a spinner item, set that category for sending to server later
                mCategory = (String)adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // don't need to do anything here if a default is set
            }
        });

        // get a reference to all the floating action buttons on this screen
        mFabOpen = (FloatingActionButton)findViewById(R.id.fab);
        mFabChoose = (FloatingActionButton)findViewById(R.id.fab_choose);
        mFabTakePhoto = (FloatingActionButton)findViewById(R.id.fab_take);
        mFabSubmit = (FloatingActionButton)findViewById(R.id.fab_submit);

        // get a reference to all floating action button animations
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);

        // set the floating action buttons to be clickable
        mFabOpen.setOnClickListener(this);
        mFabChoose.setOnClickListener(this);
        mFabTakePhoto.setOnClickListener(this);
        mFabSubmit.setOnClickListener(this);

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

    // asynchronous method that handles compression of the image user selected
    // once the image is compress and a returned as a string it will send
    // that image to the server along with all relevant field information the user filled out
    private class sendPost extends AsyncTask<Void, Void, String> {

        Context mContext = null;

        public sendPost(Context context) {
            super();
            mContext = context;
        }

        // image user chose is compressed, converted, and returned as string
        @Override
        protected String doInBackground(Void... voids) {

            mImageConverter.compressAndConvertToBase64String();
            String imageAsString = mImageConverter.getImageAsString();
            return imageAsString;

        }

        // the image string returned above is passed as a parameter here
        @Override
        protected void onPostExecute(String imageAsString) {

            // create a reader of internal memory
            InternalReader internalReader = new InternalReader(mContext);

            // get username and ID stored in memory
            String username = internalReader.readFromFile("username");
            String placerID = internalReader.readFromFile("ID");

            // get all other relevant info needed to send to server for a bounty
            String title = mTitleText.getText().toString();
            String description = mDescriptionText.getText().toString();
            String bountyAmount = mBountyText.getText().toString();
            String radius = mRadiusText.getText().toString();


            // sets the last know user coords for gps
            mGPS.setLastKnownLatLng(mContext);

            // pull the users lat and lng coordinates to use them in the downloading of the relevant list
            String lat = Double.toString(mGPS.getLat());
            String lng = Double.toString(mGPS.getLng());

            // stops the gps from listening for new coords which is costly for battery life
            mGPS.stopGps(mContext);

            // create a new "POJO" bounty to send to the server
            Bounty bounty = new Bounty(title, description, bountyAmount, radius, mCategory, lat, lng, imageAsString, username, placerID);

            // use upload class to send network request to send bounty along with image as base64 encoded string
            try {
                Upload upload = new Upload();
                upload.bountyPlaced(mContext, bounty);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // this will take the user to the placed bounties activity after bounty is placed
            Intent placedIntent = new Intent(mContext, BountiesPlaced.class);
            startActivity(placedIntent);
        }


    }

    // called when coming back from external activity (i.e. camera activity or file picker)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if everything is ok with the image picking then do the following
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            // this will get rid of the placeholder text telling the user to select an image
            mImageText.setVisibility(View.GONE);

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


            // the image the user selected is checked for proper orientation, if not it's rotated
            // the size of the image is reduced to something suitable for handheld devices
            // then finally, the image view is set with the newly transformed image
            Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(mImageLocation);
            mImageConverter = new ImageConverter(photoCapturedBitmap, mImageLocation);
            Bitmap reducedImageBitmap = mImageConverter.reduceImageSize();
            mReducedAndRotatedBitmap = mImageConverter.rotateImage(reducedImageBitmap);
            mImageView.setImageBitmap(mReducedAndRotatedBitmap);

        }

        // if everything is ok with the camera activity
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // this will get rid of the placeholder text telling the user to select an image
            mImageText.setVisibility(View.GONE);

            // the image the user selected is checked for proper orientation, if not it's rotated
            // the size of the image is reduced to something suitable for handheld devices
            // then finally, the image view is set with the newly transformed image
            Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(mImageLocation);
            mImageConverter = new ImageConverter(photoCapturedBitmap, mImageLocation);
            Bitmap reducedImageBitmap = mImageConverter.reduceImageSize();
            mReducedAndRotatedBitmap = mImageConverter.rotateImage(reducedImageBitmap);
            mImageView.setImageBitmap(mReducedAndRotatedBitmap);

        }
    }

    // used to copy a file from a source uri (where it is located)
    // to a destination file, using streaming
    public void copy(Uri source, File destination) throws IOException {

        // create an input stream from a uri, then create an output stream from a file
        InputStream inputStream = getContentResolver().openInputStream(source);
        OutputStream outputStream = new FileOutputStream(destination);

        // transfer all bytes from input to output
        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        // close both input and output streams
        inputStream.close();
        outputStream.close();
    }


    // if any of the floating action buttons are clicked
    @Override
    public void onClick(View v) {

        int id = v.getId();

        // the following intents serve to start different activities as buttons are pressed
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

                // create a new temp file for the image to be stored in
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

    // controls animation of the floating action button
    public void animateFAB(){

        if(isFabOpen){

            mFabOpen.startAnimation(rotate_backward);
            mFabChoose.startAnimation(fab_close);
            mFabTakePhoto.startAnimation(fab_close);
            mFabSubmit.startAnimation(fab_close);
            mFabChoose.setClickable(false);
            mFabTakePhoto.setClickable(false);
            mFabSubmit.setClickable(false);
            isFabOpen = false;

        } else {

            mFabOpen.startAnimation(rotate_forward);
            mFabChoose.startAnimation(fab_open);
            mFabTakePhoto.startAnimation(fab_open);
            mFabSubmit.startAnimation(fab_open);
            mFabChoose.setClickable(true);
            mFabTakePhoto.setClickable(true);
            mFabSubmit.setClickable(true);
            isFabOpen = true;
        }
    }

    // creates a temporary file in which we will be putting photos
    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);

        mImageLocation = image.getAbsolutePath();
        return image;
    }

    // open the outside activity which lets you choose an image from a file
    private void showFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // this will check that all fields that must be filled out properly, are properly filled
    // it will return a boolean of true or false if the fields are or are not properly filled
    // it will also set the text in the improperly filled fields to tell user to fill properly
    private boolean validateInput() {

        boolean allValid = true;

        // get all the data the user has currently filled out
        String title = mTitleText.getText().toString();
        String description = mDescriptionText.getText().toString();
        String bounty = mBountyText.getText().toString();
        String radius = mRadiusText.getText().toString();

        // check that the title, description, bounty, and radius entered are valid
        if (!validText(title)) {
            // set the text to red and to say must enter a valid...
            mTitleText.setHint("You need to enter a title in order to submit.");
            mTitleText.setHintTextColor(Color.RED);
            allValid = false;
        }
        if (!validText(description)) {
            // set the text to red and to say must enter a valid...
            mDescriptionText.setHint("You need to enter a description in order to submit.");
            mDescriptionText.setHintTextColor(Color.RED);
            allValid = false;
        }
        if (!vadidBounty(bounty)) {
            // set the text to red and to say must enter a valid...
            mBountyText.setHint("You need to enter a valid integer dollar amount (i.e. 5");
            mBountyText.setHintTextColor(Color.RED);
            allValid = false;
        }
        if (!vadidRadius(radius)) {
            // set the text to red and to say must enter a valid...
            mRadiusText.setHint("You need to enter a valid integer radius amount in miles (i.e. 15)");
            mRadiusText.setHintTextColor(Color.RED);
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

    // check that a valid bounty has been entered between 0 and $1 million
    // these numbers are somewhat arbitrary at the moment
    private boolean vadidBounty(String bounty) {

        int bountyAsInt;

        // if the bounty entered is not an integer, return false
        try{
            bountyAsInt = Integer.parseInt(bounty);
        }catch(NumberFormatException e){
            return false;
        }

        // if the bounty is between 0 & 1 million then it's a valid entry
        if (bountyAsInt > 0 && bountyAsInt < 1000000) {
            return true;
        }
        return false;
    }

    // check that a valid Radius has been entered between 0 and 5000 miles
    // these numbers are somewhat arbitrary at the moment
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
