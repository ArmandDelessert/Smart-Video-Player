package hesso.smartvideoplayer;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Armand Delessert on 17.06.2017.
 */

public class VideoListManager extends AppCompatActivity {

    private List<Video> videoItemsList;
    private List<String> videoStringList;

    VideoListManager(Context context) {

        videoItemsList = new LinkedList<>();
        Video video;

        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME}; // MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.VideoColumns.DATA
        Cursor videoItemsCursor = new CursorLoader(
                context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, null).loadInBackground();

        HashSet<String> videoItemsHashSet = new HashSet<>();
        try {
            videoItemsCursor.moveToFirst();
            do {
                video = new Video(
                        videoItemsCursor.getString(videoItemsCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)),
                        Uri.parse(videoItemsCursor.getString(videoItemsCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)))
                );
                videoItemsList.add(video);
                videoItemsHashSet.add(videoItemsCursor.getString(videoItemsCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
            } while (videoItemsCursor.moveToNext());
            videoItemsCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        videoStringList = new ArrayList<>(videoItemsHashSet);

        // TODO: DEBUG
        Uri videosInternalURI = MediaStore.Video.Media.INTERNAL_CONTENT_URI;
        Uri videosExternalURI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }

    List<Video> getVideoItemsList() {
        return videoItemsList;
    }

    List<String> getVideoStringList() {
        return videoStringList;
    }
}
