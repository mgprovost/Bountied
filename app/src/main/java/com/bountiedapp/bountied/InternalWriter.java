package com.bountiedapp.bountied;

import android.content.Context;

import java.io.FileOutputStream;

/*********************************************************************
 * InternalWriter was created to easliy write data to internal memory
 *********************************************************************/

public class InternalWriter {

    private Context mContext;

    public InternalWriter(Context context) {
        mContext = context;
    }

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
