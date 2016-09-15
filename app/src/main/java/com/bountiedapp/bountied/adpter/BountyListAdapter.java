package com.bountiedapp.bountied.adpter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bountiedapp.bountied.R;

import java.util.ArrayList;

/*************************************************************************
 *  This is a Basic List Adapter, NOT to be used with a recycler view,
 *  simply to be used with a simple listview.
 *************************************************************************/


public class BountyListAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private ArrayList<String> mBountyCatagoryList;

    public BountyListAdapter(Context context, int resource, ArrayList<String> bountyCatagoryList) {
        super(context, resource, bountyCatagoryList);
        mContext = context;
        mBountyCatagoryList = bountyCatagoryList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_catagory, parent, false);

        String catagory = mBountyCatagoryList.get(position);
        TextView textView = (TextView) view.findViewById(R.id.text_catagory);
        textView.setText(catagory);

        return view;
    }
}
