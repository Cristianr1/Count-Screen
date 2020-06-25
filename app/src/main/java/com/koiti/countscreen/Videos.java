package com.koiti.countscreen;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

class Videos {
    private ArrayList<Uri> videosArrayList = new ArrayList<>();
    private ContentResolver contentResolver;

    Videos(ContentResolver contentResolver){
        this.contentResolver = contentResolver;
    }

    public ArrayList<Uri> getVideosArrayList() {
        return videosArrayList;
    }

    /**
     * get video files from the folder CountScreen
     */
    void getVideos() {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(
                uri,
                null,
                "_data" + " like ? ",
                new String[]{"%CountScreen%"},
                null);

        //looping through all rows and adding to list
        assert cursor != null;
        while (cursor.moveToNext()) {
            String data = cursor.getString(cursor.getColumnIndex("_data"));
            Log.d("video", data);
            videosArrayList.add(Uri.parse(data));
        }
        cursor.close();
    }

}
