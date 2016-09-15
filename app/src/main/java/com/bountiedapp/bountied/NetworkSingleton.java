package com.bountiedapp.bountied;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**************************************************************************
 * The Network Singleton makes sense instead of instantiating a new
 * object everytime we need to make a request.  Since we are consistantly
 * making network requests, the singleton pattern allows efficiency
 * and minimal overhead.  A single instance of this lasts the lifetime
 * of the app.  Basic implementation recommended by Google.
 **************************************************************************/

public class NetworkSingleton {

    private static NetworkSingleton mNetworkSingletonInstance;
    private static Context mContext;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;


    private NetworkSingleton(Context context) {

        mContext = context;
        mRequestQueue = getRequestQueue();

        // used for caching images
        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    // a single instantce of the network singleton
    public static synchronized NetworkSingleton getInstance(Context context) {
        if (mNetworkSingletonInstance == null) {
            mNetworkSingletonInstance = new NetworkSingleton(context);
        }
        return mNetworkSingletonInstance;
    }

    // get the request queue
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    // add a request to the queue
    public <T> void addToRequestQueue(Request<T> req,String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
