package hesso.smartvideoplayer;

import android.net.Uri;

/**
 * Created by Armand Delessert on 17.06.2017.
 */
class Video {

    private String videoName;
    private Uri videoURI;


    public Video(String videoName, Uri videoURI) {
        this.videoName = videoName;
        this.videoURI = videoURI;
    }

    public String getVideoName() {
        return videoName;
    }

    public Uri getVideoURI() {
        return videoURI;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public void setVideoURI(Uri videoURI) {
        this.videoURI = videoURI;
    }
}
