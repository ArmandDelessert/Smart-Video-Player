package hesso.smartvideoplayer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.easyvideoplayersample.R;

/**
 * Created by Armand Delessert on 18.06.2017.
 */

public class VideoFileSelectionActivity extends AppCompatActivity {

    private EasyVideoPlayer player;
    private VideoListManager videoListManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_file_selection_activity_layout);

        // Récupération du VideoListManager passé en paramètre
        videoListManager = (VideoListManager) getIntent().getSerializableExtra("VideoListManager");
        initVideoList();
    }

    /**
     * Initialisation de la liste des vidéos présentes sur l'appareil.
     */
    private void initVideoList() {
//        final VideoListManager videoListManager = new VideoListManager(this);

        final ListView playListListView = (ListView) findViewById(R.id.playListListView);
        final VideoItemCustomRowAdapter videoItemCustomRowAdapter = new VideoItemCustomRowAdapter(this, videoListManager.getVideoItemsList());
        playListListView.setAdapter(videoItemCustomRowAdapter);

        playListListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                videoListManager.getPlayer().setSource(Uri.parse(videoListManager.getVideoStringList().get(position)));
            }
        });
    }
}
