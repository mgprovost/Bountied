package com.bountiedapp.bountied;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class PlaceBounty extends AppCompatActivity implements View.OnClickListener {

    private Button buttonChoose;
    private Button buttonUpload;

    private ImageView imageView;

    private EditText editTextName;

    private Bitmap bitmap;

    private int PICK_IMAGE_REQUEST = 1;

    private String UPLOAD_URL ="http://192.168.1.7:3000/placebounty";
    private String ENDPOINT ="http://192.168.1.7:3000";

    private String KEY_IMAGE = "image";
    private String KEY_NAME = "aTitle";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_bounty);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is a method that allows older versions of
        // android to display a return arrow to appear
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);

        editTextName = (EditText) findViewById(R.id.editText);

        imageView = (ImageView) findViewById(R.id.imageView);

        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);

        final Button button = (Button) findViewById(R.id.submit);


        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                new sendPost(v.getContext()).execute();

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
                Intent huntIntent = new Intent(this, ViewBountyActivity.class);
                startActivity(huntIntent);
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

            ExifInterface exif = null;
            try {
                exif = new ExifInterface("storage/extSdCard/room.jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            System.out.println(orientation);
            Bitmap bmp = BitmapFactory.decodeFile("storage/extSdCard/room.jpg");

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

            ImageUploadConverter imageUploadConverter = new ImageUploadConverter();
            imageUploadConverter.reduceImageSize(rotatedBitmap, 450, 700);
            imageUploadConverter.compressAndConvertToBase64String();
            String imageAsString = imageUploadConverter.getImageAsString();
            return imageAsString;

        }


        @Override
        protected void onPostExecute(String imageAsString) {
            //do stuff
            try {
                ImageUpload imageUpload = new ImageUpload(mContext, UPLOAD_URL, imageAsString, "room",
                        "some room", "100", "10");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }


    // takes a bitmap and resizes it, then returns the resized bitmap
    public Bitmap resizeImage(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 1200, 800, true);
        Log.d("resize", "making it resize");
        return resizedBitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private Bitmap storeImage(Bitmap image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 30, bytes);

        //you can create a new file name "test.jpg" in sdcard folder.
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + "test19.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = null;
            fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;

    }


    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="room2" +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }



//    public String getStringImage(Bitmap bitmap){
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        //byte[] imageBytes = baos.toByteArray();
//        String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
//        return encodedImage;
//    }
//
//    private void uploadImage() throws JSONException {
//        //Showing the progress dialog
//        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
//        HashMap<String, String> params = new HashMap<String, String>();
//        //Converting Bitmap to String
//        String image = getStringImage(bitmap);
//        Log.d("image", image);
//        //Getting Image Name
//        String name = editTextName.getText().toString().trim();
//        JSONObject jsonObject = new JSONObject();
//        //Adding parameters
//        jsonObject.put("image", image);
//        jsonObject.put("name", "hello");
//        params.put(KEY_IMAGE, image);
//        params.put(KEY_NAME, name);
//
//
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, UPLOAD_URL, jsonObject,
//
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        //Disimissing the progress dialog
//                        loading.dismiss();
//                        //Showing toast message of the response
//                        Toast.makeText(PlaceBounty.this, response.toString(), Toast.LENGTH_LONG).show();
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError volleyError) {
//                        //Dismissing the progress dialog
//                        loading.dismiss();
//
//                        //Showing toast
//                        Toast.makeText(PlaceBounty.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
//                    }
//                });
//            @Override
//            protected Map<String, String> getParams() {
//                //Converting Bitmap to String
//
//                String image = getStringImage(bitmap);
//                Log.d("image", image);
//                //Getting Image Name
//                String name = editTextName.getText().toString().trim();
//
//                //Creating parameters
//                Map<String,String> params = new Hashtable<String, String>();
//
//                //Adding parameters
//                params.put(KEY_IMAGE, image);
//                params.put(KEY_NAME, name);
//
//                //returning parameters
//                return params;
//            }
//        };

    //Creating a Request Queue
    //RequestQueue requestQueue = Volley.newRequestQueue(this);

    //Adding request to the queue
    //requestQueue.add(req);
//    }

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
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {

        if(v == buttonChoose){
            showFileChooser();
        }

        if(v == buttonUpload){
//            try {
//                //uploadImage();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
        }
    }

}
