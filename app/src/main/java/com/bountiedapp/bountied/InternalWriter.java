package com.bountiedapp.bountied;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import java.io.FileOutputStream;

/**
 * Created by mprovost on 8/11/2016.
 */
public class InternalWriter {

    private Context mContext;

    public InternalWriter(Context context) {
        mContext = context;
    }

    // put the id in internal memory
    public void writeToMemory(final String filename, String stringToWrite) {

        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            fileOutputStream.write(stringToWrite.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
