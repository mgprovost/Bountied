package com.bountiedapp.bountied;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/***************************************************
 * The Internal Reader Class was created to easily
 * read data from internal memory
 ***************************************************/

public class InternalReader {

    private Context mContext;

    public InternalReader(Context context) {
        mContext = context;
    }

    // read a string of data from a file saved in internal memory
    public String readFromFile(String filename) {

        String returnString = "";

        try {
            InputStream inputStream = mContext.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                returnString = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("internal reader", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("internal reader", "Can not read file: " + e.toString());
        }

        return returnString;
    }

}
