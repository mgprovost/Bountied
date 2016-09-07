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
import com.bountiedapp.bountied.model.BountyFoundListItem;
import com.bountiedapp.bountied.model.BountyHuntListItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mprovost on 6/15/2016.
 */
public class BountiesFoundAdapter extends RecyclerView.Adapter<BountiesFoundAdapter.BountyFoundHolder>{

    private static final String FOUND_IMAGES_BASE_URL = "http://192.168.1.8:3000/foundimages/";

    private List<BountyFoundListItem> bountyFoundListItems;
    private LayoutInflater layoutInflater;
    private Context mContext;

    private ItemClickCallback itemClickCallback;

    private LruCache<Integer, Bitmap> imageCache;

    // this interface allows us to communicate with the Activity without
    // the BountyHuntAdapter class having to hold the Activity in memory
    // ** this is basically a communication channel
    public interface ItemClickCallback {
        // this gets called whenever user clicks anything other than the secondary icon
        void onItemClick(int position);
        void onAcceptClick(Context context, int position);
        void onDeclineClick(int position);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback) {
        this.itemClickCallback = itemClickCallback;
    }

    public BountiesFoundAdapter(List<BountyFoundListItem> bountyFoundListItems, Context context) {

        mContext = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.bountyFoundListItems = bountyFoundListItems;

        // setup the chache for bitmaps
        // equation comes from android docs
        final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        final int chacheSize = maxMemory / 8;
        imageCache = new LruCache<>(chacheSize);
    }

//    public void setBountyHuntListData(ArrayList<Bounty> listBounties ) {
//        this.bountyHuntListData
//    }


    public void setListData(ArrayList<BountyFoundListItem> updatedList) {
        this.bountyFoundListItems.clear();
        this.bountyFoundListItems.addAll(updatedList);
    }

    @Override
    public BountyFoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.card_item_found, parent, false);
        return new BountyFoundHolder(view);
    }

    // holder is declared final so we can use it within the network call
    @Override
    public void onBindViewHolder(final BountyFoundHolder holder, int position) {

        BountyFoundListItem bountyFoundListItem = bountyFoundListItems.get(position);

        // Display image in ImageView widget
        // NEED TO DEFINE .getID & ID etc..... ALSO NEED TO SETUP getBitmap
        Bitmap bitmap = null; //imageCache.get(bountyHuntListItem.getId());
//        if (bitmap != null) {
//            holder.thumbnail.setImageBitmap(bitmap);
//        }
//        else {
        String imageUrl = FOUND_IMAGES_BASE_URL + bountyFoundListItem.getImageURL() + ".jpg";

        Uri uri = Uri.parse(imageUrl);

        Picasso.with(mContext).load(uri).fit().centerCrop().into(holder.bountyImage);

    }

    @Override
    public int getItemCount() {
        return bountyFoundListItems.size();
    }

    // a RecyclerView's adapter needs a view holder class
    // in order to conform to the view holder pattern
    // so we are creating it here
    // purpose of this is to help us assign data to the appropriate
    // places and represent a single view item of the recycler view
    class BountyFoundHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView bountyImage;
        private View container;
        private View accept;
        private View decline;

        public BountyFoundHolder(View itemView) {
            super(itemView);

            bountyImage = (ImageView)itemView.findViewById(R.id.card_image_found);
            container = itemView.findViewById(R.id.card_item_found);
            accept = itemView.findViewById(R.id.card_accept);
            decline = itemView.findViewById(R.id.card_decline);

            // assign an onclick listener to the container as to track clicks of the entire container
            container.setOnClickListener(this);
            accept.setOnClickListener(this);
            decline.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // if the view that is clicked is the container
            if (view.getId() == R.id.card_item_found) {
                itemClickCallback.onItemClick(getAdapterPosition());
            }
            if (view.getId() == R.id.card_accept) {
                itemClickCallback.onAcceptClick(view.getContext(), getAdapterPosition());
            }
            if (view.getId() == R.id.card_decline) {
                itemClickCallback.onDeclineClick(getAdapterPosition());
            }
        }
    }
}
