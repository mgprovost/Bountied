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

import com.bountiedapp.bountied.R;
import com.bountiedapp.bountied.model.BountyFoundListItem;
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

public class BountiesFoundAdapter extends RecyclerView.Adapter<BountiesFoundAdapter.BountyFoundHolder>{

    // endpoint on server to find all possible found images
    private static final String FOUND_IMAGES_BASE_URL = StaticStrings.BASE_URL + "foundimages/";

    // this is the list data passed in through "recyclerview.setAdapter(adapter)" in the activity
    private List<BountyFoundListItem> mBountyFoundListItems;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    private ItemClickCallback mItemClickCallback;

    private LruCache<Integer, Bitmap> mImageCache;

    // this interface allows us to communicate with the Activity without
    // the BountyHuntAdapter class having to hold the Activity in memory
    // *** this is basically a communication channel
    public interface ItemClickCallback {

        // these get call when user clicks in the card
        void onItemClick(int position);
        void onAcceptClick(Context context, int position);
        void onDeclineClick(int position);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback) {
        this.mItemClickCallback = itemClickCallback;
    }

    // constructor for class, takes list data and context of activity
    public BountiesFoundAdapter(List<BountyFoundListItem> bountyFoundListItems, Context context) {

        mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mBountyFoundListItems = bountyFoundListItems;

        // setup the chache for bitmaps
        // equation comes from android docs
        final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        final int chacheSize = maxMemory / 8;
        mImageCache = new LruCache<>(chacheSize);
    }


    public void setListData(ArrayList<BountyFoundListItem> updatedList) {
        this.mBountyFoundListItems.clear();
        this.mBountyFoundListItems.addAll(updatedList);
    }

    // create a new viewholder object, by using an inflater created in ctor
    @Override
    public BountyFoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // inflate the xml layout here
        View view = mLayoutInflater.inflate(R.layout.card_item_found, parent, false);
        return new BountyFoundHolder(view);
    }

    // holder is declared final so we can use it within the network call
    // position is used to grab appropriate data in our list
    @Override
    public void onBindViewHolder(final BountyFoundHolder holder, int position) {

        // gets the appropriate data based on the position in list
        BountyFoundListItem bountyFoundListItem = mBountyFoundListItems.get(position);

        // this is where i can request and set the image....
        Bitmap bitmap = null; //imageCache.get(bountyHuntListItem.getId());
//        if (bitmap != null) {
//            holder.thumbnail.setImageBitmap(bitmap);
//        }
//        else {
        String imageUrl = FOUND_IMAGES_BASE_URL + bountyFoundListItem.getImageURL() + ".jpg";

        Uri uri = Uri.parse(imageUrl);

        Picasso.with(mContext).load(uri).fit().centerCrop().into(holder.bountyImage);

    }

    // tells adapter how many view holder objects it needs to create
    @Override
    public int getItemCount() {
        return mBountyFoundListItems.size();
    }

    // a RecyclerView's adapter needs a view holder class
    // in order to conform to the view holder pattern
    // so we are creating it here
    // the purpose of this is to help us assign data to the appropriate
    // places and represent a single view item of the recycler view
    class BountyFoundHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // ui element
        private ImageView bountyImage;

        // these are used for the onclicklisteners
        private View container;
        private View accept;
        private View decline;

        public BountyFoundHolder(View itemView) {
            super(itemView);

            // references to elements in xml
            bountyImage = (ImageView)itemView.findViewById(R.id.card_image_found);
            container = itemView.findViewById(R.id.card_item_found);
            accept = itemView.findViewById(R.id.card_accept);
            decline = itemView.findViewById(R.id.card_decline);

            // assign an onclick listeners
            container.setOnClickListener(this);
            accept.setOnClickListener(this);
            decline.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // if the view that is clicked is the container
            if (view.getId() == R.id.card_item_found) {
                mItemClickCallback.onItemClick(getAdapterPosition());
            }
            if (view.getId() == R.id.card_accept) {
                mItemClickCallback.onAcceptClick(view.getContext(), getAdapterPosition());
            }
            if (view.getId() == R.id.card_decline) {
                mItemClickCallback.onDeclineClick(getAdapterPosition());
            }
        }
    }
}
