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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mprovost on 6/15/2016.
 */
public class BountyAcceptedAdapter extends RecyclerView.Adapter<BountyAcceptedAdapter.BountyHuntHolder>{

    private static final String BOUNTY_IMAGES_BASE_URL = "http://192.168.1.8:3000/images/";

    private List<BountyHuntListItem> bountyHuntListData;
    private LayoutInflater layoutInflater;
    private Context mContext;

    private ItemClickCallback itemClickCallback;

    private LruCache<Integer, Bitmap> imageCache;

    // this interface allows us to communicate with the Activity without
    // the BountyHuntAdapter class having to hold the Activity in memory
    // ** this is basically a communication channel
    public interface ItemClickCallback {
        // this gets called whenever user clicks anything other than the secondary icon
        void onMapClick(int position);
        void onDeleteClick(int position);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback) {
        this.itemClickCallback = itemClickCallback;
    }

    public BountyAcceptedAdapter(List<BountyHuntListItem> bountyHuntListData, Context context) {

        mContext = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.bountyHuntListData = bountyHuntListData;

        // setup the chache for bitmaps
        // equation comes from android docs
        final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        final int chacheSize = maxMemory / 8;
        imageCache = new LruCache<>(chacheSize);
    }

//    public void setBountyHuntListData(ArrayList<Bounty> listBounties ) {
//        this.bountyHuntListData
//    }


    public void setListData(ArrayList<BountyHuntListItem> updatedList) {
        this.bountyHuntListData.clear();
        this.bountyHuntListData.addAll(updatedList);
    }

    @Override
    public BountyHuntHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.card_item_accepted, parent, false);
        return new BountyHuntHolder(view);
    }

    // holder is declared final so we can use it within the network call
    @Override
    public void onBindViewHolder(final BountyHuntHolder holder, int position) {

        BountyHuntListItem bountyHuntListItem = bountyHuntListData.get(position);

        // this is where i think i can request and set the image....
        holder.title.setText(bountyHuntListItem.getmTitle());
        holder.description.setText(bountyHuntListItem.getmDescription());
        holder.bounty.setText("$" + bountyHuntListItem.getmBounty());

        // Display image in ImageView widget
        // NEED TO DEFINE .getID & ID etc..... ALSO NEED TO SETUP getBitmap
        Bitmap bitmap = null; //imageCache.get(bountyHuntListItem.getId());
//        if (bitmap != null) {
//            holder.thumbnail.setImageBitmap(bitmap);
//        }
//        else {
        String imageUrl = BOUNTY_IMAGES_BASE_URL + bountyHuntListItem.getmImageUrl() + ".jpg";

        Uri uri = Uri.parse(imageUrl);

        Picasso.with(mContext).load(uri).fit().centerCrop().into(holder.bountyImage);

    }

    @Override
    public int getItemCount() {
        return bountyHuntListData.size();
    }

    // a RecyclerView's adapter needs a view holder class
    // in order to conform to the view holder pattern
    // so we are creating it here
    // purpose of this is to help us assign data to the appropriate
    // places and represent a single view item of the recycler view
    class BountyHuntHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title;
        private TextView description;
        private TextView bounty;
        private ImageView bountyImage;
        private TextView deleteIcon;
        private TextView mapIcon;

        public BountyHuntHolder(View itemView) {
            super(itemView);

            title = (TextView)itemView.findViewById(R.id.card_title);
            description = (TextView)itemView.findViewById(R.id.card_description);
            bounty = (TextView)itemView.findViewById(R.id.card_bounty);
            bountyImage = (ImageView)itemView.findViewById(R.id.card_image);

            mapIcon = (TextView)itemView.findViewById(R.id.card_map);
            mapIcon.setOnClickListener(this);

            deleteIcon = (TextView)itemView.findViewById(R.id.card_delete);
            deleteIcon.setOnClickListener(this);
            // assign an onclick listener to the container as to track clicks of the entire container
        }

        @Override
        public void onClick(View view) {
            // if the view that is clicked is the container
            if (view.getId() == R.id.card_map) {
                itemClickCallback.onMapClick(getAdapterPosition());
            }
            if (view.getId() == R.id.card_delete) {
                itemClickCallback.onDeleteClick(getAdapterPosition());
            }
        }
    }
}
