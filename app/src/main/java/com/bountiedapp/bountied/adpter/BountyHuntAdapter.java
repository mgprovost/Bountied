package com.bountiedapp.bountied.adpter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.model.BountyHuntListItem;
import com.bountiedapp.bountied.model.StaticStrings;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/*****************************************************************************
 * The adapter classes are built to set all the data that is passed in
 * properly into views(xml layout) that we also pass this class.
 * It then works with a recycler view to create many instances of the views,
 * while keeping track of which data is where in the list.
 * These adapters/recycler views are much quick than the older list views.
 * They are also much more efficient as the recycler views simply "recycle"
 * the old objects instead of create a large supply of them.
 *
 * Different adapters correspond with different types of data and views
 *****************************************************************************/


public class BountyHuntAdapter extends RecyclerView.Adapter<BountyHuntAdapter.BountyHuntHolder>{

    // endpoint on our server for bounty images
    private static final String BOUNTY_IMAGES_BASE_URL = StaticStrings.BASE_URL + "images/";

    // this is the list data passed in through "recyclerview.setAdapter(adapter)" in the activity
    private List<BountyHuntListItem> mBountyHuntListData;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    private ItemClickCallback mItemClickCallback;

    private LruCache<Integer, Bitmap> imageCache;

    // this interface allows us to communicate with the Activity without
    // the BountyHuntAdapter class having to hold the Activity in memory
    // ** this is basically a communication channel
    public interface ItemClickCallback {
        // these get call when user clicks in the card
        void onItemClick(int position);
        void onHuntClick(int position);
        void onSaveClick(int position);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback) {
        this.mItemClickCallback = itemClickCallback;
    }

    // constructor for class, takes list data and context of activity
    public BountyHuntAdapter(List<BountyHuntListItem> bountyHuntListData, Context context) {

        mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mBountyHuntListData = bountyHuntListData;

        // setup the chache for bitmaps
        // equation comes from android docs
        final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        final int chacheSize = maxMemory / 8;
        imageCache = new LruCache<>(chacheSize);
    }


    public void setListData(ArrayList<BountyHuntListItem> updatedList) {
        this.mBountyHuntListData.clear();
        this.mBountyHuntListData.addAll(updatedList);
    }

    // create a new viewholder object, by using an inflater created in ctor
    @Override
    public BountyHuntHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // inflate the xml layout here
        View view = mLayoutInflater.inflate(R.layout.card_item, parent, false);
        return new BountyHuntHolder(view);
    }

    // holder is declared final so we can use it within the network call
    // position is used to grab appropriate data in our list
    @Override
    public void onBindViewHolder(final BountyHuntHolder holder, int position) {

        // gets the appropriate data based on the position in list
        BountyHuntListItem bountyHuntListItem = mBountyHuntListData.get(position);

        // this is where i can request and set the image....
        // the following variables correspond with the holder class below
        holder.title.setText(bountyHuntListItem.getTitle());
        holder.description.setText(bountyHuntListItem.getDescription());
        holder.bounty.setText("$" + bountyHuntListItem.getBounty());

        // Display image in ImageView widget
        Bitmap bitmap = null; //imageCache.get(bountyHuntListItem.getId());
//        if (bitmap != null) {
//            holder.thumbnail.setImageBitmap(bitmap);
//        }
//        else {
        String imageUrl = BOUNTY_IMAGES_BASE_URL + bountyHuntListItem.getImageUrl() + ".jpg";

        Uri uri = Uri.parse(imageUrl);

        Picasso.with(mContext).load(uri).fit().centerCrop().into(holder.bountyImage);

    }

    // tells adapter how many view holder objects it needs to create
    @Override
    public int getItemCount() {
        return mBountyHuntListData.size();
    }

    // a RecyclerView's adapter needs a view holder class
    // in order to conform to the view holder pattern
    // so we are creating it here
    // purpose of this is to help us assign data to the appropriate
    // places and represent a single view item of the recycler view
    class BountyHuntHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // ui elements
        private TextView title;
        private TextView description;
        private ImageView bountyImage;
        private TextView bounty;

        // these are used for the onclicklisteners
        private View container;
        private View hunt;
        private View save;

        public BountyHuntHolder(View itemView) {
            super(itemView);

            // references to elements in xml
            title = (TextView)itemView.findViewById(R.id.card_title);
            description = (TextView)itemView.findViewById(R.id.card_description);
            bountyImage = (ImageView)itemView.findViewById(R.id.card_image);
            bounty = (TextView)itemView.findViewById(R.id.card_bounty);
            container = itemView.findViewById(R.id.card_item);
            hunt = itemView.findViewById(R.id.card_capture);
            save = itemView.findViewById(R.id.card_save);

            // assign an onclick listener to the container as to track clicks of the entire container
            container.setOnClickListener(this);
            hunt.setOnClickListener(this);
            save.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // if the view that is clicked is the container
            if (view.getId() == R.id.card_item) {
                mItemClickCallback.onItemClick(getAdapterPosition());
            }
            if (view.getId() == R.id.card_capture) {
                mItemClickCallback.onHuntClick(getAdapterPosition());
            }
            if (view.getId() == R.id.card_save) {
                mItemClickCallback.onSaveClick(getAdapterPosition());
            }
        }
    }
}
