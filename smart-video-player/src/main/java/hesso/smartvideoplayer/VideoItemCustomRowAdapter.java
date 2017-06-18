package hesso.smartvideoplayer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.afollestad.easyvideoplayersample.R;

import java.util.List;

/**
 * Created by Armand Delessert on 17.06.2017.
 */
public class VideoItemCustomRowAdapter extends BaseAdapter {

    private List<Video> videoList;
    private static LayoutInflater layoutInflater;


    public VideoItemCustomRowAdapter(Activity activity, List<Video> videoList) {
        layoutInflater = activity.getLayoutInflater();
        this.videoList = videoList;
    }

    @Override
    public int getCount() {
        return videoList.size();
    }

    @Override
    public Object getItem(int position) {
        return videoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.video_custom_row, null);
        }

        TextView videoNameTextView = (TextView) view.findViewById(R.id.videoNameTextView);
        TextView videoPathTextView = (TextView) view.findViewById(R.id.videoPathTextView);

        videoNameTextView.setText(videoList.get(position).getVideoName());
        videoPathTextView.setText(String.valueOf(videoList.get(position).getVideoURI()));

        return view;
    }
}
